# Day 01 — Project Foundation

**Date:** 18 October 2026  
**Focus:** Development environment, version control, PostgreSQL, Docker, Spring Boot, database connectivity, and automated testing  
**Status:** Completed

---

## 1. Today's objective

The objective was to establish a working development foundation for LiquidityLab.

Before implementing business features such as accounts, balances, transfers, and transactions, the application needs a reliable environment in which code can be written, tested, executed, and connected to a database.

Today's work established:

- A Java development environment
- A Git repository and GitHub remote
- A Spring Boot backend generated using Spring Initializr
- PostgreSQL running inside Docker
- A database connection between Spring Boot and PostgreSQL
- Initial automated test verification
- An application health endpoint

No business-domain features have been implemented yet. The application can start and connect to its database, but we have not created the Account, Balance, or Transaction tables.

---

## 2. What is LiquidityLab?

LiquidityLab is a learning project intended to develop practical software engineering and architecture skills.

The application will model a fictional corporate liquidity system. It will evolve from a working backend into an application with accounts, balances, transactions, transfers, and other liquidity-management features.

The long-term learning objectives include:

- Backend development
- Relational data modelling
- API design
- Testing and debugging
- Transaction management
- Service boundaries and modular architecture
- Distributed communication and failure handling as the application evolves

The project uses fictional data. It must not contain confidential information, source code, credentials, or proprietary designs from my employer.

The initial implementation is a Spring Boot application with PostgreSQL. More complex architecture will be introduced only when the requirements justify it.

---

## 3. Development environment

The following tools were verified during setup.

| Tool | Verified version or configuration | Purpose |
|---|---|---|
| Operating system | macOS on Apple Silicon | Local development machine |
| Java | Temurin JDK 21.0.12.1 | Compiles and runs Java code |
| `javac` | 21.0.12.1 | Java compiler |
| Spring Boot | 4.1.1 | Backend application framework |
| Maven Wrapper | `./mvnw` | Builds and tests the backend |
| Node.js | 24.14.1 | JavaScript runtime for frontend tooling |
| npm | 11.11.0 | JavaScript package manager |
| Git | 2.52.0 | Version control |
| Docker | 29.8.2 | Runs isolated containers |
| Docker Compose | 5.6.0 | Defines and manages local multi-container services |
| PostgreSQL | 17.11, running in a Docker container | Relational database |

Versions above are the versions observed during this session. They are not a claim that these are the latest available versions.

### 3.1 Java and the JDK

Java is the programming language used for the backend.

The Java Development Kit (JDK) contains the tools required to develop Java applications, including the Java compiler.

Important terms:

- **JDK:** Development kit containing the compiler and other development tools.
- **`javac`:** Compiles Java source code into Java bytecode.
- **JVM:** Java Virtual Machine, which executes Java bytecode.
- **Bytecode:** Platform-independent compiled representation of Java programs.
- **Java version:** The language and runtime version targeted by the project.

Our project targets Java 21. Spring Boot and its dependencies run on the Java runtime installed on the machine.

### 3.2 Maven and the Maven Wrapper

Maven is a Java build and dependency-management tool.

It helps us:

- Download dependencies
- Compile source code
- Compile tests
- Execute automated tests
- Package the application

The `pom.xml` file is Maven's project configuration file. It describes project metadata, dependencies, properties, and build plugins.

The Maven Wrapper is the repository's own Maven launcher. On macOS and Linux, we invoke it using:

```bash
./mvnw test
```

The Windows equivalent is `mvnw.cmd`.

Using the wrapper reduces dependence on a separately installed Maven version. It also helps team members use the Maven version configured by the project.

Important Maven terms:

- **Dependency:** A library or module required by the application.
- **Dependency management:** Rules governing dependency versions.
- **Plugin:** A tool Maven invokes to perform a build task.
- **Compile:** Transform source code into executable bytecode.
- **Test:** Run automated checks against the code.
- **Build:** The overall process of compiling, testing, and potentially packaging the application.

