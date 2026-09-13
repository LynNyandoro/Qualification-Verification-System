# QVS test scripts

Automated tests for the Qualification Verification System (MIM736).  
Last run: **13 September 2026**.

## Latest results

| Suite | Command | Result |
| --- | --- | --- |
| Backend | `cd backend && mvn -B verify` | **BUILD SUCCESS** — 26 tests, 0 failures, 0 errors, 0 skipped. Checkstyle 0 violations. JaCoCo line-coverage gate (≥ 50%) **met** (bundle instruction coverage **80%**, lines **1,258 / 1,540**). |
| Frontend | `cd frontend && npm run test:ci` | **PASS** — 3 test suites, **9 tests**, 0 failures. Statement coverage **24.58%** (no frontend coverage gate). |
| CI | `.github/workflows/ci.yml` | Same commands on GitHub Actions for `main` / `develop` / pull requests. |

HTML coverage: `backend/target/site/jacoco/index.html`  
Frontend `lcov`: `frontend/coverage/lcov-report/index.html`

---

## How to run

### Backend (JUnit 5, MockMvc, Checkstyle, JaCoCo)

```bash
cd backend
mvn verify
```

Faster (tests only, no coverage gate):

```bash
cd backend
mvn test
```

Single class:

```bash
cd backend
mvn -Dtest=QualificationVerificationIT test
```

Requires **Java 21** and Maven. Tests use in-memory H2 (`backend/src/test/resources/application.yml`). They do not need Docker or PostgreSQL.

### Frontend (Jest, React Testing Library)

```bash
cd frontend
npm install --legacy-peer-deps
npm run test:ci
```

Interactive:

```bash
cd frontend
npm test
```

Requires **Node 20** (CI pins Node 20).

---

## Backend scripts (`backend/src/test/java`)

### Unit

| Class | Tests | What it proves |
| --- | --- | --- |
| `CredentialHashServiceTest` | `hashIsDeterministicForSamePayload`, `hashChangesWhenTitleChanges`, `sha256Produces64HexCharacters` | SHA-256 of the canonical credential payload is stable and tamper-sensitive. |
| `JwtServiceTest` | `roundTripUsername`, `tamperedTokenIsRejected` | JWT encode/decode; altered tokens are rejected. |
| `QualificationServiceTest` | `registerPersistsHashedCredential`, `futureIssueDateIsRejected`, `invalidNqfLevelIsRejected` | Register writes a hash; future issue dates and illegal NQF levels are rejected. |
| `AgenticInsightsServiceTest` | `returnsRuleBasedInsightsWithoutLlmKey`, `reportsNoDataWhenAuditTrailIsEmpty` | Insights are `RULE_BASED` without an LLM key; empty audit trail is handled. |
| `H2SchemaAlignerTest` | `convertsLegacyRoleEnumSoStudentCanBeInserted` | Legacy H2 `ROLE` enum is widened to `VARCHAR`. |
| `PostgresSchemaAlignerTest` | `skipsWhenDatasourceIsNotPostgres`, `convertsOnlyByteaColumns` | Postgres aligner no-ops on H2; `bytea` text columns are converted. |

### Integration (`QualificationVerificationIT`)

HTTP-level scripts against the full Spring context:

| Test method | What it proves |
| --- | --- |
| `demoCatalogFillsDirectorySearchAndAuditScreens` | Seeded qualifications, students, and audit rows are visible. |
| `seededStudentsHaveUniqueFullNames` | Every seeded student full name is unique. |
| `issuerCanRegisterSearchAndVerifierCanConfirmAuthenticity` | Issuer registers; verifier searches and verifies `VALID`. |
| `unknownCodeIsNotFoundAndAudited` | Unknown code → `NOT_FOUND`. |
| `verifierCannotRegisterQualification` | Verifier gets HTTP 403 on register. |
| `registerAndLoginWorkForNewVerifier` | Public self-registration and login. |
| `issuerCanRevokeAndVerificationReportsRevoked` | Revoke then verify → `REVOKED`. |
| `authorizedUserCanVerifyByCandidateName` | Verify by candidate name. |
| `studentCanViewOwnCredentialButCannotRegister` | Student can read own record, cannot register. |
| `verifierCanSearchByHolderNameOrQualificationCode` | Search by name or code. |
| `issuerSeesOwnInstitutionStudents` | Issuer student list is campus-scoped. |
| `adminCanOpenDirectoryAndStudentRecord` | Admin directory and student detail. |
| `verifierCanRequestAgenticInsights` | `POST /api/ai/verification-insights` returns analysis fields. |

---

## Frontend scripts (`frontend/src`)

| File | Tests | What it proves |
| --- | --- | --- |
| `App.test.js` | Root and `/login` show sign-in; guest `/dashboard` redirects to login; login copy does not list demo passwords; About/Contact link; candidate-name search for verifiers; dark/light theme toggle | Routing and unauthenticated first page. |
| `VerifyPage.test.js` | `verify page posts a code and shows a valid outcome` | Verify UI posts a code and shows a valid result (mocked API). |
| `AgentInsightsPage.test.js` | `agent insights page requests analysis and renders summary` | Insights page calls `/ai/verification-insights` and renders the summary. |

---

## Mapping to assignment operations

| Requirement | Covered by |
| --- | --- |
| Register qualifications | `QualificationServiceTest`, `issuerCanRegisterSearchAndVerifierCanConfirmAuthenticity` |
| Search / retrieve | `verifierCanSearchByHolderNameOrQualificationCode`, `authorizedUserCanVerifyByCandidateName` |
| Verify authenticity | `issuerCanRegisterSearchAndVerifierCanConfirmAuthenticity`, `unknownCodeIsNotFoundAndAudited`, `VerifyPage.test.js` |
| Audit history | `demoCatalogFillsDirectorySearchAndAuditScreens`, revoke/unknown-code tests |
| Agentic insights | `AgenticInsightsServiceTest`, `verifierCanRequestAgenticInsights`, `AgentInsightsPage.test.js` |
| Security / roles | `verifierCannotRegisterQualification`, `studentCanViewOwnCredentialButCannotRegister`, `JwtServiceTest` |

---

## Manual demo (not automated)

Live walkthrough (login, search, verify, audit, AI insights): `docs/DEMO_SCRIPT.md` on `origin/main`.
