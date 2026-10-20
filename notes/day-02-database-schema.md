# Day 2 — Database Schema, JPA Entity and Repository

## 1. Today's objective

The objective was to introduce persistent account data into LiquidityLab.

We created an `accounts` table in PostgreSQL, mapped it to a Java entity using JPA, exposed database operations through Spring Data JPA, and wrote an integration test to verify that data can be saved and retrieved.

The main learning goal was to understand how Java objects become database rows and how database schema changes are managed safely.

## 2. Architecture we built

The persistence flow is:

Java application
    -> Account entity
    -> AccountRepository
    -> Spring Data JPA
    -> Hibernate ORM
    -> JDBC and PostgreSQL driver
    -> PostgreSQL database

Each component has a distinct responsibility.

- Java application: contains our application logic.
- Entity: represents a database-backed domain object.
- Repository: provides the persistence operations used by application code.
- Spring Data JPA: generates repository implementations and integrates them with Spring.
- Hibernate: implements the ORM behaviour and translates entity operations into SQL.
- JDBC: Java Database Connectivity, the standard Java API for communicating with relational databases.
- PostgreSQL driver: translates JDBC operations into the PostgreSQL wire protocol.
- PostgreSQL: stores and enforces the data.

## 3. Database schema and migration V1

File:

`backend/src/main/resources/db/migration/V1__create_accounts.sql`

The migration creates an `accounts` table with these columns:

| Column | Type | Purpose |
|---|---|---|
| id | BIGINT identity | Generated primary key |
| account_number | VARCHAR(34) | Unique account identifier |
| account_name | VARCHAR(120) | Human-readable account name |
| currency | Initially CHAR(3) | Three-character currency code |
| status | VARCHAR(20) | Account lifecycle status |
| created_at | TIMESTAMPTZ | Timestamp of creation |

### SQL concepts

- `CREATE TABLE`: defines a new relational table.
- `BIGINT`: an integer type with a larger range than a standard 32-bit integer.
- `GENERATED ALWAYS AS IDENTITY`: lets PostgreSQL generate the ID automatically.
- `PRIMARY KEY`: uniquely identifies each row and cannot be null.
- `VARCHAR(n)`: variable-length character data with a maximum length.
- `NOT NULL`: prohibits a missing value in the column.
- `UNIQUE`: prevents duplicate values in a column or column combination.
- `DEFAULT`: supplies a value when an insert omits the column or requests the default.
- `CURRENT_TIMESTAMP`: provides the current transaction timestamp in PostgreSQL.
- `TIMESTAMPTZ`: PostgreSQL timestamp type with time-zone-aware input/output semantics.
- `CHECK`: enforces a condition on stored values.

### Constraints in our migration

The account number must be unique.

The currency code must match three uppercase letters using:

`CHECK (currency ~ '^[A-Z]{3}$')`

Here, `~` is PostgreSQL's regular-expression match operator. The pattern requires exactly three uppercase letters. This is a format check, not a complete validation of whether a currency is officially supported.

The status is restricted to `ACTIVE` or `INACTIVE`.

These constraints are enforced by the database, even if an application bug attempts to insert invalid data.

## 4. Why we use Flyway

Flyway manages database schema changes through versioned migration scripts.

Our migration filename:

`V1__create_accounts.sql`

The naming convention is:

- `V`: marks a versioned migration.
- `1`: the migration version.
- `__`: separates the version from the description.
- `create_accounts`: a human-readable description.
- `.sql`: the migration contains SQL.

Flyway records applied migrations in its schema history table. On application startup, it checks which migrations have already run and applies pending migrations.

### Why we do not casually edit an applied migration

Once V1 has been applied, it is part of the database's migration history. Editing it later can cause an existing database and a newly created database to have different schemas.

Instead, we created a new migration:

`backend/src/main/resources/db/migration/V2__change_currency_to_varchar.sql`

Its content is:

```sql
ALTER TABLE accounts
ALTER COLUMN currency TYPE VARCHAR(3);
```

V2 changes the currency column from `CHAR(3)` to `VARCHAR(3)`.

This preserves the migration history and makes the change explicit. The currency format check remains in place.

## 5. What caused the currency type mismatch?

Our first schema validation failed with a message similar to:

`found [bpchar (Types#CHAR)], but expecting [varchar(3) (Types#VARCHAR)]`

PostgreSQL represents `CHAR(n)` internally as the fixed-length character type `bpchar`. Our Java entity mapping expected a variable-length character column.

The test exposed a disagreement between the database schema and the ORM mapping.

We resolved it by adding V2 rather than modifying V1 or deleting the database.

### General lesson

An entity mapping and the actual database schema must agree on compatible types, lengths, nullability and other mapping details.

`spring.jpa.hibernate.ddl-auto=validate` asks Hibernate to validate the schema. It does not automatically repair schema mismatches.

## 6. The Account JPA entity

File:

`backend/src/main/java/com/liquiditylab/backend/Account.java`

The entity represents one row of the `accounts` table.

Important annotations:

- `@Entity`: marks the class as a JPA entity.
- `@Table(name = "accounts")`: maps it to the `accounts` table.
- `@Id`: identifies the primary key field.
- `@GeneratedValue(strategy = GenerationType.IDENTITY)`: uses the database identity column to generate the ID.
- `@Column`: configures column mapping details such as name, length, nullability and whether the field participates in inserts or updates.

### Why the fields use Java types

- `Long id`: represents the generated numeric ID.
- `String accountNumber`: represents the account identifier.
- `String accountName`: represents the display name.
- `String currency`: represents the currency code.
- `String status`: represents the lifecycle status.
- `OffsetDateTime createdAt`: represents a date and time with an offset.