### 3.3 Node.js and npm

Node.js executes JavaScript outside a browser. npm manages JavaScript packages.

They will be useful when we develop the React and TypeScript frontend. They are not responsible for running the Java backend.

### 3.4 Git and GitHub

Git is a distributed version-control system. It records changes to project files and allows us to compare versions, create branches, and recover earlier states.

GitHub hosts Git repositories remotely, allowing us to back up and share the project.

Important terms:

- **Repository:** A project's files and version history.
- **Working tree:** The current files on disk.
- **Staging area:** The set of changes selected for the next commit.
- **Commit:** A recorded snapshot of selected changes.
- **Branch:** An independent line of development.
- **Remote:** A named reference to another Git repository.
- **Push:** Send local commits to a remote repository.
- **Pull:** Fetch and integrate changes from a remote repository.
- **`.gitignore`:** Rules specifying which untracked files Git should ignore.

The project has a `main` branch and a feature branch named `feature/project-foundation`. The feature branch is used for current development.

A typical Git workflow is:

1. Modify files.
2. Inspect changes.
3. Stage the intended changes.
4. Commit them with a meaningful message.
5. Push the commit to GitHub.

`git add .` stages changes under the current directory that Git is not instructed to ignore. It does not automatically commit or push them.

`git commit` records the staged changes locally.

`git push` transfers commits to the remote repository.

These are three different operations.

---

## 4. Backend architecture at this stage

The backend is a Java application built with Spring Boot.

The important components are:

- Java: the programming language
- Spring Boot: application framework and startup configuration
- Spring Web MVC: HTTP request-handling infrastructure
- Spring Data JPA: repository abstractions for persistence
- Hibernate: the ORM implementation used by JPA
- JDBC: Java's database connectivity API
- PostgreSQL: the relational database
- Flyway: database schema migration tool
- Spring Boot Actuator: operational endpoints such as application health
- JUnit and Spring Boot test support: automated testing infrastructure

These components have different responsibilities. They are not interchangeable.

### 4.1 Spring Boot

Spring Boot simplifies the creation and operation of Spring applications.

It provides auto-configuration, dependency integration, and application startup infrastructure.

Auto-configuration means Spring Boot attempts to configure components based on the dependencies present and the configuration supplied by the application.

For example, the presence of database dependencies and a configured database URL allows Spring Boot to configure a datasource.

Spring Boot does not eliminate the need to understand the underlying components. It automates much of the setup while still allowing the developer to control configuration and behaviour.

### 4.2 Spring Initializr

Spring Initializr generates a starter project with the selected language, build system, Spring Boot version, and dependencies.

It provided our initial backend structure, including `pom.xml`, the main application class, the test class, and `application.properties`.

Generated code is a starting point, not a completed application.

### 4.3 Dependencies in `pom.xml`

Our generated project includes these important dependencies:

| Dependency | Responsibility |
|---|---|
| `spring-boot-starter-actuator` | Operational endpoints such as health |
| `spring-boot-starter-data-jpa` | JPA integration and repository support |
| `spring-boot-starter-flyway` | Flyway integration with Spring Boot |
| `spring-boot-starter-validation` | Bean validation infrastructure |
| `spring-boot-starter-webmvc` | HTTP and Spring MVC infrastructure |
| `flyway-database-postgresql` | PostgreSQL-specific Flyway support |
| `postgresql` | PostgreSQL JDBC driver |
| Spring Boot test starters | Test support for the corresponding application components |

The PostgreSQL JDBC driver is declared with Maven scope `runtime`. This means it is needed when the application runs, but application source code does not generally need to compile directly against driver implementation classes.

The test dependencies have test scope. They are intended for the test lifecycle rather than the packaged application's normal runtime.

A **starter** is a convenient dependency bundle that brings in related libraries and integration. It is not itself the database or the application feature.

