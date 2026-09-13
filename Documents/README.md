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

| Username | Password | Role | Notes |
| --- | --- | --- | --- |
| `student` | `Student@123` | Student (alumni, MSU) | Amina Chikomo · ID `MISM-MSU-0001` · code `QVS-DEMO12345` |
| `graduating` | `Graduating@123` | Student (finishing, NUST) | Tawanda Ncube · no award yet |
| `freshman` | `Freshman@123` | Student (new, UZ) | Rudo Moyo · no award yet |
| Extra students | `Student@123` | Student | Usernames `msu001`–`msu099`, `nust000`–`nust099`, `uz000`–`uz099` |
| `issuer` | `Issuer@123` | MSU registrar | |
| `nust` / `uz` | `Nust@123` / `Uz@123` | NUST / UZ registrars | |
| `econet` / `cbz` / `delta` | `Econet@123` / `Cbz@123` / `Delta@123` | Employer verifiers | |
| `verifier` | `Verifier@123` | Demo employer | Same verify role as the three companies |
| `admin` | `Admin@123` | Operator | Directory of every user, campus and student |

For assessment convenience, demo accounts are listed in this README, but the login/dashboard UI does not display the full sample account list as on-screen copy.

Seeded volume: **3 universities** (UZ, NUST, MSU) with **100 students each**, each with a **unique full name**. Most alumni hold a unique programme **credential ID** (for example `MISM-MSU-0001`) and a separate **verification code** (`QVS-…`). A sample of credentials is marked **revoked** or **expired**. Startup also writes about **80 mixed verification events** so Audit, Dashboard stats and AI insights are populated. **3 employers** (Econet, CBZ, Delta). Students copy either identifier; employers paste it in Search or Verify.

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

Open [http://localhost:3000](http://localhost:3000). The first screen is **sign in** (`/` and `/login`). The CRA dev server proxies `/api` to `http://localhost:8080`. After login the app opens `/dashboard`. Protected routes send guests back to `/login`.

If `mvn spring-boot:run` fails with `Value not permitted for column ... "STUDENT"`, stop any other Java process on port 8080, then run again. The API now converts the old H2 `ROLE` enum to `VARCHAR` on startup. To reset demo data completely:

```bash
rm -f backend/data/qvs.mv.db backend/data/qvs.trace.db
cd backend && mvn spring-boot:run
```

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

UI: [http://localhost](http://localhost) (login first) · API: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

If your existing PostgreSQL volume was created from an older schema where qualification text fields were persisted as `bytea`, the API now auto-aligns those columns to `varchar` on startup before search queries run. Unfiltered catalog listing no longer uses `LOWER(CONCAT(...))` against Postgres, so Search and Dashboard stay populated. If startup still fails because the old volume is corrupted or incompatible, recreate it:

```bash
docker compose down -v
docker compose up --build
```

### Optional LLM assist for agentic insights

The `/api/ai/verification-insights` endpoint and the **AI insights** page always compute counts from the audit table. Narrative text is **rule-based** unless a key is set. Prefer Groq (OpenAI-compatible). Put the key in a gitignored `.env` at the repo root so Docker Compose can inject it:

```bash
GROQ_API_KEY=<your_groq_key>
```

Default Groq model: `openai/gpt-oss-20b` (`QVS_AI_GROQ_MODEL` to override). OpenAI still works with:

```bash
QVS_AI_OPENAI_API_KEY=<your_api_key>
QVS_AI_MODEL=gpt-4o-mini
```

Do not commit API keys. If Groq is configured, the page mode is `LLM_ASSISTED`; otherwise `RULE_BASED`. Verify results stay deterministic either way.

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
5. Agentic AI analytics — `POST /api/ai/verification-insights` (risk patterns, confidence score, trend vs previous window, recommendations, method/result breakdowns)

Evidence for Git, CI, testing and evaluation lives under `docs/`.
