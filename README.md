# 🏦 BankDash API

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![JWT](https://img.shields.io/badge/JWT-Security-orange.svg)](https://jwt.io/)
[![Docker](https://img.shields.io/badge/Docker-✔-blue.svg)](https://www.docker.com/)
[![Render](https://img.shields.io/badge/Render-Deployed-purple.svg)](https://render.com)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 About the Project

A complete REST API for a modern digital bank, developed as a portfolio project. It manages users, accounts, transactions, cards, loans, insurance, and real-time notifications.

### 🎯 Objective

Demonstrate backend development skills using Java/Spring Boot, following best practices in architecture, security, and performance.

---

## ✨ Features

### 🔐 Authentication
- JWT login
- Refresh token
- Roles (USER/ADMIN)

### 👤 Users
- Registration and management
- Sensitive data encryption (CPF)
- Notification preferences

### 💳 Bank Accounts
- Types: checking, savings, student, salary
- Deposits and withdrawals
- Transaction history
- Pix keys (email, phone, CPF)

### 💸 Transfers
- Via account number
- Via Pix key
- Idempotency
- Transfer reversal

### 💳 Cards
- Debit and credit
- Card number/CVV generation
- Blocking and soft delete
- Spending by category

### 📊 Loans
- Request with installment calculation
- Admin approval/rejection
- Bill generation

### 🛡️ Life Insurance
- Request
- Admin approval/rejection

### 🔔 Notifications
- WebSocket (STOMP)
- Real-time notifications

### 👑 Admin
- Dashboard with statistics
- User CRUD
- Transaction management
- Loan management
- Insurance management
- System settings

---

## 🛠️ Technologies

| Technology | Version | Purpose |
|------------|--------|------------|
| Java | 21 | Main language |
| Spring Boot | 3.2 | Framework |
| Spring Security | 6.x | Auth/AuthZ |
| Spring Data JPA | 3.x | ORM |
| PostgreSQL | 16 | Database |
| WebSocket (STOMP) | - | Real-time |
| JWT | 0.12 | Auth tokens |
| Maven | 3.9 | Build tool |
| Docker | Latest | Containerization |
| JUnit 5 | - | Unit testing |
| Mockito | - | Integration testing |

---

## 🏗️ Project Structure

```
src/main/java/com/witalo/digitalbank/
├── auth/
├── account/
├── transaction/
├── card/
├── loan/
├── insurance/
├── notification/
├── user/
├── admin/
└── common/
```

---

## 🚀 How to Run

### Requirements
- JDK 21
- Docker (optional)
- PostgreSQL 16

### Docker

```bash
git clone https://github.com/witaloxz/digitalbank-backend.git
cd digitalbank-backend
cp .env.example .env
docker-compose up -d
```

---

## 🔐 Main Endpoints

| Method | Endpoint | Description |
|--------|----------|------------|
| POST | /api/v1/auth/login | Login |
| POST | /api/v1/users | Register |
| GET | /api/v1/users/me | Profile |
| GET | /api/v1/accounts/user/{userId} | Account |
| POST | /api/v1/transactions/deposit | Deposit |
| POST | /api/v1/transactions/withdraw | Withdraw |
| POST | /api/v1/transfers | Transfer |
| GET | /api/v1/transactions | Statement |
| GET | /api/v1/cards | Cards |
| POST | /api/v1/loans/account/{accountId}/request | Loan request |

---

## 📚 Swagger

http://localhost:8080/swagger-ui.html  
https://digitalbank-backend.onrender.com/swagger-ui.html  

---

## 🧪 Tests

```bash
./mvnw test
./mvnw verify
./mvnw jacoco:report
```

---

## 🐳 Docker

```bash
docker build -t digitalbank-api .
docker run -p 8080:8080 --env-file .env digitalbank-api
```

---

## 🌐 Deploy (Render)

- Connect repo
- Set environment variables
- Start command:
```
java -Xms128m -Xmx256m -jar app.jar
```

---

## 📄 License

MIT © Witalo Dias Santos
