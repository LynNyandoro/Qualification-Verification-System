# Qualification Verification System (QVS)

DevOps-enabled system for **MIM736 Practical Assignment (August 2026)**. Authorised users can register qualifications, search records, verify authenticity, and inspect an auditable history of verification activity.

## Stack

| Layer | Choice |
| --- | --- |
| API | Java 21, Spring Boot 3.3, Spring Security (JWT), JPA |
| UI | React 18, Create React App (`react-scripts` 5) |
| Data | H2 (local) / PostgreSQL (Docker) |
| CI/CD | GitHub Actions |
| Quality | JUnit 5, MockMvc, JaCoCo (≥ 50% line coverage), Checkstyle, Jest |
| Deploy | Docker Compose (API + Nginx UI + PostgreSQL) |

## Demo accounts

| Username | Password | Role |
| --- | --- | --- |
| `admin` | `Admin@123` | Administrator |
| `issuer` | `Issuer@123` | Issuing institution |
| `verifier` | `Verifier@123` | Employer / verifier |

Seeded credential: **QVS-DEMO12345** (Amina Chikomo, Master of Information Systems Management).

## Local run (development)

Terminal 1:

```bash
cd backend
mvn spring-boot:run
```

Terminal 2:

```bash
cd frontend
npm install --legacy-peer-deps
npm start
```

Open [http://localhost:3000](http://localhost:3000). The CRA dev server proxies `/api` to `http://localhost:8080`.

## Tests and quality gates

```bash
cd backend && mvn verify          # Checkstyle + unit/integration tests + JaCoCo
cd frontend && npm run test:ci    # Jest + coverage
```

Coverage HTML: `backend/target/site/jacoco/index.html`.

## Docker deployment

```bash
docker compose up --build
```

UI: [http://localhost](http://localhost) · API: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Repository layout

```
backend/     Spring Boot API
frontend/    Create React App
docs/        Report, contribution statement, Git workflow, issues
.github/     CI/CD workflow
```

## Assignment mapping

1. Register qualifications — `POST /api/qualifications` (ISSUER, ADMIN)
2. Search/retrieve — `GET /api/qualifications`
3. Verify authenticity — `POST /api/verify` (code, credential ID, optional SHA-256)
4. Audit history — `GET /api/audit`

Evidence for Git, CI, testing and evaluation lives under `docs/`.
