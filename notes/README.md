# LiquidityLab — Engineering Notes

A detailed engineering notebook documenting the development of LiquidityLab, the concepts behind each implementation decision, the problems encountered, and the lessons learned.

The purpose of these notes is not just to record commands. They explain why we use a technology, how it works, how it connects to other components, and what trade-offs it introduces.

## Learning objectives

Through LiquidityLab, I want to develop practical understanding of:

- Backend development with Java and Spring Boot
- REST APIs and HTTP communication
- Relational database design and PostgreSQL
- SQL, database migrations, and schema management
- JPA, Hibernate, and JDBC
- Automated testing and debugging
- Git, GitHub, branches, and incremental delivery
- Docker and reproducible development environments
- Software architecture and modular design
- Distributed systems, service communication, consistency, and failure handling as the project evolves

## Session index

| Session | Topic | Status |
|---|---|---|
| [Day 01 — Project Foundation](day-01-project-foundation.md) | Development environment, Git, Docker, PostgreSQL, Spring Boot, database connectivity, and first test | Completed |
| Day 02 — Database Schema | Initial schema, Flyway migration, and Account domain model | Planned |

## How to use these notes

Each session documents:

1. The objective and what was built
2. Concepts and terminology
3. Files and configuration changes
4. How the components work together
5. Problems encountered and their root causes
6. Architectural decisions and trade-offs
7. Verification and test results
8. Key takeaways and revision questions
9. The next session's starting point

## Ground rules

- Document what was actually implemented and verified.
- Clearly distinguish facts, assumptions, and future plans.
- Prefer understanding over blindly copying commands.
- Keep credentials, production secrets, and proprietary company information out of the repository.
- Update the relevant session notes before closing each development session.

## Project repository

The project root contains the application, development infrastructure, requirements, and engineering documentation. These notes complement the main project README and technical documentation; they do not replace them.