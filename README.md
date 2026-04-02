# APIVault (Sentinel) - Event-Sourced API Gateway

APIVault is an event-sourced API Gateway designed for security, observability, and forensic replay.

## Project Structure

This project follows a microservices/multi-module architecture containing the following key components:

- **gateway-service**: The core API Gateway module handling incoming requests.
- **event-store-service**: Service responsible for storing and managing the event stream.
- **forensics-replay-service**: A service with query capabilities to replay and analyze API events.
- **policy-engine-service**: Manages and enforces policies dynamically.
- **forensic-dashboard**: The Frontend/UI part of the project.
- **backend-mock-services**: Provides mock backend targets for the gateway to route to.
- **shared**: Shared libraries and common code.

## Getting Started

1. Ensure you have Java 17+ and Maven installed.
2. Build the project from the root directory:
   ```bash
   mvn clean install
   ```
3. Docker Compose can be used to run the associated services:
   ```bash
   docker-compose up -d
   ```
