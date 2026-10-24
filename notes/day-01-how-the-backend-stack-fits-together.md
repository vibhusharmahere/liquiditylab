# LiquidityLab Day 1 --- How the Backend Stack Fits Together

## A practical mental model, using one account request

> Purpose: Understand how Java, Spring Boot, Spring MVC, Spring Data
> JPA, Hibernate, JDBC, PostgreSQL, Flyway, Actuator, Maven and JUnit
> fit together. The goal is not to memorize definitions, but to be able
> to explain the flow in your own words.

------------------------------------------------------------------------

## 1. Start with one concrete example

Imagine a client asks LiquidityLab:

`GET /api/accounts`

The request means: "Give me the accounts stored in the database."

Our application handles it through this chain:

``` text
Client (curl / browser / frontend)
        |
        | HTTP request: GET /api/accounts
        v
Web server + Spring Web MVC
        |
        | routes the request
        v
AccountController (our Java code)
        |
        | accountRepository.findAll()
        v
Spring Data JPA repository
        |
        v
Hibernate (JPA implementation)
        |
        | SQL + JDBC API + PostgreSQL driver
        v
PostgreSQL database
        |
        | rows returned
        v
JDBC driver -> Hibernate -> repository -> controller
        |
        | Java objects are serialized as JSON
        v
Spring MVC / web server -> HTTP response -> client
```

For an account-creation request, the broad path is similar, but the
controller would accept data from a request body and call a save
operation. Our current API exposes GET endpoints; the create-account
example is a conceptual illustration of a future POST endpoint.

**Important:** Not every component runs once per request. Flyway
normally checks/applies migrations during application startup, Actuator
exposes operational endpoints, and JUnit runs tests. They are part of
the overall application toolkit, but they are not all steps in every
account request.

------------------------------------------------------------------------

## 2. The components, explained through the example

### 2.1 Java --- the programming language

Java is the language in which we write application code.

Example:

``` java
Account account = new Account(
    "LL-1001",
    "ABC Corporation",
    "INR"
);
```

This creates a Java object representing an account. Java syntax and the
JVM do not automatically know how this object should be stored in
PostgreSQL. Other libraries and frameworks help connect the object to
web requests and persistence.

**Mental model:** Java is the language we use to express instructions
and application logic.

### 2.2 Spring Boot --- application setup and startup

Our entry point is:

``` java
@SpringBootApplication
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
```

Spring Boot makes it convenient to start and configure a Spring
application. It applies auto-configuration where appropriate,
initializes the Spring application context, helps wire configured
components, and starts the embedded web server when web support is
configured.

Spring Boot is itself software, largely written in Java. It is not a
magical compiler that compiles our source code every time a request
arrives.

**Mental model:** Spring Boot helps assemble and start the application.
It does not replace Java, Maven, or the JVM.

### 2.3 Spring Web MVC --- HTTP request routing

A client sends:

``` http
GET /api/accounts
```

Our controller contains:

``` java
@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    @GetMapping
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
}
```

Spring Web MVC maps the HTTP method and URL to the relevant controller
method. It also participates in binding request data and writing the
response.

**Mental model:** MVC routes the incoming web request to the right
handler and helps turn the handler's result into an HTTP response.

### 2.4 Spring Data JPA --- repository abstraction

Our repository is:

``` java
public interface AccountRepository
        extends JpaRepository<Account, Long> {
}
```

This declares that the repository manages `Account` entities whose IDs
are `Long` values. Spring Data JPA supplies a repository implementation
at runtime, including common methods such as:

-   `save(account)`
-   `findById(id)`
-   `findAll()`
-   `deleteById(id)`
-   `existsById(id)`

**Mental model:** Spring Data JPA gives application code a convenient
interface for common persistence operations, without us manually
implementing each one.

### 2.5 JPA --- a persistence specification and API

JPA (Jakarta Persistence) defines standard interfaces and annotations
for mapping Java objects to relational database data. Examples include
`@Entity`, `@Id`, and `@Column`.

JPA is not itself a database server, and it is not the same thing as
Hibernate.

**Mental model:** JPA defines the contract; an implementation library
carries out that contract.

### 2.6 Hibernate --- the ORM implementation

Hibernate is the ORM (Object-Relational Mapping) implementation used by
our application for JPA persistence.

A Java object contains fields. A relational database stores rows and
columns. Hibernate uses entity mappings to translate between these
representations and to generate database operations.

For example, it may generate SQL conceptually similar to:

``` sql
SELECT id, account_number, account_name, currency, status, created_at
FROM accounts;
```

The actual SQL depends on the mapping and Hibernate configuration.

Hibernate is a library name; it does not mean the application or
database is "hibernating."

**Mental model:** Hibernate is the persistence engine that implements
JPA behaviour and maps entities to SQL/database results.

