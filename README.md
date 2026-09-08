# ⚡ RateMaster — API Gateway & Rate Limiting Engine

A Spring Boot API Gateway that demonstrates API key authentication, Redis-backed rate limiting, Kafka-based audit logging, and configurable traffic policies for downstream microservices.

RateMaster sits in front of backend services, authenticates incoming requests, applies endpoint-specific rate limits, forwards allowed requests, and asynchronously publishes audit events. The project is inspired by gateway patterns commonly used in distributed backend systems.

---

## 🛠️ Tech Stack

**Backend:** Java 21, Spring Boot, Spring Security, Spring WebFlux (`WebClient`)

**Data & Messaging:** Redis, PostgreSQL, Apache Kafka

**Tools:** Docker Compose, Maven, Lombok

---

## ✨ Features

* API key authentication with **FREE**, **PRO**, and **ENTERPRISE** subscription tiers.
* Three rate-limiting algorithms:

  * Fixed Window
  * Sliding Window
  * Token Bucket
* Redis-backed atomic counters for concurrent request handling.
* Configurable per-endpoint rate limits stored in PostgreSQL.
* Asynchronous Kafka audit logging for allowed and blocked requests.
* Request proxying to downstream Spring Boot microservices using `WebClient`.
* Metrics APIs exposing traffic, blocked requests, and top API consumers.

---

## 🏗️ Architecture

```text
                 Client / Postman
                        │
                X-API-Key Header
                        │
                        ▼
              +----------------------+
              |      RateMaster      |
              |----------------------|
              | API Key Filter       |
              | Rate Limiter Service |
              | Rule Engine          |
              | Kafka Audit Producer |
              | Gateway Controller   |
              +----------------------+
                        │
                        ▼
         FinStream Banking Microservice
```

### Request Flow

1. Validate the incoming API key.
2. Identify the client tier (FREE / PRO / ENTERPRISE).
3. Load endpoint-specific rate limits from PostgreSQL.
4. Check request eligibility using Redis.
5. Forward allowed requests to the downstream service.
6. Publish audit events asynchronously through Kafka.

---

## ⚙️ Rate Limiting Algorithms

| Algorithm          | Description                                                       |
| ------------------ | ----------------------------------------------------------------- |
| **Fixed Window**   | Counts requests within a fixed time window.                       |
| **Sliding Window** | Uses request timestamps for smoother rate limiting.               |
| **Token Bucket**   | Allows controlled bursts while enforcing an average request rate. |

Redis atomic operations are used to safely update counters during concurrent requests.

---

## 🎯 Rule Engine

Rate limits are configurable per endpoint and per subscription tier.

Example:

| Endpoint            | FREE  | PRO    | ENTERPRISE |
| ------------------- | ----- | ------ | ---------- |
| `/api/transactions` | 5/min | 50/min | 500/min    |
| `/auth/login`       | 3/min | 10/min | 100/min    |

Rules are stored in PostgreSQL and can be updated through REST APIs without modifying application code.

---

## 📨 Kafka Audit Pipeline

Every request generates an audit event.

* Allowed requests are published to Kafka.
* Blocked requests are also published.
* A Kafka consumer persists audit logs into PostgreSQL.
* Audit logging is decoupled from the request processing flow.

This demonstrates an event-driven architecture where auditing is handled asynchronously.

---

## 📊 Metrics

The application exposes runtime metrics including:

* Total requests processed.
* Allowed vs blocked requests.
* Block rate percentage.
* Client-wise traffic statistics.
* Top API consumers.

These endpoints help monitor gateway traffic without querying Redis or Kafka directly.

---

## 🚀 Getting Started

### Prerequisites

* Java 21
* PostgreSQL
* Redis
* Docker Desktop
* Maven

### Run Locally

```bash
git clone https://github.com/Vaishnavi-0309/RateMaster.git
cd RateMaster

docker-compose up -d
mvn spring-boot:run
```

Configure PostgreSQL and Redis credentials in `application.properties` before starting the application.

---

## 📡 Key REST APIs

| Endpoint                    | Purpose                                    |
| --------------------------- | ------------------------------------------ |
| `POST /auth/register`       | Register a client and generate an API key. |
| `POST /auth/regenerate-key` | Generate a new API key.                    |
| `ANY /gateway/**`           | Gateway endpoint for downstream APIs.      |
| `POST /rules`               | Create endpoint-specific rate-limit rules. |
| `GET /rules`                | Retrieve configured rules.                 |
| `PUT /rules/{id}`           | Update existing rules.                     |
| `DELETE /rules/{id}`        | Remove rules.                              |
| `GET /metrics/summary`      | Gateway traffic summary.                   |

---

## 🔍 How Request Processing Works

```text
Incoming Request
        │
        ▼
Validate API Key
        │
        ▼
Identify Client Tier
        │
        ▼
Load Endpoint Rule (PostgreSQL)
        │
        ▼
Rate Limit Check (Redis)
        │
   ┌────┴─────┐
   │          │
Allowed     Blocked
   │          │
Forward      Return HTTP 429
   │          │
Kafka Audit Event
        │
        ▼
PostgreSQL Audit Logs
```

---

## 🧪 Sample Response Headers

Successful gateway responses include rate-limit metadata.

```http
X-RateLimit-Remaining: 8
X-RateLimit-Reset: 60
```

Requests exceeding the configured limit return **HTTP 429 (Too Many Requests)**.

---

## 📚 Backend Concepts Demonstrated

* Spring Security Filter Chain
* API Key Authentication
* Redis Atomic Operations
* Fixed Window, Sliding Window & Token Bucket algorithms
* Event-Driven Architecture with Kafka
* Request Proxying using Spring WebClient
* PostgreSQL-backed Rule Engine
* Global Exception Handling
* Docker-based local development

---

## 💡 Project Highlights

* Designed a reusable API Gateway that separates authentication, rate limiting, auditing, and downstream routing responsibilities.
* Implemented configurable traffic policies using Redis and PostgreSQL.
* Demonstrated asynchronous event publishing with Kafka for audit logging.
* Built the project using Spring Boot microservice design principles suitable for distributed backend systems.

---

## 👩‍💻 Author

**Vaishnavi Udipi**

Java Backend Engineer • Spring Boot • Distributed Systems • Kafka • Redis