### 4.4 Spring Web MVC

Spring Web MVC provides infrastructure for handling HTTP requests and producing HTTP responses.

HTTP is the protocol used by clients and servers to exchange requests and responses.

For example, a future client could send:

```http
GET /api/accounts
```

The backend could respond with JSON representing accounts.

We have not implemented this business endpoint yet. Today we verified the operational health endpoint supplied by Actuator.

### 4.5 Spring Data JPA

JPA stands for Jakarta Persistence API. It defines standard interfaces and rules for mapping Java objects to relational database data.

Spring Data JPA adds repository abstractions that reduce the amount of persistence boilerplate we need to write.

For example, a future `AccountRepository` could provide operations for finding and saving account entities.

The test logs reported that Spring Data scanned for repository interfaces and found zero. This is expected because we have not created our own repositories yet.

### 4.6 Hibernate and ORM

Hibernate is the ORM implementation used by our JPA integration.

ORM stands for Object-Relational Mapping. It connects concepts in object-oriented programming with relational database structures.

For example, a future Java `Account` entity could map to a PostgreSQL `accounts` table.

- A Java object represents an application-level object.
- An entity is an object mapped to persistent data.
- A database row represents one record.
- A column represents one field in a row.

JPA defines the standard; Hibernate implements much of the ORM behaviour.

We have not yet created our own entity classes.

### 4.7 JDBC

JDBC stands for Java Database Connectivity.

It is a Java API for communicating with relational databases through a database driver.

In simplified terms:

Java application → JDBC API → PostgreSQL JDBC driver → PostgreSQL server

Spring Data JPA and Hibernate sit above this lower-level connectivity layer and use it to interact with the database.

### 4.8 Datasource and connection pool

A datasource provides database connections to the application.

Our logs showed HikariCP starting successfully.

HikariCP is a JDBC connection-pool implementation. Rather than opening a completely new database connection for every operation, a pool can maintain and reuse connections.

A pool helps manage connection creation, reuse, and limits.

The log messages:

- `HikariPool-1 - Starting...`
- `Added connection ...`
- `Start completed.`

showed that the application successfully established a PostgreSQL connection.

This was the critical evidence that the datasource configuration was working.

---

## 5. PostgreSQL and Docker

### 5.1 PostgreSQL

PostgreSQL is a relational database management system.

A relational database stores structured data in tables with rows and columns. Tables can be related using keys and constraints.

We will use PostgreSQL to store LiquidityLab's accounts, balances, transactions, and other business data.

Today's logs identified the connected database as PostgreSQL 17.11.

### 5.2 Docker

Docker runs applications and supporting services inside containers.

A container packages a process with its required filesystem and runtime environment, using the host operating system's kernel. It is not the same as a full virtual machine.

Containers help create more reproducible development environments across machines.

We ran PostgreSQL in Docker rather than installing and maintaining a separate database server directly on macOS.

### 5.3 Docker image and container

These terms have different meanings:

- **Image:** The packaged template from which a container is created.
- **Container:** A running or stopped instance created from an image.
- **Registry:** A service from which images can be downloaded or to which images can be uploaded.

Our image was `postgres:17-alpine`.

`postgres` identifies the image, `17` identifies the major PostgreSQL version, and `alpine` identifies the Alpine Linux-based image variant.

Docker downloaded the image and created the `liquiditylab-db` container.

### 5.4 Docker Compose

Docker Compose describes and manages services used together by a development environment.

We created `compose.yaml` in the project root.

Its important configuration is:

```yaml
services:
  db:
    image: postgres:17-alpine
    container_name: liquiditylab-db
    environment:
      POSTGRES_DB: liquiditylab
      POSTGRES_USER: liquiditylab
      POSTGRES_PASSWORD: localdevpassword
    ports:
      - "5432:5432"
    volumes:
      - liquiditylab-postgres-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U liquiditylab -d liquiditylab"]
      interval: 5s
      timeout: 5s
      retries: 5

volumes:
  liquiditylab-postgres-data:
```

