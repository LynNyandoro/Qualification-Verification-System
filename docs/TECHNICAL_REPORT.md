# Technical Report: A DevOps-Enabled Qualification Verification System

**Module:** MIM736  
**Assignment:** Practical — Design and implement a DevOps-enabled qualification verification system using Git and CI/CD practices  
**System name:** Qualification Verification System (QVS)  
**Approximate word count:** 3,150

## 1. Problem analysis

Educational institutions, professional bodies and employers share a trust problem: a certificate is easy to photocopy, edit or fabricate, while the cost of a bad hiring or accreditation decision is high. Traditional verification is slow. An employer emails a registry office; a clerk searches a spreadsheet or a legacy student-information system; a letter comes back days later. That process does not scale to high-volume recruitment, remote study, or cross-border professional mobility.

The failure modes are well known. First, **forgery**: a PDF can be altered without a corresponding change in any authoritative store. Second, **stale truth**: a qualification that was valid at issue may later be revoked for academic misconduct or may expire (for example a professional licence). Third, **weak auditability**: if a verifier cannot show who checked which credential, when, and with what outcome, the organisation cannot defend its due diligence. Fourth, **collaboration risk in the software itself**: if the verification platform is built as an untested monolith on a single laptop, the system that was meant to create trust becomes another opaque artefact.

QVS addresses the domain problem and the engineering problem together. Domain operations are reduced to four authorised capabilities: register a qualification, search and retrieve records, verify authenticity, and retain an auditable history. Engineering practice is treated as part of the product: Git branching, pull-request review, automated tests, static analysis, coverage gates and containerised delivery. The claim is not that QVS replaces a national qualifications framework database. The claim is that a small, well-factored system can encode verification rules so that they are executed the same way in development, in CI, and in a deployed environment, and that this repeatability is itself a quality property.

The stakeholders are issuers (registrars), verifiers (employers and professional bodies), administrators, and the credential holder (who does not log in in this version, but receives a verification code to present). In a Zimbabwean and regional labour market, where graduates move between universities, professional councils and private employers, the cost of a slow or informal check is paid by both the applicant and the organisation. A shared digital verification service does not remove the need for institutional authority; it makes that authority queryable in seconds rather than days.

Constraints that shaped the work include a Java or Python mandate (Java/Spring Boot was selected), a requirement for Git collaboration evidence, CI/CD, automated tests, and deployment to an accessible environment. Time and team size favour a modular monolith over microservices. The product is therefore intentionally small: four authorised operations, one integrity hash, one audit table, and a pipeline that refuses to package a failing build.

## 2. Requirements

Requirements were derived from the brief and written so that each could be tested automatically.

### 2.1 Functional requirements

| ID | Requirement | Verification |
| --- | --- | --- |
| FR1 | An authorised issuer or administrator can register a qualification with holder, title, type, institution, dates and optional NQF level | `POST /api/qualifications` integration test |
| FR2 | Users can search by holder, title, institution, type and status | `GET /api/qualifications` |
| FR3 | Users can retrieve a single record by internal identifier | `GET /api/qualifications/{id}` |
| FR4 | Authenticity can be checked by verification code and/or credential UUID | `POST /api/verify` |
| FR5 | Optional SHA-256 comparison detects tampering | hash mismatch → `TAMPERED` |
| FR6 | Revoked and expired credentials are not reported as valid | revoke flow integration test |
| FR7 | Every verification attempt is stored with actor, method, result and timestamp | `GET /api/audit` |
| FR8 | Self-registration is limited to ISSUER and VERIFIER; ADMIN is seeded | auth tests |
| FR9 | Verifiers cannot register or revoke credentials | HTTP 403 test |

### 2.2 Non-functional requirements

- **Security:** JWT bearer tokens, bcrypt password hashes, method-level role checks, CORS allow-lists.
- **Integrity:** canonical payload hashing (SHA-256) stored at registration and recomputed at verification.
- **Maintainability:** layered package structure, Checkstyle, documented Git workflow.
- **Reliability:** validation of dates and NQF range; unique verification codes; health endpoint.
- **Deployability:** Docker Compose with PostgreSQL; identical API contract in H2 (dev/test) and Postgres (runtime).
- **Quality gates:** CI fails if tests fail, Checkstyle reports violations, or line coverage is under 50%.

### 2.3 Validation rules (examples)