### The protected no-argument constructor

JPA needs a no-argument constructor to instantiate entities. A protected constructor satisfies this requirement while discouraging ordinary application code from creating incomplete entities accidentally.

The public constructor accepts the required account details:

`Account(String accountNumber, String accountName, String currency)`

The generated ID and creation timestamp are not passed by the caller.

## 7. The created_at database default bug

The database column was defined as:

`created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP`

Initially, `createdAt` was null in the Java object.

Hibernate generated an INSERT that explicitly supplied null for `created_at`. PostgreSQL therefore rejected the insert: a database default is not applied when an explicit NULL is supplied.

We changed the entity mapping to:

```java
@Generated(event = EventType.INSERT)
@Column(
    name = "created_at",
    nullable = false,
    insertable = false,
    updatable = false
)
private OffsetDateTime createdAt;
```

The corresponding imports are:

```java
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;
```

What the annotations and attributes mean:

- `insertable = false`: Hibernate omits the field from INSERT statements.
- `updatable = false`: Hibernate omits the field from UPDATE statements.
- `@Generated(event = EventType.INSERT)`: informs Hibernate that the database generates the value during insertion and that Hibernate should retrieve the generated value.

PostgreSQL now supplies the timestamp using its default, and Hibernate retrieves the generated value.

This makes the database the source of truth for the creation timestamp.

## 8. Why we separated identity from balance

The initial `accounts` table does not contain a current balance.

An account's identity and metadata are different from its financial position and transaction history. A future design may represent balances, ledger entries, transactions and reservations in separate structures.

This separation will become important when we study financial consistency and distributed systems.

## 9. The AccountRepository

File:

`backend/src/main/java/com/liquiditylab/backend/AccountRepository.java`

```java
package com.liquiditylab.backend;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
```

The repository extends `JpaRepository<Account, Long>`.

- `Account`: the entity managed by the repository.
- `Long`: the Java type of the entity's primary key.

Spring Data JPA creates the implementation at runtime. We do not have to manually implement the basic persistence operations.

Examples include:

- `save(account)`: saves a new entity or updates an existing one according to Spring Data JPA's entity-state handling.
- `findById(id)`: retrieves an entity by primary key and returns an `Optional`.
- `findAll()`: retrieves all entities.
- `deleteById(id)`: deletes by primary key.
- `existsById(id)`: checks whether a row exists.

An `Optional<Account>` represents the possibility that no matching account exists. Calling `orElseThrow()` lets application code handle the missing-result case explicitly.

## 10. Integration test

File:

`backend/src/test/java/com/liquiditylab/backend/AccountRepositoryTests.java`

The test uses `@SpringBootTest` to start the Spring application context and inject `AccountRepository`.

It creates an account, saves it, checks that an ID was generated, retrieves it by ID, and compares the stored account number, name and currency with the expected values.

This is an integration test because multiple components work together, including Spring, JPA, Hibernate, the database driver and PostgreSQL.

### Why the test failed on a later run

The first successful run inserted `LL-TEST-001` into the persistent local database.

The next run attempted to insert the same unique account number and PostgreSQL rejected it.

We added a `@BeforeEach` cleanup method that finds and deletes only the specific test account before each test.

This made repeated runs work, but the test still uses the development database. A more robust future setup should isolate test data using a dedicated test database or Testcontainers.

## 11. Useful commands

Run tests from the backend directory:

```bash
./mvnw test
```

Start PostgreSQL from the repository root:

```bash
docker compose up -d
docker compose ps
```

Inspect the table:

```bash
docker compose exec db psql -U liquiditylab -d liquiditylab -c "\d accounts"
```

Inspect rows:

```bash
docker compose exec db psql -U liquiditylab -d liquiditylab -c "SELECT * FROM accounts;"
```

Do not use `docker compose down -v` casually: the `-v` option removes named volumes and can delete local database data.

## 12. Debugging lessons

### Lesson A: A test file in the wrong directory

We accidentally placed `AccountRepositoryTests.java` under `src/main/java`.

Maven then tried to compile test-only imports such as JUnit as production code. Moving the file to `src/test/java` fixed the compilation error.

### Lesson B: A schema mismatch

Hibernate found `CHAR(3)` in PostgreSQL while the entity mapping expected `VARCHAR(3)`. We fixed the mismatch with V2.

### Lesson C: Database-generated timestamps

Hibernate explicitly supplied null for a non-null timestamp. We changed the mapping so PostgreSQL could apply its default and Hibernate could retrieve the generated value.

### Lesson D: Unique constraint violation

Repeated integration tests attempted to insert the same account number. We added targeted cleanup and identified the need for database isolation in a future iteration.

## 13. Revision questions

1. What is the difference between a primary key and a unique constraint?
2. Why does a database default not apply when an INSERT explicitly supplies NULL?
3. What is the difference between JPA and Hibernate?
4. What does `JpaRepository<Account, Long>` mean?
5. Why should we add V2 instead of editing an already-applied V1 migration?
6. What does `ddl-auto=validate` do?
7. Why is the test database currently less isolated than ideal?
8. Why did PostgreSQL's generated IDs increase even after test rows were deleted?

## 14. End-of-day summary

We created and verified the account schema, introduced versioned database migrations, mapped the schema to a JPA entity, added a Spring Data repository and successfully tested a database round trip.

The main takeaway: schema, ORM mapping and application assumptions must remain consistent. Database constraints are important safeguards, and migration history should be treated deliberately.