#### `services`

Defines the services managed by Compose. Our configuration has one service named `db`.

#### `image`

Specifies which image Compose should use to create the database container.

#### `container_name`

Gives the container a recognizable name: `liquiditylab-db`.

#### `environment`

Supplies environment variables to the PostgreSQL container.

The official PostgreSQL image uses these variables during initial database setup:

- `POSTGRES_DB`: initial database name
- `POSTGRES_USER`: initial database user
- `POSTGRES_PASSWORD`: password for that user

These values are for local development only. They must not be treated as production credentials.

#### `ports`

```yaml
ports:
  - "5432:5432"
```

The format is:

`HOST_PORT:CONTAINER_PORT`

The first `5432` exposes a port on the Mac. The second is the PostgreSQL port inside the container.

This allows the backend running directly on the Mac to connect to PostgreSQL using `localhost:5432`.

If the backend were itself running in the same Compose network, it would generally connect to the database using the service name `db` and the container port instead.

#### `volumes`

```yaml
volumes:
  - liquiditylab-postgres-data:/var/lib/postgresql/data
```

A Docker volume stores database files outside the container's disposable writable layer.

This helps preserve database data when the container is stopped or recreated.

Stopping a container is different from deleting a volume. We must avoid commands that remove volumes unless we intentionally want to delete the local database data.

#### `healthcheck`

The health check runs:

```bash
pg_isready -U liquiditylab -d liquiditylab
```

`pg_isready` checks whether PostgreSQL is accepting connections.

Compose uses the health-check result to report whether the database service is healthy.

It is a useful readiness signal, but it does not prove that every application query or business operation will work.

### 5.5 Docker commands used

Start services in the background:

```bash
docker compose up -d
```

- `docker compose`: invokes Compose.
- `up`: creates and starts the configured services.
- `-d`: detached mode, meaning the command returns while services run in the background.

Inspect service status:

```bash
docker compose ps
```

The database showed `Up ... (healthy)`.

Stop services without deleting their stored volume:

```bash
docker compose stop
```

Restart them later:

```bash
docker compose up -d
```

We verified that the database container was running and healthy before configuring Spring Boot.

---

## 6. Application configuration

The file is:

`backend/src/main/resources/application.properties`

It currently contains:

```properties
spring.application.name=liquiditylab

# Database connection
spring.datasource.url=jdbc:postgresql://localhost:5432/liquiditylab
spring.datasource.username=liquiditylab
spring.datasource.password=localdevpassword
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false

# Flyway database migrations
spring.flyway.enabled=true

# Health endpoint
management.endpoints.web.exposure.include=health
```

### 6.1 Properties files

A `.properties` file stores configuration as key-value pairs.

For example:

```properties
spring.application.name=liquiditylab
```

The key is `spring.application.name`; the value is `liquiditylab`.

Spring Boot reads configuration and uses it to configure the application.

The `#` character starts a comment in this file.

### 6.2 Application name

```properties
spring.application.name=liquiditylab
```

Gives the application a meaningful name that can appear in logs and other integrations.

It does not create a database or a database user.

