# Day 3 — REST API, HTTP and Automated Controller Testing

## 1. Today's objective

The objective was to expose LiquidityLab's account data through HTTP endpoints.

We created a Spring MVC controller, connected it to the account repository, tested the endpoints using `curl`, improved missing-resource error handling, and added automated controller tests using MockMvc.

The main learning goal was to understand how an HTTP request becomes a Java method call and how the result becomes an HTTP response.

## 2. The application architecture

Our current request flow is:

HTTP client
    -> Spring MVC request mapping
    -> AccountController
    -> AccountRepository
    -> Spring Data JPA
    -> Hibernate
    -> PostgreSQL
    -> response serialized as JSON
    -> HTTP client

Each layer has a distinct responsibility.

- HTTP client: sends a request and displays the response.
- Spring MVC: matches the HTTP method and URL to a handler.
- Controller: coordinates the request and invokes application dependencies.
- Repository: provides data-access operations.
- JPA and Hibernate: map Java entities to relational data and execute persistence operations.
- PostgreSQL: stores the records and enforces database constraints.
- JSON serialization: converts Java return values into a representation suitable for an HTTP response.

This is a layered modular-monolith architecture. The components run in the same application process; the repository is not a separate network service.

## 3. The AccountController

File:

`backend/src/main/java/com/liquiditylab/backend/AccountController.java`

The controller is responsible for exposing account retrieval endpoints.

The class uses these annotations:

- `@RestController`: marks the class as a web controller whose return values are written to the response body. Objects are normally serialized as JSON.
- `@RequestMapping("/api/accounts")`: defines the common URL prefix for the controller.
- `@GetMapping`: maps HTTP GET requests to a handler method.
- `@GetMapping("/{id}")`: maps a GET request whose URL contains an ID segment.
- `@PathVariable`: binds the URL's ID segment to a Java method parameter.

### Constructor injection

The controller receives an `AccountRepository` through its constructor.

```java
private final AccountRepository accountRepository;

public AccountController(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
}
```

Spring creates and manages the controller and supplies the repository dependency.

Constructor injection makes the dependency explicit and allows the field to be final. It also makes the controller's dependency easier to understand and test.

### Endpoint 1: List accounts

`GET /api/accounts`

The handler calls:

`accountRepository.findAll()`

The repository returns a list of account entities. Spring MVC serializes that list into JSON.

When no accounts exist, a successful response can contain an empty JSON array:

```json
[]
```

When accounts exist, the response contains an array of JSON objects.

### Endpoint 2: Get one account

`GET /api/accounts/{id}`

For example:

`GET /api/accounts/5`

Spring binds the path segment `5` to a `Long` parameter. The controller calls `findById(5)`.

The repository returns an `Optional<Account>` because an account with that ID may or may not exist.

## 4. HTTP basics

HTTP is the protocol used by clients and servers to exchange requests and responses.

A request includes information such as:

- HTTP method, for example GET or POST.
- URL or path.
- Headers.
- An optional request body.

A response includes:

- Status code.
- Headers.
- An optional response body.

Our `curl` commands sent GET requests to the local Spring Boot server.

### Important status codes

| Status | Meaning | Example in LiquidityLab |
|---|---|---|
| 200 OK | Request succeeded | An account was retrieved |
| 404 Not Found | Requested resource was not found | Account ID 999 does not exist |
| 500 Internal Server Error | Unexpected server-side failure | An unhandled exception caused a server error |

The status code communicates the outcome independently of the response body.

## 5. Why the first missing-account request returned 500

Our initial controller implementation was:

```java
@GetMapping("/{id}")
public Account getAccountById(@PathVariable Long id) {
    return accountRepository.findById(id)
            .orElseThrow();
}
```

When the account did not exist, `findById()` returned an empty `Optional`. The no-argument `orElseThrow()` raised a `NoSuchElementException`.

We had not mapped this exception to the intended HTTP response. Spring therefore returned a 500 response.

A missing account is an expected condition, not necessarily an unexpected server failure. The API should communicate it as 404.

## 6. Explicit 404 handling

We changed the controller method to:

```java
@GetMapping("/{id}")
public Account getAccountById(@PathVariable Long id) {
    return accountRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Account not found: " + id
            ));
}
```

The imports are:

```java
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
```

Concepts:

- `HttpStatus.NOT_FOUND`: represents HTTP status 404.
- `ResponseStatusException`: allows a handler to raise an exception associated with a chosen HTTP status.
- Lambda expression `() -> ...`: supplies the exception to create when the `Optional` is empty.
- `orElseThrow(...)`: returns the value if present; otherwise, throws the supplied exception.

This keeps the endpoint concise while communicating the expected error correctly.

A future improvement would be centralized exception handling with `@ControllerAdvice`, especially as the API grows.

