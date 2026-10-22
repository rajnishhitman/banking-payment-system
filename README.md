# Banking Payment System — 3 Microservices

Java 17 + Spring Boot 3.4.5 + PostgreSQL. Runs locally without Docker.

## Services
- `account-service` — port 8081, database `account_db`
- `payment-service` — port 8082, database `payment_db`
- `notification-service` — port 8083, database `notification_db`

All databases are on `localhost:5433`. Default DB username/password are `postgres`/`postgres`; override using `DB_USERNAME` and `DB_PASSWORD` environment variables.

## 1. Create databases
Connect to your PostgreSQL server on port 5433 using pgAdmin or psql and run:

```sql
CREATE DATABASE account_db;
CREATE DATABASE payment_db;
CREATE DATABASE notification_db;
```

If your PostgreSQL username/password are different, set `DB_USERNAME` and `DB_PASSWORD` before starting each service, or edit that service's `application.properties`.

## 2. Open the project in IntelliJ IDEA
1. Extract the ZIP file to a folder.
2. In IntelliJ IDEA, choose **File → Open** and select the top-level `banking-payment-system` folder (the folder containing the root `pom.xml`).
3. If IntelliJ asks whether to open the project as a Maven project, choose **Load** or **Open as Project**.
4. If Maven is not imported, open the Maven tool window and click **Reload All Maven Projects**. Confirm IntelliJ is using a JDK 17 or newer.

The top-level `pom.xml` is an aggregator POM that includes all three services. Each service still has its own `pom.xml` and can be run independently.

## 3. Start services
Open three terminals, from this project directory:

```bash
cd account-service
mvn spring-boot:run
```

```bash
cd payment-service
mvn spring-boot:run
```

```bash
cd notification-service
mvn spring-boot:run
```

Start `account-service` and `notification-service` before `payment-service`.

## 4. Create two accounts
```bash
curl -X POST http://localhost:8081/api/accounts -H "Content-Type: application/json" -d '{"ownerName":"Asha Rao","initialBalance":1000.00}'
curl -X POST http://localhost:8081/api/accounts -H "Content-Type: application/json" -d '{"ownerName":"Ravi Kumar","initialBalance":250.00}'
```
Copy the IDs returned by the responses.

## 5. Transfer money
Replace `1` and `2` with the actual account IDs:
```bash
curl -X POST http://localhost:8082/api/payments/transfer -H "Content-Type: application/json" -d '{"fromAccountId":1,"toAccountId":2,"amount":100.00,"currency":"INR"}'
```

Check balances:
```bash
curl http://localhost:8081/api/accounts/1
curl http://localhost:8081/api/accounts/2
```

Check payment history:
```bash
curl http://localhost:8082/api/payments
```

Check notifications:
```bash
curl http://localhost:8083/api/notifications
```

## Transfer design and learning notes
`payment-service` coordinates a small Saga-style workflow: debit the sender, credit the receiver, then request a notification. If crediting fails after debit, it attempts a compensating refund. Each account operation has an operation ID so a retry of that same operation does not apply it twice. Account balance changes use a database transaction and a pessimistic row lock.

This is an educational local project, not production banking software. A network/process failure between debit and credit can leave a payment `PENDING`; production systems need durable retries/reconciliation, stronger authentication/authorization, audit controls, and robust saga/outbox handling. Notifications are best-effort and do not roll back a successful transfer.


## Java package structure

Each microservice separates HTTP controllers, business services, repositories, entities, and request/response DTOs:

```text
com.example.banking.<service>
├── controller/
├── dto/
├── entity/
├── repository/
├── service/
└── <Service>Application.java
```

The application entry point stays in the root package so Spring Boot scans all subpackages automatically.