### 2.7 JDBC --- Java Database Connectivity

JDBC is Java's standard API for communicating with relational databases.
Hibernate can use JDBC interfaces to execute SQL and retrieve results.

A database-specific JDBC driver translates JDBC operations into the
database's communication protocol.

**Mental model:** JDBC is the Java database-connectivity interface. The
driver connects that interface to a particular database.

### 2.8 PostgreSQL --- the database server

PostgreSQL stores the actual data in tables and enforces database rules
such as primary keys, unique constraints, `NOT NULL`, and `CHECK`
constraints.

For example, our `accounts` table has a unique account number.
PostgreSQL rejects an insert that attempts to reuse an account number
already present in the table.

PostgreSQL is a separate database server process in our local Docker
setup. It is not just another Java library inside the Spring Boot
application.

**Mental model:** PostgreSQL is the system that stores and protects the
relational data.

### 2.9 Flyway --- database schema migration history

A database needs a structure: tables, columns, constraints, indexes, and
so on. Flyway manages changes to that structure through versioned
migration scripts.

Example filename:

`V1__create_accounts.sql`

Later, a schema change can be added as a new migration, for example:

`V2__change_currency_to_varchar.sql`

Flyway records applied migrations and applies pending migrations
according to its configuration.

We use migrations rather than casually editing an already-applied
migration because existing databases may already have that earlier
version recorded and applied.

**Mental model:** Flyway manages versioned changes to the database
schema. It is not the ORM and does not handle every query in every
request.

### 2.10 Spring Boot Actuator --- operational information

Actuator exposes operational endpoints such as application health when
they are configured and exposed.

Our configuration includes:

``` properties
management.endpoints.web.exposure.include=health
```

A local health check can be requested with:

``` bash
curl -i http://localhost:8080/actuator/health
```

Health details depend on the health indicators and configuration
available. Actuator does not create accounts or implement account
business rules.

**Mental model:** Actuator helps operators inspect application health
and other operational information.

### 2.11 JUnit and Spring Boot test support --- automated checks

JUnit is a testing framework. Spring's test support can load application
components and provide testing utilities.

Examples from our project:

-   `BackendApplicationTests` checks whether the application context
    loads.
-   `AccountRepositoryTests` tests saving and retrieving an account
    using the configured database.
-   `AccountControllerTests` uses MockMvc to test HTTP/controller
    behaviour, including expected `200 OK` and `404 Not Found` statuses.

**Mental model:** Tests repeatedly check whether the application behaves
as expected. They are not normally part of handling a production user's
request.

------------------------------------------------------------------------

## 3. Build time versus runtime

This distinction is critical.

### Build time

When we run Maven tests or package the application, the build process
compiles Java source code and resolves dependencies.

A simplified picture:

``` text
Our .java source files
        |
        v
Maven build + Java compiler
        |
        v
.class files containing Java bytecode
        |
        v
Packaged application JAR + dependency libraries
```

Maven is the build tool. The Java compiler compiles Java source code.
Spring Boot does not normally compile the source code every time an HTTP
request arrives.

### Runtime

When the application runs:

``` text
JVM starts the application
        |
        v
Spring Boot initializes the Spring application context
        |
        v
Configured application components and web server run
        |
        v
Requests execute the already-built code
```

The JVM loads and executes Java bytecode. It may JIT-compile frequently
executed code into native machine code at runtime for performance.

A typical executable Spring Boot JAR packages application classes and
dependency libraries. Most of these libraries run in the same Java
process; they are not separate microservices or independent native
executables.

In our local setup, PostgreSQL is separate: the Spring Boot application
communicates with the PostgreSQL server over a connection using the
PostgreSQL driver.

### Commands from our project

Run tests from the `backend` directory:

``` bash
./mvnw test
```

Run the application from the `backend` directory:

``` bash
./mvnw spring-boot:run
```

Call the running API from another terminal:

``` bash
curl -i http://localhost:8080/api/accounts
```

Saving a `.java` file does not, by itself, guarantee that the running
application is using the new code. The source must be compiled and the
running application must reload/restart appropriately, unless a
configured development tool handles that workflow.

------------------------------------------------------------------------

## 4. Is Spring Boot like a project manager?

This analogy is partly useful, but it has limits.

Spring Boot helps assemble and start the application: it applies
configuration, initializes the Spring context, wires components, and
helps start the embedded server.

It does not personally execute every business operation, and it does not
automatically coordinate every service in a production system.

