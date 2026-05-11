🔐 Event Store Service (Tamper-Proof Audit Log)

A Spring Boot backend service that stores API events in a hash-chained structure to detect tampering.

---

🚀 Features

- 🔗 Hash chaining (blockchain-like integrity)
- ⏱️ Timestamp-based event ordering (nanoseconds)
- 🔍 Chain verification endpoint
- 📊 Event filtering (user, decision, risk, endpoint)
- 🧾 Session timeline tracking
- 🌐 Captures real request metadata (IP, endpoint, HTTP method)

---

🧠 How it works

Each event contains:

- "previousHash"
- "currentHash"

Hash is generated as:

hash = SHA256(previousHash + timestamp + userId + endpoint + method + ip + decision + riskScore)

👉 If ANY field is changed → hash mismatch → chain becomes invalid

---

⚙️ Tech Stack

- Java 17
- Spring Boot 3
- Spring Data JPA
- PostgreSQL (or H2/MySQL configurable)
- Flyway (DB migrations)

---

▶️ How to Run

1. Clone repository

git clone <your-repo-url>
cd apivault

2. Configure database (application.properties)

spring.datasource.url=jdbc:postgresql://localhost:5432/apivault
spring.datasource.username=your_username
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

---

3. Run application

mvn spring-boot:run

---

4. Server runs at

http://localhost:8082

---

📡 API Endpoints

➤ Create Event

POST /events

Body:

{
  "userId": "user1",
  "decision": "ALLOW",
  "riskScore": 0.2
}

---

➤ Verify Chain

GET /events/verify

Response:

- "Chain is VALID ✅"
- "Chain is TAMPERED ❌"

---

➤ Get All Events

GET /events

---

➤ Filter by User

GET /events/user/{userId}

---

➤ Filter by Decision

GET /events/decision/{decision}

---

➤ Filter by Time Range

GET /events/time?start=2026-04-22T00:00:00Z&end=2026-04-23T00:00:00Z

---

➤ High Risk Events

GET /events/risk/high?threshold=0.5

---

➤ Filter by Endpoint

GET /events/endpoint?endpoint=/api/login

---

➤ Session Timeline

GET /events/sessions/{sessionId}/timeline

---

➤ Internal Event Ingestion (Gateway)

POST /events/internal/events

Body:

{
  "eventType": "REQUEST_RECEIVED",
  "endpoint": "/login",
  "httpMethod": "POST",
  "userId": "user1",
  "decision": "ALLOW",
  "riskScore": 0.3
}

---

🧪 Tamper Detection Test

1. Insert events using POST "/events"
2. Manually modify any record in DB
3. Call:

GET /events/verify

👉 Output:

Chain is TAMPERED ❌

---

📂 Project Structure

com.apivault.eventstore
│── EventController.java
│── EventService.java
│── EventRepository.java
│── Event.java
│── HashUtil.java
│── dto/

---

📌 Future Improvements

- 🔐 Digital signature (prevent forged hashes)
- 🚨 Alert system for tampering detection
- 📊 UI dashboard for visualization
- 🌐 API Gateway integration

---

👩‍💻 Author

Your Name