### 6.3 JDBC URL

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/liquiditylab
```

Breaking it down:

- `jdbc`: the Java database connectivity protocol prefix
- `postgresql`: the database driver/protocol family
- `localhost`: the host reachable from the Mac itself
- `5432`: the exposed PostgreSQL port
- `liquiditylab`: the database name

The URL identifies the database endpoint to which the backend should connect.

### 6.4 Database credentials

```properties
spring.datasource.username=liquiditylab
spring.datasource.password=localdevpassword
```

These identify the database user and its password.

They match the initial user and password configured in `compose.yaml`.

This is a disposable local development credential. It is not suitable for production.

Before making a public repository or deploying the application, credentials should be supplied through environment variables or a proper secrets-management mechanism rather than committed as real secrets.

### 6.5 JDBC driver class

```properties
spring.datasource.driver-class-name=org.postgresql.Driver
```

Identifies the PostgreSQL JDBC driver implementation.

The PostgreSQL driver dependency is already present in `pom.xml`, so we did not need to add another driver dependency.

### 6.6 Hibernate schema validation

```properties
spring.jpa.hibernate.ddl-auto=validate
```

This tells Hibernate to validate the database schema against the entity mappings it knows about.

It does not ask Hibernate to create or update tables.

This is useful when database schema changes are deliberately managed by migrations.

We currently have no custom entities or business tables, so this initial test did not validate any of our future business tables.

### 6.7 Open Session in View

```properties
spring.jpa.open-in-view=false
```

This disables Spring's Open EntityManager in View behaviour for web requests.

Keeping it disabled helps encourage deliberate transaction and persistence boundaries instead of relying on database access during response rendering.

As we build APIs, we will need to manage transactions and loading of related data deliberately.

### 6.8 Flyway enabled

```properties
spring.flyway.enabled=true
```

Flyway manages database schema changes through ordered migration scripts.

Instead of relying on Hibernate to create tables automatically, we can define versioned SQL files and apply them in sequence.

For example, a future file could be:

`backend/src/main/resources/db/migration/V1__create_accounts.sql`

Flyway records applied migrations in a history table, allowing it to track which schema changes have run.

We have not created this migration yet.

### 6.9 Actuator health exposure

```properties
management.endpoints.web.exposure.include=health
```

Spring Boot Actuator provides operational endpoints.

This configuration exposes the `health` endpoint over HTTP. It is separate from our future business API endpoints.

We used it to check whether the running application was healthy.

Only endpoints needed by the application should be exposed, especially in production.

---

## 7. The first database configuration error

### 7.1 What happened?

The first execution of:

```bash
./mvnw test
```

failed when Spring Boot attempted to start the test application context.

The error stated that the datasource URL was not specified and no suitable embedded database could be configured.

### 7.2 Why did it happen?

The initial `application.properties` contained only:

```properties
spring.application.name=backend
```

However, the application included JPA, Flyway, and the PostgreSQL driver.

Because database-related components were present, Spring Boot attempted to configure and initialize database access. There was no database URL configured.

The driver dependency alone was not enough. The application also needed to know where the database was and which credentials to use.

### 7.3 How did we fix it?

We:

1. Started PostgreSQL using Docker Compose.
2. Verified that the container became healthy.
3. Added the JDBC URL and credentials to `application.properties`.
4. Configured Hibernate schema validation.
5. Kept Flyway enabled.
6. Reran the automated test.

The next test run connected successfully to PostgreSQL.

This illustrates an important debugging principle:

**An installed dependency does not automatically mean the external service is running or correctly configured.**

---

## 8. What the successful test proved

The command was:

```bash
./mvnw test
```

The final output included:

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

BUILD SUCCESS
```

The logs also showed:

- Spring Boot started the test application context.
- Spring Data scanned for repository interfaces and found zero.
- HikariCP established a database connection.
- Flyway connected to PostgreSQL.
- Hibernate initialized its persistence unit.
- Actuator exposed one endpoint beneath `/actuator`.

### 8.1 What is an application context?

The Spring application context is the container that manages application objects, called beans, and their dependencies.

During startup, Spring Boot builds this context and configures the components needed by the application.

A context-load test verifies that the application can start in the test environment with the configured components.

### 8.2 What is a test?

An automated test is executable code that checks expected behaviour or setup.

The generated `BackendApplicationTests` test class uses Spring's test infrastructure to load the application context.

One passing test is useful, but it does not mean the business application is complete. We still need tests for database migrations, account operations, validation, transfers, and other behaviour.

### 8.3 What is a test failure versus an error?

