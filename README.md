# 🛒 E-commerce Microservices System

## 🚀 Overview
This is a **microservices-based e-commerce backend system** built using Spring Boot.  
It demonstrates real-world backend architecture including service communication, authentication, and distributed system design.

---

## 🧱 Architecture

- **API Gateway** → Central entry point & security
- **Auth Service** → JWT-based authentication
- **Product Service** → Product management
- **Order Service** → Order lifecycle management
- **Payment Service** → Handles payments
- **Eureka Server** → Service discovery

---

## 🔗 Communication

- Order → Product (Feign Client)
- Order → Payment (Feign Client)
- Gateway → All services

---

## 🔐 Security

- JWT Authentication at API Gateway
- Role-based access control
- Header propagation (X-User-Id)
- No direct client-service communication

---

## 🛒 Business Logic

- Order lifecycle:
  - PENDING_PAYMENT → CONFIRMED / CANCELLED
- Payment handled via separate microservice
- Stock reduced **after successful payment**
- Ownership validation implemented

---

## ⚙️ Tech Stack

- Java 17
- Spring Boot
- Spring Cloud (Eureka, Gateway, OpenFeign)
- MySQL (per service database)
- Maven

---

## ▶️ How to Run

1. Start **Eureka Server**
2. Start all services:
   - Auth Service
   - Product Service
   - Order Service
   - Payment Service
3. Start **API Gateway**
4. Use Postman to test APIs

---

## 🎯 Key Highlights

- Microservices architecture
- Inter-service communication using Feign
- Centralized security using Gateway
- Proper error handling & resilience
- Real-world order-payment flow

---

## 📌 Future Improvements

- Dockerization
- Kafka (event-driven architecture)
- Distributed tracing & logging
- Rate limiting

---

## 👨‍💻 Author

Prashant