## 7. Testing the live HTTP server with curl

We started the application using:

```bash
./mvnw spring-boot:run
```

The application ran in the foreground. We used a second terminal to issue requests.

### List accounts

```bash
curl -i http://localhost:8080/api/accounts
```

The `-i` flag tells curl to include the HTTP response headers as well as the body.

We received HTTP 200 and a JSON array containing the test account.

### Retrieve one account

```bash
curl -i http://localhost:8080/api/accounts/5
```

We received HTTP 200 and the account JSON.

### Retrieve a missing account

```bash
curl -i http://localhost:8080/api/accounts/999
```

We received HTTP 404.

These were real HTTP requests to the running local server, not simulated controller calls.

## 8. Why account IDs changed between runs

Our test account initially had ID 4. After the test cleanup removed the row and the test inserted it again, the new row had ID 5.

PostgreSQL identity values are generated from a sequence-like mechanism. Deleting a row does not normally reset that generator.

Consequently, IDs can contain gaps. Clients must not assume that IDs are consecutive or that deleting a row makes its ID available for reuse.

The test account's ID is not a stable identifier for the test. Its account number is a more appropriate value for finding that particular test record.

## 9. Automated controller tests with MockMvc

File:

`backend/src/test/java/com/liquiditylab/backend/AccountControllerTests.java`

The test uses:

- `@SpringBootTest`: loads the Spring application context.
- `@AutoConfigureMockMvc`: configures MockMvc for the application.
- `@Autowired`: injects the configured `MockMvc` instance.
- `MockMvc`: performs simulated HTTP requests against the Spring MVC application.
- `get(...)`: constructs a simulated GET request.
- `andExpect(...)`: asserts a property of the response.
- `status().isOk()`: expects HTTP 200.
- `status().isNotFound()`: expects HTTP 404.

The first test verifies that `GET /api/accounts` returns HTTP 200.

The second test verifies that `GET /api/accounts/999999` returns HTTP 404.

### MockMvc versus curl

| MockMvc | curl |
|---|---|
| Runs inside automated tests | Runs as a command-line HTTP client |
| Does not need a network port for these requests | Sends requests to a running server |
| Can be executed repeatedly by Maven | Useful for manual exploration and debugging |
| Verifies Spring MVC request handling and responses | Verifies the externally reachable HTTP endpoint |

Both are useful, but they exercise the application differently.

These tests still load the application and use the configured database. They are not isolated from development data just because MockMvc is used.

## 10. Why TestRestTemplate did not compile

Our first controller-test draft used `TestRestTemplate`.

Compilation failed because the class was unavailable through the project's current test dependency setup.

The project already included `spring-boot-starter-webmvc-test`, so we inspected the dependencies instead of adding another dependency blindly.

We replaced the test approach with MockMvc, which is appropriate for testing Spring MVC routing and response behaviour without opening a real HTTP port.

The important debugging lesson is to distinguish a missing test API from a bug in application logic. Compilation errors should be diagnosed from the missing package and the dependency configuration.

## 11. Running the tests

From the `backend` directory:

```bash
./mvnw test
```

Maven compiles production code, compiles test code and executes the tests.

A successful build confirms that the configured tests completed without reported failures or errors. It does not automatically prove that every possible API behaviour is correct.

## 12. Limitations and next improvements

The current API supports read operations only.

Potential improvements include:

1. Add a POST endpoint to create accounts.
2. Validate incoming request data.
3. Introduce request and response DTOs instead of exposing JPA entities directly.
4. Add pagination to the list endpoint.
5. Add a consistent error-response format.
6. Add a dedicated service layer when business logic grows.
7. Isolate integration tests from the development database.
8. Add tests for successful retrieval, empty lists, invalid IDs and other edge cases.
9. Add API documentation.
10. Add authentication and authorization before exposing sensitive account information.

For a financial application, we should be especially deliberate about validation, access control, auditability and avoiding accidental exposure of internal persistence details.

## 13. Revision questions

1. What is the difference between HTTP GET and POST?
2. What does `@RestController` do?
3. What is the difference between `@RequestMapping` and `@GetMapping`?
4. Why does `findById()` return an `Optional`?
5. Why should a missing account return 404 rather than 500?
6. What does constructor injection achieve?
7. How does MockMvc differ from curl?
8. Why can database identity IDs have gaps?
9. What does an HTTP status code tell a client?
10. Why might a production API use DTOs instead of returning JPA entities directly?

## 14. End-of-day summary

We exposed two GET endpoints, verified the responses against the running application, changed missing-account handling from 500 to 404, and added automated MVC tests.

The main takeaway: an API is a contract with its clients. Correct HTTP methods, status codes, response formats and automated tests make that contract more predictable and easier to maintain.