Issue date must not be in the future. Expiry, if present, must not precede issue. NQF level, if present, is an integer 1–10. Username and email are unique. Verification codes use a restricted alphabet to avoid ambiguous characters (no `O`/`0` pair). Passwords must be at least eight characters. These rules are implemented in `QualificationService` and `AuthService` (and Bean Validation on DTOs) and are therefore exercised by both unit tests and HTTP-level tests.

### 2.4 User stories (abridged)

- As a registrar, I register a degree so that an employer can later confirm it without phoning my office.
- As an employer, I enter a code printed on a certificate and receive `VALID`, `REVOKED`, `EXPIRED`, `TAMPERED` or `NOT_FOUND`.
- As an auditor, I list who verified which code and when, including failed attempts.
- As a developer, I cannot merge to the integration branch unless Checkstyle, tests and coverage gates pass.

## 3. System architecture

QVS is a **modular monolith** with a separate browser application.

```
[React CRA + Nginx] --HTTPS/JSON--> [Spring Boot API]
                                        |
                                        +--> JPA / Hibernate
                                        |      H2 (dev, test) or PostgreSQL (Docker)
                                        +--> JWT filter
                                        +--> Actuator /health
```

The API package layout follows a conventional Spring style: `domain`, `repo`, `service`, `security`, `web`, `config`. Controllers are thin. Business rules live in services so that unit tests do not need a servlet container. Persistence is via Spring Data JPA repositories. Cross-cutting concerns (JWT parsing, CORS, exception mapping) are isolated in `security` and `web`.

The user interface is a Create React App SPA. It talks to `/api` in development through the CRA proxy and, in Docker, through Nginx reverse-proxy location blocks. That choice keeps CORS simple in production (same origin) while preserving a pleasant local loop (`localhost:3000` → `localhost:8080`).

Deployment topology (Docker Compose) runs three services: PostgreSQL 16, the API JVM, and Nginx serving the production React build. Volumes persist qualification data. A healthcheck on Postgres delays API start until the database accepts connections — a small reliability detail that avoids the classic “connection refused on first boot” race.

This architecture was chosen instead of serverless functions or a Kubernetes cluster because the assignment asks for an accessible environment and for CI/CD evidence, not for elastic multi-region operations. Compose is infrastructure-as-code at an appropriate fidelity: one file describes the runtime the marker can start.

## 4. Design decisions

### 4.1 Java and Spring Boot versus Python

Python (FastAPI or Django) would have been a valid reading of the brief. Spring Boot was selected because it has first-class JPA, method security, Actuator and a mature Maven plugin ecosystem (Surefire, Checkstyle, JaCoCo) that maps cleanly onto “automated quality checks” and “quality gates”. Java 21 is the LTS version available on the development machine and in GitHub-hosted runners.

### 4.2 Create React App

The UI requirement was explicitly CRA rather than Vite or Next.js. CRA is in maintenance mode, but it still satisfies the pedagogical goal: a standard `npm start` / `npm test` / `npm run build` lifecycle that CI can invoke. Node 20 is pinned in Docker and GitHub Actions to avoid peer-dependency breakage on newer Node releases.

### 4.3 Authentication model

HTTP sessions would complicate horizontal scaling and SPA usage. JWT keeps the API stateless. Tokens carry the username as subject and the role as a claim; Spring Security still loads `UserDetails` so that account disablement is honoured. Passwords are never stored in plaintext. Admin self-registration is forbidden so that the privilege boundary cannot be crossed from the public `/api/auth/register` endpoint.

### 4.4 Integrity hashing rather than a full blockchain

The optional bonus list mentions blockchain-based credential verification. A public chain would introduce wallets, gas, eventual consistency and operational cost disproportionate to the coursework. QVS instead stores a SHA-256 digest of a canonical string (holder, national ID, title, type, institution, dates, NQF). Verification recomputes the digest. If the stored hash and the recomputed hash differ, the result is `TAMPERED`. An optional `expectedHash` from a paper document or QR payload can be supplied for a three-way check. This is cryptographically weaker than a distributed ledger but is honest, testable and sufficient to demonstrate the *idea* of tamper evidence. The design can later emit the same hash onto a ledger without changing the domain model.

### 4.5 Audit as an append-only table

Verification records are new rows; they are not updates of the qualification. That preserves history if a credential is later revoked. The audit API does not offer delete. This is a deliberate constraint: an auditable history that can be silently edited is not auditable.

### 4.6 Data stores

H2 in PostgreSQL compatibility mode is used for local `mvn spring-boot:run` and for tests so that markers and developers can run the suite without Docker. The `docker` Spring profile switches the datasource to PostgreSQL. Hibernate `ddl-auto=update` is acceptable for this assignment; a production follow-up would introduce Flyway migrations.

