# LiquidityLab — MVP Requirements

## 1. Business scenario

One fictional company operates multiple INR-denominated
bank accounts. It needs to view balances, transfer funds,
and move surplus funds into a designated target account.

All data and transactions are simulated.

## 2. Functional requirements

### FR-01: Account management

- Each account has a unique identifier.
- Each account has a currency and an operational status.
- Account balances and transaction history can be viewed.
- Only eligible accounts can participate in transfers.

### FR-02: Transfers

- A transfer specifies source, destination, amount,
  and an optional description.
- The amount must be greater than zero.
- Source and destination must be different accounts.
- Both accounts must exist and be eligible.
- Both accounts must use the same supported currency.
- The source must have sufficient available funds.
- A successful transfer updates both balances atomically.
- A failed transfer must not leave partial balance changes.

### FR-03: Transaction ledger

- Every successful transfer creates a debit and a credit.
- Both entries belong to the same transfer.
- Debit and credit amounts must be equal.
- Ledger entries are retained for transaction history.
- Balances must be reconcilable against ledger movements.

### FR-04: Cash sweeps

- A sweep rule identifies a source account, target account,
  and minimum balance threshold.
- The sweep transfers eligible funds above that threshold.
- The source must retain the configured threshold.
- Sweeps follow the same validation rules as transfers.
- Rules can be enabled or disabled.
- Each execution records its outcome and any transfer.

### FR-05: Audit trail

- Important actions record an event type and timestamp.
- Events reference the relevant account, transfer,
  sweep rule, or execution where applicable.
- Failed operations must not be recorded as successful transfers.
- Audit events can be reviewed.

### FR-06: Dashboard

- Display account balances and statuses.
- Display recent transactions and their outcomes.
- Allow users to initiate transfers.
- Allow users to review and trigger sweeps.
- Display execution results and useful error messages.

## 3. Financial correctness

- Use Java BigDecimal for monetary arithmetic.
- Use an appropriate decimal database type for money.
- Balance updates and ledger writes commit together.
- Ordinary transfers must not overspend an account.
- Concurrent transfers must not bypass balance validation.
- Repeated requests with the same idempotency key must
  not create duplicate transfers.
- Reconciliation must detect discrepancies between stored
  balances and opening balances plus ledger movements.

## 4. Non-functional requirements

- Use a modular monolith with clear business boundaries.
- Expose REST APIs using JSON.
- Validate inputs and return consistent error responses.
- Enforce relational integrity in PostgreSQL.
- Test important business invariants automatically.
- Document APIs and local setup.
- Support reproducible local deployment using Docker Compose.

## 5. Out of scope

- Real banking integrations or payment execution.
- Foreign-exchange conversion.
- Multiple companies or complex tenant isolation.
- Production scheduling of sweep jobs.
- Elaborate authentication and authorization.
- Microservices, Kubernetes, and cloud deployment.
- Claims of production financial or regulatory compliance.

## 6. Acceptance scenarios

### Successful transfer

Given two eligible INR accounts, a transfer of INR 50000
reduces the source balance by INR 50000, increases the
destination balance by INR 50000, and records matching
debit and credit ledger entries.

### Insufficient funds

Given a source balance of INR 10000, a transfer of
INR 15000 fails. Neither account balance changes, and
no successful-transfer ledger entries are created.

### Cash sweep

Given a source balance of INR 150000, a threshold of
INR 100000, and an eligible target, a successful sweep
transfers INR 50000 and leaves INR 100000 in the source.

### Duplicate request

Retrying a successful transfer with the same idempotency
key must not create another transfer or duplicate entries.

### Transaction rollback

If a critical database operation fails during a transfer,
all balance updates and ledger writes from that transfer
must roll back together.