# Demonstration script (10–15 minutes)

Record screen + voice. Suggested sequence:

1. **Opening (1 min)** — Problem: fake certificates. Solution: QVS with Git and CI/CD.
2. **Architecture (1 min)** — React CRA UI, Spring Boot API, PostgreSQL, Docker Compose, GitHub Actions.
3. **Git workflow (2 min)** — Show `git log --oneline --graph --all`, `docs/GIT_WORKFLOW.md`, a merged feature branch, `docs/CONFLICT_LOG.md`, and `docs/ISSUES.md`.
4. **CI/CD (2 min)** — Open `.github/workflows/ci.yml`. If the repo is on GitHub, show a green Actions run, Surefire/JaCoCo artefacts, coverage gate, Docker build job.
5. **Live system (6 min)**
   - Sign in as `issuer` / `Issuer@123`
   - Register a qualification; copy the verification code
   - Sign out, sign in as `verifier` / `Verifier@123`
   - Search by holder name
   - Verify the new code (VALID) and a bogus code (NOT_FOUND)
   - Open Audit history
   - Optional: revoke as issuer, verify again (REVOKED)
6. **Automated tests (2 min)** — `mvn verify` excerpt and `npm run test:ci`.
7. **Close (1 min)** — Quality gates stop a broken build; hash + audit support trust.

Speak to maintainability and why JWT, role separation and immutable audit rows were chosen.