- **Failure:** A test ran but an assertion or expected condition did not hold.
- **Error:** A test could not complete normally because an unexpected exception or setup problem occurred.
- **Skipped:** A test was not executed.

The final run reported zero failures and zero errors.

### 8.4 What is a build?

The build process executes configured Maven phases, including compilation and tests.

`BUILD SUCCESS` means Maven completed the requested test goal successfully.

It does not mean every future feature has been implemented or tested.

---

## 9. Understanding the important log messages

### HikariCP

```text
HikariPool-1 - Start completed.
```

The datasource connection pool initialized successfully.

### PostgreSQL

```text
Database: jdbc:postgresql://localhost:5432/liquiditylab (PostgreSQL 17.11)
```

Flyway reported the database endpoint and server version.

### Flyway

```text
Successfully validated 0 migrations
No migrations found.
```

There were no migration scripts to validate. This warning was expected because we had not created any migrations yet.

Flyway then created its schema-history table and reported that the empty schema was up to date.

### Hibernate

```text
Initialized JPA EntityManagerFactory
```

The JPA persistence infrastructure initialized successfully.

### Actuator

```text
Exposing 1 endpoint beneath base path '/actuator'
```

The application exposed one configured management endpoint.

### External repository metadata warnings

Maven also reported HTTP 401 warnings for metadata URLs associated with two external repositories.

Despite those warnings, Maven resolved the required dependencies and completed the build.

Since the build succeeded, these warnings did not block today's work. If dependency resolution later fails, we should investigate the relevant repository configuration rather than assuming all warnings are harmless.

### Mockito and Java agent warnings

The test output also contained warnings about Mockito self-attachment and dynamic Java-agent loading.

They did not cause the test to fail. We can revisit them if the test configuration or Java version requires changes.

---

## 10. Running the application separately

A test that loads the application context is different from running the backend as a standalone process.

We started the backend with:

```bash
./mvnw spring-boot:run
```

This runs the application through Maven's Spring Boot plugin.

The server runs until it is stopped, usually with `Ctrl + C` in the terminal where it was started.

We then checked the health endpoint from a second terminal:

```bash
curl -i http://localhost:8080/actuator/health
```

### Understanding the command

- `curl` is a command-line tool for making HTTP requests.
- `-i` includes the HTTP response headers.
- `http://localhost:8080` refers to the local machine and the application's default HTTP port.
- `/actuator/health` is the Actuator health endpoint.

An HTTP `200 OK` response with a healthy status indicates that the endpoint responded successfully.

The user confirmed that the standalone application and health check worked.

This verifies more than the context-load test alone: the application can also run as a server and respond to an HTTP request.

---

## 11. Line-ending warning from Git

When staging files, Git displayed:

```text
warning: in the working copy of 'backend/mvnw.cmd',
LF will be replaced by CRLF the next time Git touches it
```

This is a line-ending warning, not a failed `git add`.

Line endings mark where one line ends and another begins.

- **LF (`\n`):** Common on macOS and Linux.
- **CRLF (`\r\n`):** Common in Windows text files.

`mvnw.cmd` is the Windows version of the Maven Wrapper launcher. Its line endings may differ from those of Unix shell scripts.

Git can normalize or convert line endings based on repository settings and attributes.

We do not need to modify this file just because the warning appeared. We should check the staged changes and ensure the wrapper remains intact.

---

## 12. Architecture decisions and trade-offs

### Why PostgreSQL?

We need a relational database for structured financial-like records and relationships.

PostgreSQL supports transactions, constraints, and relational queries that will be important as the application develops.

### Why Docker?

Docker provides a repeatable way to run PostgreSQL without requiring a separate local installation.

The trade-off is that Docker Desktop must be running and the configured port must be available.

### Why Spring Boot?

Spring Boot gives us a mature Java application framework with integrated HTTP handling, persistence support, configuration, and testing tools.