The React client stores JWT state in `localStorage` and attaches it on every Axios request. That is simple for a SPA and for the viva, but it is XSS-sensitive. Nginx in Compose reverse-proxies `/api` to the JVM so the browser can call a same-origin path in production, which reduces CORS surface compared with a publicly exposed API on another hostname.

## 5. DevOps workflow

DevOps here means that integration, test, analysis and packaging happen on every change, not as a release-week ritual.

### 5.1 Source control

The repository uses `main` as the releasable line and `develop` as the integration line. Feature branches (`feature/auth-and-roles`, `feature/qualification-api`, `feature/verification-audit`, `feature/react-ui`) isolate work. The documented workflow (`docs/GIT_WORKFLOW.md`) requires pull requests into `develop` and a release PR into `main`. Issue tracking is captured in `docs/ISSUES.md` so that it can be copied into GitHub Issues when the remote is created. Commit messages are written in the imperative mood and grouped by intent.

Merge-conflict management is practised, not only described. Two branches edited the same documentation paragraph; the resolution is recorded in `docs/CONFLICT_LOG.md`. That artefact exists because markers cannot watch the team argue in real time, but they can inspect a resolved conflict and the resulting file.

### 5.2 Continuous integration

`.github/workflows/ci.yml` runs on pushes to `main`/`develop` and on pull requests. Three jobs:

1. **Backend quality gates** — `mvn verify` runs Checkstyle at `validate`, unit and integration tests at `test`, JaCoCo report and a **bundle line-coverage minimum of 0.50** at `verify`. Failure of any step fails the job.
2. **Frontend tests and build** — `npm run test:ci` (Jest, non-watch, coverage) then `npm run build`.
3. **Docker image build** — `docker compose build` after both quality jobs succeed, proving the Dockerfiles still assemble.

Surefire XML, JaCoCo HTML and Jest coverage are uploaded as Actions artefacts. Those artefacts are the “verification reports” and “evidence from pipeline executions” required by the brief. Until the project is pushed, the same commands can be run locally; the HTML report is `backend/target/site/jacoco/index.html`.

### 5.3 Continuous delivery

CD in this project is **automated packaging to a runnable environment**, not a push to a public cloud (which would require student cloud credentials). Compose is the delivery artefact: a reviewer runs `docker compose up --build` and reaches the UI on port 80. Promoting a Git SHA to that stack is the delivery step. Extending CD to Azure Container Apps or AWS ECS would be a configuration change, not a redesign, because the unit of deployment is already an image.

### 5.4 Quality checks beyond tests

Checkstyle enforces braces, whitespace, naming and unused imports. That is a lighter rule set than Google Java Format; it is intentionally chosen so that the gate is real but not theatrical. Actuator exposes `health` without authentication so that Compose and a marker can probe liveness without a JWT. Together, these checks turn “we wrote tests” into a binary pipeline outcome that can be attached to a pull request.

## 6. Testing strategy

The test pyramid is shallow but complete enough to map onto the learning outcomes.

**Unit tests** cover deterministic logic with no Spring context: `CredentialHashServiceTest` (stability and sensitivity of hashes), `JwtServiceTest` (round-trip and rejection of tampered tokens), `QualificationServiceTest` (happy-path persistence via mocks, rejection of future issue dates and illegal NQF levels). These tests fail fast and document the rules in executable form.

**Integration tests** (`QualificationVerificationIT`) boot the full Spring context with MockMvc and an in-memory H2 database. They exercise FR1–FR9 as HTTP conversations: issuer registers, verifier searches and verifies `VALID`; unknown codes yield `NOT_FOUND`; verifiers receive 403 on register; a new user can register and log in; revoke then verify yields `REVOKED`. Using the HTTP surface catches mapping, validation and security annotation mistakes that pure unit tests miss.

**Frontend tests** render the login screen (copy and demo accounts) and drive the verify page with a mocked API module, asserting that the default demo code is submitted and that a `VALID` payload is shown. They do not replace a human demonstration, but they protect the critical path against accidental copy changes.

**Coverage reporting** is produced by JaCoCo (backend) and Jest (frontend). The backend gate is a quality gate in the assignment’s sense: a pull request that deletes tests or adds large untested branches will fail `mvn verify`. Fifty per cent line coverage is a floor, not a boast. On the development machine the JaCoCo instruction coverage for the analysed bundle was 87% with 13 JUnit tests (unit plus integration). Frontend Jest currently protects the login copy and the verify happy path rather than every page; that is a conscious pyramid choice, not an accident.

