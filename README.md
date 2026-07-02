# Bank Account Management System

A full-stack bank account management application built with **Spring Boot 4.1** (REST API) and **Angular 19** (SPA frontend).

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Backend Setup](#backend-setup)
  - [Frontend Setup](#frontend-setup)
- [API Reference](#api-reference)
- [Database Schema](#database-schema)
- [Features](#features)

---

## Overview

This project consists of two parts:

- **Part 1** — REST API for managing bank accounts: deposits, withdrawals, currency exchange and transaction history.
- **Part 2** — Angular SPA that consumes the REST API, with account overview, infinite scroll transaction history, balance history chart and PDF export.

---

## Tech Stack

### Backend
| Technology | Version | Purpose |
|---|---|---|
| Java | 25 | Language |
| Spring Boot | 4.1.0 | Framework |
| Spring Data JPA | — | Database access |
| Spring Validation | — | Request validation |
| H2 Database | 2.4.240 | In-memory SQL database |
| Hibernate | 7.4.1 | ORM |
| Lombok | 1.18.46 | Boilerplate reduction |

### Frontend
| Technology | Version | Purpose |
|---|---|---|
| Angular | 19 | SPA framework |
| TypeScript | — | Language |
| Chart.js + ng2-charts | — | Balance history chart |
| jsPDF + jspdf-autotable | — | PDF export |
| SCSS | — | Styling |

---

## Project Structure

```
bank-account/               ← Spring Boot backend
├── src/main/java/com/bank/platform/account/
│   ├── controllers/        ← REST controllers
│   ├── service/            ← Business logic
│   ├── entity/             ← JPA entities
│   ├── dto/                ← Request/Response DTOs
│   ├── enums/              ← Currency, TransactionType enums
│   ├── repository/         ← Spring Data repositories
│   ├── config/             ← WebConfig (CORS)
│   └── utils/
│       ├── exceptions/     ← Custom exceptions
│       └── GlobalExceptionHandler.java
├── src/main/resources/
│   ├── application.yml
│   ├── schema.sql
│   └── data.sql

bank-frontend/              ← Angular frontend
├── src/app/
│   ├── core/
│   │   ├── models/         ← TypeScript interfaces
│   │   └── services/       ← HTTP services
│   └── pages/
│       ├── home/                    ← Account list
│       ├── account-overview/        ← Balance + transactions + chart
│       └── transaction-overview/    ← Transaction detail + PDF export
```

---

## Getting Started

### Prerequisites

- Java 17+
- Node.js 18+
- Angular CLI (`npm install -g @angular/cli`)
- Gradle (wrapper included)

### Backend Setup

```bash
# Clone the repo and navigate to backend
cd bank-account

# Run the application
./gradlew bootRun
```

The API will start on `http://localhost:8090`.

H2 Console is available at `http://localhost:8090/h2-console`:
- JDBC URL: `jdbc:h2:mem:bankdb`
- Username: `sa`
- Password: `changeit`

The database is seeded automatically on startup via `schema.sql` and `data.sql`.

#### Key `application.yml` settings

```yaml
server:
  port: 8090

spring:
  datasource:
    url: jdbc:h2:mem:bankdb;DB_CLOSE_DELAY=-1
    username: sa
    password: changeit
  h2:
    console:
      enabled: true
      path: /h2-console
  sql:
    init:
      mode: always
  jpa:
    hibernate:
      ddl-auto: none
    open-in-view: false
```

### Frontend Setup

```bash
cd bank-frontend

# Install dependencies
npm install --legacy-peer-deps

# Start dev server
ng serve
```

The app will be available at `http://localhost:4200`.

---

## API Reference

Base URL: `http://localhost:8090/api/v1`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/accounts` | Get all accounts for current user |
| GET | `/accounts/{accountId}/balance` | Get account balance |
| POST | `/accounts/{accountId}/deposit` | Deposit money |
| POST | `/accounts/{accountId}/withdraw` | Withdraw money |
| POST | `/accounts/{accountId}/exchange` | Exchange currency between accounts |
| GET | `/accounts/{accountId}/transactions` | Get paginated transaction history |
| GET | `/accounts/{accountId}/transactions/{transactionId}` | Get single transaction |
| GET | `/accounts/{accountId}/balance-history` | Get balance time-series for chart |

### Request/Response Examples

#### Deposit
```http
POST /api/v1/accounts/1/deposit
Content-Type: application/json

{
  "amount": 500.00
}
```

```json
{
  "id": 1,
  "currency": "EUR",
  "balance": 3000.0000
}
```

#### Currency Exchange
```http
POST /api/v1/accounts/1/exchange
Content-Type: application/json

{
  "targetAccountId": 2,
  "amount": 100.00
}
```

#### Transaction History (paginated)
```http
GET /api/v1/accounts/1/transactions?page=0&size=20
```

```json
{
  "content": [
    {
      "id": 5,
      "type": "WITHDRAWAL",
      "amount": 200.00,
      "currency": "EUR",
      "balanceAfter": 2800.00,
      "description": "Withdrawal for expenses",
      "createdAt": "2026-01-15T11:30:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 5,
  "totalPages": 1,
  "last": true
}
```

---

## Database Schema

```
users (1) ──< (N) accounts (1) ──< (N) transactions
exchange_rates (standalone lookup table)
```

### Supported Currencies

| ID | Code | Name |
|---|---|---|
| 1 | EUR | Euro |
| 2 | USD | US Dollar |
| 3 | SEK | Swedish Krona |
| 4 | GBP | British Pound |
| 5 | VND | Vietnamese Dong |

### Transaction Types

| ID | Type | Description |
|---|---|---|
| 1 | DEPOSIT | Money added to account |
| 2 | WITHDRAWAL | Money debited from account |
| 3 | EXCHANGE_IN | Incoming leg of a currency exchange |
| 4 | EXCHANGE_OUT | Outgoing leg of a currency exchange |

---

## Features

### Backend
- Deposit and withdraw money from accounts
- Currency exchange between accounts using fixed exchange rates
- Transaction history with pagination
- Balance time-series data for chart rendering
- External system call simulation before every withdrawal (`https://httpstat.us/200`)
- Global exception handling with structured error responses
- Request validation with descriptive error messages

### Frontend
- **Home Page** — lists all accounts with balance and currency
- **Account Overview Page**
  - Current balance display
  - Infinite scroll transaction history (loads 20 at a time)
  - Line chart of historical account balance over time
- **Transaction Overview Page**
  - Full transaction details
  - Export transaction summary as a PDF file
