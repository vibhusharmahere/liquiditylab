# LiquidityLab Learning Notes

This folder contains detailed, day-by-day notes for building LiquidityLab and learning backend engineering, database design, REST APIs, and eventually distributed systems.

The notes explain not only what was implemented, but also why each decision was made, how the components work, which trade-offs matter, and what we learned from debugging.

## Learning log

1. [Day 1 — Project Foundation](day-01-project-foundation.md)
   - Development environment and tooling
   - Java, Maven, Spring Boot and Git
   - Docker Compose and PostgreSQL
   - Application configuration, health checks and the first test

2. [Day 2 — Database Schema, JPA Entity and Repository](day-02-database-schema.md)
   - Account schema and database constraints
   - Flyway migrations and schema evolution
   - JPA, Hibernate and entity mapping
   - Spring Data JPA repository
   - Database-generated timestamps and integration testing

3. [Day 3 — REST API, HTTP and Automated Controller Testing](day-03-rest-api.md)
   - Spring MVC and controller design
   - GET endpoints and JSON responses
   - HTTP status codes and missing-resource handling
   - Testing with curl and MockMvc
   - Debugging lessons and next improvements

## How to use these notes

- Read the day's notes after finishing the implementation.
- Revisit the debugging sections when a similar error appears.
- Try answering the revision questions without looking at the explanations.
- Keep future notes aligned with the actual code and decisions in the repository.

The notes are a learning record, not a substitute for checking the current source code when implementation details change.