The trade-off is that auto-configuration can hide some underlying mechanics. We will study those mechanics rather than treating Spring Boot as magic.

### Why Flyway?

Schema changes need a reliable, reviewable history.

Versioned SQL migrations let us change the database deliberately and reproducibly.

The trade-off is that migrations need care: once applied to shared or persistent environments, changing historical scripts can create inconsistencies.

### Why `ddl-auto=validate`?

We want database schema changes to be deliberate and managed through migrations rather than generated implicitly by Hibernate.

The trade-off is that the schema must exist and match the entity mappings once we introduce entities.

### Why start simple?

We are beginning with one backend application and one database.

This keeps early development and debugging manageable. Distributed services, messaging, and more complex consistency problems should be introduced when the application has enough functionality to make those trade-offs meaningful.

---

## 13. Security and configuration hygiene

The current database username and password are disposable local development values.

They must not be reused in production.

Before deploying the application or sharing a repository containing real credentials:

- Read secrets from environment variables or a secrets manager.
- Never commit real credentials, access tokens, private keys, or `.env` files.
- Review staged changes before committing.
- Keep generated build output out of version control.
- Do not put proprietary company data or code into this project.

A `.gitignore` file helps exclude unwanted generated files, but it does not remove a file that Git is already tracking.

---

## 14. Day 01 summary

Today we established the foundation needed for future application development.

The key outcome is not merely that one test passed. It is that we understand the main path through the development environment:

1. Java runs the backend application.
2. Spring Boot configures the application.
3. Maven builds and tests the project.
4. Docker runs PostgreSQL.
5. The JDBC URL and credentials tell the backend how to connect.
6. HikariCP manages database connections.
7. Hibernate and JPA provide persistence infrastructure.
8. Flyway is ready to manage schema migrations.
9. Actuator exposes the health endpoint.
10. Git records our work so it can be reviewed and recovered.

### Current state

- Development tools verified
- Git repository and feature branch established
- PostgreSQL container running successfully
- Backend datasource configured
- Spring Boot context test passing
- Standalone application health check confirmed
- No custom database migrations or business entities implemented yet

---

## 15. Revision questions

Try to answer these without looking at the explanations above.

1. What is the difference between the JDK, JVM, and `javac`?
2. Why use the Maven Wrapper instead of relying only on an installed Maven version?
3. What is the difference between Git staging, committing, and pushing?
4. What is the difference between a Docker image and a container?
5. What does `docker compose up -d` do?
6. Why does the database volume matter?
7. Why was a PostgreSQL JDBC driver dependency insufficient without a datasource URL?
8. What is the difference between JDBC, JPA, Hibernate, and Spring Data JPA?
9. What does HikariCP do?
10. Why use Flyway if Hibernate already understands database entities?
11. What does `spring.jpa.hibernate.ddl-auto=validate` mean?
12. Why did Flyway report that no migrations were found?
13. What does a Spring application context test verify?
14. What does `BUILD SUCCESS` prove, and what does it not prove?
15. What does `/actuator/health` tell us?
16. What is the difference between LF and CRLF?
17. Why should real credentials never be committed to a repository?
18. Why are we starting with a single backend instead of immediately splitting everything into microservices?

---

## 16. Day 02 — next session

**Objective:** Create the first deliberate database schema and the first domain model.

Planned work:

1. Review the initial domain requirements.
2. Decide what an Account represents in LiquidityLab.
3. Create the first Flyway migration.
4. Define the initial `accounts` table with appropriate columns and constraints.
5. Create the Java Account entity.
6. Keep schema changes under Flyway rather than asking Hibernate to generate tables.
7. Add tests that verify the migration and persistence setup.
8. Review the implementation and its trade-offs.

The exact table fields and constraints should be decided before writing the migration. They should reflect LiquidityLab's fictional requirements rather than assumptions about any proprietary company system.

**End of Day 01.**