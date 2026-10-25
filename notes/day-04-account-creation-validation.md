# Day 4 — Creating Accounts, Validation, and Error Handling

## What we built

In Days 2–3, LiquidityLab could read accounts:

- `GET /api/accounts` — list accounts
- `GET /api/accounts/{id}` — fetch one account
- A missing account returns `404 Not Found`

On Day 4, we added account creation and learned how Spring MVC handles invalid requests and database exceptions.

## 1. The request DTO

File: `src/main/java/com/liquiditylab/backend/CreateAccountRequest.java`

```java
package com.liquiditylab.backend;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(

        @NotBlank
        @Size(max = 34)
        String accountNumber,

        @NotBlank
        @Size(max = 120)
        String accountName,

        @NotBlank
        @Pattern(regexp = "^[A-Z]{3}$")
        String currency

) {
}
```

### Why use a request DTO?

A DTO (Data Transfer Object) represents the data accepted by the API. It separates incoming API input from the database entity.

- `record` is a concise Java data carrier. Java generates its constructor and accessor methods.
- `@NotBlank` rejects null, empty, or whitespace-only text.
- `@Size(max = ...)` limits the maximum length.
- `@Pattern(regexp = "^[A-Z]{3}$")` accepts exactly three uppercase letters, such as `INR` or `USD`.

Writing these annotations defines validation rules, but the request must also be validated by Spring. We do that with `@Valid` on the controller parameter.

## 2. The POST endpoint

File: `src/main/java/com/liquiditylab/backend/AccountController.java`

The controller method we added:

```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public Account createAccount(
        @Valid @RequestBody CreateAccountRequest request) {

    Account account = new Account(
            request.accountNumber(),
            request.accountName(),
            request.currency()
    );

    return accountRepository.save(account);
}
```

Relevant imports:

```java
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
```

### Request flow

1. A client sends a `POST /api/accounts` request with JSON.
2. Spring MVC converts the JSON into a `CreateAccountRequest`.
3. `@Valid` asks Spring to validate the request.
4. The controller creates an `Account` entity from the validated values.
5. `accountRepository.save(account)` asks the persistence layer to save it.
6. On success, `@ResponseStatus(HttpStatus.CREATED)` sets the response status to `201 Created`.
7. The response contains the saved account, including generated fields such as its database ID and creation timestamp.

Example request:

```bash
curl -i -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "LL-1001",
    "accountName": "Mumbai Operations",
    "currency": "INR"
  }'
```

Observed result: `201 Created`, with the saved account JSON. The response included an ID, `createdAt`, and `status: ACTIVE`; these were not included in the request body.

## 3. Input validation: `400 Bad Request`

We tested two invalid requests:

### Invalid currency

The request used `"currency": "INDIA"`. Our pattern requires exactly three uppercase letters, so the API returned `400 Bad Request`.

### Blank account name

The request used `"accountName": "   "`. `@NotBlank` rejects whitespace-only text, so the API returned `400 Bad Request`.

**Key idea:** validation is performed at the API boundary. Invalid input is rejected before the controller proceeds to create and save an account.

## 4. Duplicate account numbers and database constraints

The `accounts` table has a unique constraint on `account_number`. We sent another request with the already-existing account number `LL-1001`.

Initially, the API returned `500 Internal Server Error`. The database rejected the duplicate, but the application did not have a suitable exception handler for the resulting integrity exception.

This demonstrated an important distinction:

- Request validation checks rules such as required fields, maximum length, and currency format.
- The database's `UNIQUE` constraint protects data integrity, including against duplicates.
- An exception handler translates a persistence-layer failure into an appropriate HTTP response.

## 5. Global exception handling

File: `src/main/java/com/liquiditylab/backend/GlobalExceptionHandler.java`

```java
package com.liquiditylab.backend;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDataIntegrityViolation(
            DataIntegrityViolationException exception) {

        return "Request conflicts with existing data or a database constraint.";
    }
}
```

After adding this class, the same duplicate request returned `409 Conflict`.

### What the annotations mean

- `@RestControllerAdvice` lets Spring discover a centralized controller-advice component.
- `@ExceptionHandler(DataIntegrityViolationException.class)` registers the method to handle this exception type.
- `@ResponseStatus(HttpStatus.CONFLICT)` sets the HTTP response status to `409`.
- The returned string becomes the response body.

### How does Spring call this method if the controller never calls it?

The controller does **not** directly call `GlobalExceptionHandler`.

