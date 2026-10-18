# LiquidityLab

LiquidityLab is a fictional corporate cash and liquidity
management application built for software engineering
and system design learning.

## Business problem

A company operates multiple bank accounts and needs to
monitor balances, transfer funds, manage surplus cash,
and maintain an auditable transaction history.

## MVP features

- Manage company accounts, currencies, and statuses.
- View account balances and transaction history.
- Transfer money between eligible accounts.
- Record balanced debit and credit ledger entries.
- Reject invalid transfers and insufficient-funds requests.
- Configure and execute threshold-based cash sweeps.
- Review sweep executions and audit events.
- Display balances, transactions, and sweep results in a dashboard.
- Test important business rules and transaction correctness.

## Technology stack

- Java and Spring Boot
- React and TypeScript
- PostgreSQL
- REST APIs using JSON
- Maven Wrapper
- JUnit and Spring integration testing
- OpenAPI / Swagger
- Git and GitHub
- Docker and Docker Compose

Exact stable dependency versions will be selected and verified
when we create the application skeleton.

## Architecture

The backend will be a modular monolith with separate
accounts, transactions, sweeps, and audit modules.

The React frontend will communicate with the backend
through REST APIs. PostgreSQL will persist application data.

## Project scope

This is a local learning and portfolio project using
fictional company data. It will not initiate real bank
payments, connect to real banking networks, or use
proprietary employer or customer information.

It is not intended for production financial use.

## Target deadline

13 December 2026.