Different tools have different jobs:

  Tool/component                  Main responsibility
  ------------------------------- ----------------------------------------------------
  Java                            Programming language
  Maven                           Build, dependency management, and test lifecycle
  Java compiler                   Compiles Java source to bytecode
  JVM                             Loads and executes Java bytecode
  Spring Boot                     Application setup, auto-configuration, and startup
  Spring Web MVC                  HTTP request routing and web handling
  Spring Data JPA                 Repository abstraction
  JPA                             Persistence API/specification
  Hibernate                       ORM implementation
  JDBC + database driver          Java-to-database connectivity
  PostgreSQL                      Data storage and database rules
  Flyway                          Versioned schema migrations
  Actuator                        Operational endpoints
  JUnit and Spring test support   Automated testing

This separation of responsibilities is an example of **separation of
concerns**.

------------------------------------------------------------------------

## 5. What happens if we replace one component?

These boundaries provide flexibility, but not everything is
interchangeable without work.

### Replace Spring Boot

We could use another Java application setup or framework, while keeping
PostgreSQL. The database generally does not care which application
framework is used; it sees database connections, protocol messages and
SQL.

However, we would need to replace the Spring Boot-specific setup and
configure the application differently.

### Replace PostgreSQL

We could potentially use another relational database, but the JDBC
driver, SQL dialect, schema types, constraints, migrations and
configuration may need changes.

JPA reduces some coupling, but it does not erase all database-specific
behaviour.

### Replace Hibernate

Another JPA implementation could be used, but compatibility,
configuration, provider behaviour and dependencies would need to be
checked.

**General lesson:** Abstractions reduce coupling; they do not make
implementations perfectly interchangeable.

------------------------------------------------------------------------

## 6. Common misunderstandings to avoid

1.  **"Spring Boot compiles my code."**\
    Maven and the Java compiler handle the build. Spring Boot primarily
    helps configure and start the application.

2.  **"Every component is a separate binary or service."**\
    Many components are libraries running in one JVM process. PostgreSQL
    is a separate server process in our setup.

3.  **"JPA and Hibernate are the same thing."**\
    JPA is the persistence API/specification; Hibernate implements it.

4.  **"JDBC is the database."**\
    JDBC is a Java API. PostgreSQL is the database server.

5.  **"Flyway runs on every account query."**\
    Flyway normally checks/applies migrations as part of application
    startup or an explicitly configured migration process.

6.  **"Actuator implements the business logic."**\
    Actuator exposes operational information such as health.

7.  **"A passing build proves the entire application is correct."**\
    It proves that the configured build and tests passed. Test coverage
    determines which behaviours were actually checked.

8.  **"The database knows Spring Boot was replaced."**\
    Normally, the database only sees its client connection and database
    operations. It does not need to know the framework used by the
    client.

------------------------------------------------------------------------

## 7. The one-minute explanation to practise

Try explaining this without reading:

"When a client sends `GET /api/accounts`, the web server and Spring MVC
route the request to `AccountController`. The controller calls
`AccountRepository`. Spring Data JPA provides the repository
implementation; Hibernate implements JPA persistence and translates the
operation into SQL. Hibernate uses JDBC and the PostgreSQL driver to
communicate with PostgreSQL. PostgreSQL returns rows, which are mapped
into Java entities. The controller returns them, and Spring MVC
serializes the result into JSON for the HTTP response.

Maven and the Java compiler build the application before runtime. Spring
Boot configures and starts it. Flyway manages schema migrations,
Actuator exposes operational endpoints, and JUnit plus Spring test
support verify behaviour."

Do not aim to recite this word-for-word. Aim to explain the same flow
naturally.

------------------------------------------------------------------------

## 8. Self-check questions

Answer these in your own words before moving on:

1.  What is the difference between Java, Maven, the Java compiler, and
    the JVM?
2.  Does Spring Boot compile source code every time a request arrives?
3.  What does Spring MVC do when `GET /api/accounts` arrives?
4.  What does `JpaRepository<Account, Long>` tell Spring Data JPA?
5.  How do JPA and Hibernate differ?
6.  What is JDBC, and why is a PostgreSQL driver needed?
7.  Which component stores the account row?
8.  What is the difference between a Flyway migration and a Hibernate
    query?
9.  Why is PostgreSQL a separate process in our local Docker setup while
    Hibernate is a library in the application?
10. Which components participate in a typical account request, and which
    are primarily for startup, operations, build, or testing?

## 9. Final mental model

Think in three groups:

-   **Build and run the application:** Java, compiler, Maven, JVM,
    Spring Boot.
-   **Handle and persist a request:** web server, Spring MVC, our Java
    code, Spring Data JPA, JPA/Hibernate, JDBC driver, PostgreSQL.
-   **Support and maintain the application:** Flyway for schema changes,
    Actuator for operations, JUnit and Spring test support for testing.

The exact request path depends on the operation and configuration. The
key is to understand each component's responsibility and how data moves
between them---not to memorize a list of names.
