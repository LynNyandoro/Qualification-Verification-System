# Submission pack (MIM736)

Hand in this repository plus the written artefacts below. The marker can clone, run tests, and start Docker without extra explanation.

## Files to include in the ZIP / LMS upload

1. This Git repository (or a GitHub URL with `main` and `develop`).
2. `docs/TECHNICAL_REPORT.md` (3,000–4,000 words) — convert to PDF/Word if the LMS requires it.
3. `docs/INDIVIDUAL_CONTRIBUTION.md` — one copy per student; other teammates add their own.
4. Demonstration video (10–15 minutes) following `docs/DEMO_SCRIPT.md`.
5. Pipeline evidence: GitHub Actions run URL, or local screenshots of `mvn verify` and `npm run test:ci`, plus `backend/target/site/jacoco/index.html`.

## After you push to GitHub

```bash
git remote add origin <your-github-url>
git push -u origin main
git push origin develop
```

Recreate Issues from `docs/ISSUES.md`. Open at least one pull request (`feature/*` → `develop`) so the Git deliverable includes PR evidence.

## Local quality evidence (already run during development)

- Backend: Checkstyle 0 violations, 13 tests, JaCoCo line-coverage gate (≥ 50%) passed.
- Frontend: Jest suites for login and verify pages passed.