At application startup, Spring discovers annotated components and registers relevant handler methods. When an exception occurs during request processing, Spring MVC's exception-resolution mechanism looks for a matching registered handler and invokes it.

This is an example of **inversion of control**: application code provides components and annotations; the framework manages when they are invoked.

### Important limitation of the current implementation

This handler maps every `DataIntegrityViolationException` to `409`, not just duplicate account numbers. That is acceptable for this learning step, but a production API should identify specific constraint failures and return a structured, precise error response. The current message is intentionally generic.

## 6. Controller tests with MockMvc

File: `src/test/java/com/liquiditylab/backend/AccountControllerTests.java`

We added tests for:

- A valid POST request returns `201 Created`.
- Invalid currency returns `400 Bad Request`.
- Creating an account and then sending the same account number again returns `409 Conflict`.

The test requests use JSON and `MockMvc`:

```java
mockMvc.perform(post("/api/accounts")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
                {
                  "accountNumber": "LL-TEST-POST-101",
                  "accountName": "Test Account",
                  "currency": "INR"
                }
                """))
        .andExpect(status().isCreated());
```

`MockMvc` exercises Spring MVC request handling without starting a real network listener. This differs from the manual `curl` checks, which sent real HTTP requests to the running application.

The existing GET tests remain:

- `GET /api/accounts` returns `200 OK`.
- `GET /api/accounts/999999` returns `404 Not Found` when that account ID does not exist.

After adding the POST tests, `./mvnw test` completed successfully.

### Test-data caveat

These tests currently use the configured PostgreSQL database. Created test rows can remain after a test run, and repeating tests with the same unique account numbers can cause conflicts. The current setup is useful for learning, but it is not ideal test isolation.

A future improvement is to use a dedicated test database or Testcontainers and clean up test data reliably.

## 7. How annotations and method calls differ

A method can compile even if nothing ever calls it:

```java
public String hello() {
    return "Hello";
}
```

Java's compiler checks whether the code is valid; it does not require every method to be called.

Annotations only have special behaviour when a framework or library recognizes and processes them:

- `@GetMapping` registers a method for matching HTTP GET requests.
- `@ExceptionHandler` registers a method for matching exceptions.
- `@Component` tells Spring that a class can be managed as a bean; it does not automatically execute every method in that class.

Changing a method name such as `handleDataIntegrityViolation` generally does not break handler registration, because Spring uses the annotation and method signature rather than that particular method name. Removing `@ExceptionHandler`, however, means Spring MVC will not register that method through this annotation mechanism.

## 8. The overall POST request flow

```text
HTTP POST /api/accounts
        |
        v
Spring MVC receives the request
        |
        v
JSON -> CreateAccountRequest
        |
        v
@Valid checks request fields
        |
        +---- invalid input ----> 400 Bad Request
        |
        v
AccountController creates Account
        |
        v
AccountRepository.save(...)
        |
        v
Hibernate/JDBC communicate with PostgreSQL
        |
        +---- integrity exception ----> GlobalExceptionHandler -> 409 Conflict
        |
        v
Successful save -> 201 Created + account JSON
```

## 9. Day 4 summary

By the end of Day 4, LiquidityLab can:

- Read all accounts.
- Read an account by ID and return `404` when missing.
- Create an account through `POST /api/accounts`.
- Reject invalid input with `400`.
- Map a database integrity exception to `409`.
- Automatically test the GET and POST endpoint behaviours.

### Key concepts to remember

1. **DTO:** API input shape.
2. **Validation:** Rejects invalid request fields before saving.
3. **Entity:** Represents the persisted account.
4. **Repository:** Provides persistence operations.
5. **Database constraint:** Protects data integrity even if application-level checks are missing.
6. **Global exception handler:** Maps matching exceptions to HTTP responses.
7. **MockMvc vs. curl:** MockMvc tests Spring MVC without a network listener; curl exercises the running HTTP server.
8. **Annotations are not magic:** Framework code reads them and implements their documented behaviour.

## Self-check questions

Try answering these without looking at the notes:

1. Why do we use `CreateAccountRequest` instead of accepting the `Account` entity directly?
2. What does `@Valid` do in the POST method?
3. Why did the invalid currency request return `400`?
4. Why did the duplicate account initially return `500`?
5. What changed when we added `GlobalExceptionHandler`?
6. Does `@Component` automatically execute every method in a class?
7. How is a MockMvc test different from a manual curl request?
8. Why can repeated test runs conflict when they use a persistent PostgreSQL database?