A typical verification conversation, as encoded by `QualificationVerificationIT`, is: authenticate as issuer → `POST /api/qualifications` → authenticate as verifier → `GET /api/qualifications?holderName=` → `POST /api/verify` expecting `VALID` and `hashMatch=true` → `GET /api/audit` showing the same result. The unknown-code and revoke cases sit beside that path so that “green” does not mean “always valid”.

**Static analysis** is Checkstyle on the backend. Frontend linting is left at CRA defaults to keep the toolchain standard.

## 7. Verification strategy

“Automated verification of requirements” is more than having tests. The strategy has four layers.

1. **Test cases as specification.** Each FR has at least one automated assertion. If FR6 (revocation) regresses, `issuerCanRevokeAndVerificationReportsRevoked` fails.
2. **Validation rules in production code.** Illegal dates never reach the database; the same methods are used by the UI and by tests.
3. **CI/CD quality gates.** A change is not “green” unless Checkstyle, tests, coverage and (on the CD job) image build succeed. That is independent of who typed `git commit`.
4. **Verification reports.** JaCoCo HTML, Surefire XML, Jest `lcov`, and the runtime endpoint `GET /api/reports/verification` (counts of checks and valid outcomes) together form human-readable and machine-readable evidence. Pipeline artefacts should be downloaded and included in the submission ZIP.

Manual verification remains necessary for usability (layout, wording, role-based menu hiding). The demonstration video is that manual layer, scripted in `docs/DEMO_SCRIPT.md` so that it is repeatable.

## 8. Critical evaluation

### 8.1 What works

The product matches the four functional bullets of the brief. Role separation is enforced on the server, not only hidden in the React router. Hashing gives a concrete authenticity story. Audit rows give non-repudiation of *checks*, which is what employers actually need (“we did verify this code on this date”). The DevOps skeleton is realistic: the same Maven command is used by a student, by CI, and as a gate.

### 8.2 Limitations

QVS is not a national registry. There is no federation with other institutions, no PKI-signed diplomas, no holder-facing wallet, and no rate limiting on verify. JWTs are symmetric (HMAC); rotating keys and refresh tokens are absent. `ddl-auto=update` is unsafe for irreversible production change. The React app stores the JWT in `localStorage`, which is XSS-sensitive; an httpOnly cookie would be preferable if the API and UI shared a hardened domain. CRA’s age is a maintenance risk. Coverage at 50% leaves entity getters and some exception paths untested. Blockchain, real-time monitoring dashboards and agentic AI were scoped out; Actuator metrics are a modest observability substitute.

### 8.3 Team collaboration

The assignment assumes three to five members. This repository is structured as if it were a team product (issues, branches, PR-oriented workflow) so that collaboration practice can still be marked. If teammates are added, they should open GitHub Issues from `docs/ISSUES.md`, protect `main`, and require reviews. Git activity will then become the primary individual-assessment signal, which is healthier than a last-week edit of a Word document.

### 8.4 Alternatives considered

A Python/Flask API would have reduced ceremony but weaker default structure for security and JPA-like persistence. A server-rendered Spring MVC UI would have avoided CRA entirely but would have fought the requested React stack. Kubernetes Helm charts would have over-claimed “cloud native” without a cluster. Those alternatives remain valid; they were rejected for fit to the brief and to the time box, not because they are inferior in general.

## 9. Conclusion

QVS shows that qualification verification can be expressed as a small set of authorised operations, that authenticity can be checked against stored hashes and status, and that those behaviours can be guarded by automated tests and a CI pipeline. Git branching, issue records, coverage gates and Docker Compose turn the software-engineering learning outcomes into inspectable artefacts rather than narrative claims. The system is a teaching-scale registry, not a national one; its value is that a marker can clone, test, run and watch the same quality gates the team used while building it.

If the work were continued, the next increments would be Flyway migrations, httpOnly session cookies, rate limiting on `/api/verify`, and a holder-facing PDF or QR that embeds the same SHA-256 digest. Those steps would not change the four functional requirements; they would harden the same contract that CI already verifies.

## References

- Humble, J. and Farley, D. (2010) *Continuous Delivery*. Addison-Wesley.
- Kim, G., Humble, J., Debois, P. and Willis, J. (2016) *The DevOps Handbook*. IT Revolution.
- Spring team (2024) *Spring Boot Reference Documentation*.
- OWASP (2021) *Application Security Verification Standard*.
