# Sentinel

Sentinel is a demo-ready zero-trust API gateway with:

- live ABAC enforcement through `gateway-service`
- append-only audit storage in PostgreSQL via `event-store-service`
- replay and what-if analysis through `forensics-replay-service`

The frontend is not part of the critical demo path. The supported demo is API-first.

## Runtime

- `gateway-service` on `8080`
- `event-store-service` on `8081`
- `policy-engine-service` on `8082`
- `forensics-replay-service` on `8083`
- PostgreSQL on `5432`
- mock backends on `9001` to `9003`

Database defaults:

- database: `sentinel_db`
- user: `sentinel`
- password: `sentinel`

## Build

```bash
mvn clean package -DskipTests
```

## Start

```bash
docker-compose up --build
```

## Load demo sessions

```bash
bash fixtures/load_demo_sessions.sh
```

Seeded sessions:

- `11111111-1111-1111-1111-111111111111` normal request flow
- `22222222-2222-2222-2222-222222222222` policy denial
- `33333333-3333-3333-3333-333333333333` flagged and denied attack path

## Core APIs

- `POST /api/events`
- `GET /events/sessions`
- `GET /events/sessions/{sessionId}`
- `POST /policy/evaluate`
- `POST /policy/evaluate-snapshot`
- `GET /admin/policies/snapshots/{snapshotId}`
- `POST /forensics/replay`
- `GET /forensics/query/sessions`
- `GET /forensics/query/sessions/{sessionId}/report`
- `GET /forensics/query/sessions/{sessionId}/verify-hashes`
- `POST /forensics/sessions/{sessionId}/whatif`

## Demo checks

1. Valid JWT to `GET /api/users/profile` should return `200`.
2. Missing or bad JWT to the same path should return `401`.
3. Non-admin `GET /api/admin/dashboard` should return `403`.
4. `DELETE /api/payments/delete` with the wrong role or outside allowed hours should return `403`.
5. `GET /forensics/query/sessions` should list live DB sessions.
6. `POST /forensics/replay` with the seeded attack session should reconstruct stored decisions.
7. `POST /forensics/sessions/{sessionId}/whatif` should show an earlier divergence for stricter policy.

## Note

Older docs in the repo that still mention `apivault`, mock-only replay, or the larger UI are legacy notes and should not be treated as the current startup guide.
