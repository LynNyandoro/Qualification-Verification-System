# Individual contribution report

**Student:** Lynn Yandoro  
**Module:** MIM736  
**Artefact:** Qualification Verification System (QVS)  
**Word count:** approximately 720

## Role and contributions

I acted as full-stack engineer and DevOps lead for this practical. My work covers the Spring Boot API (domain model, JWT security, qualification lifecycle, hash-based verification, audit persistence), the Create React App interface, Docker Compose packaging, GitHub Actions quality gates, and the technical documentation.

Concrete deliverables:

- Domain model for users, qualifications and verification records, including validation rules (dates, NQF band, unique verification codes).
- SHA-256 canonical hashing so authenticity checks detect payload tampering.
- Role-based access: issuers register and revoke; verifiers search and verify; administrators have full access.
- Automated tests (`CredentialHashServiceTest`, `QualificationServiceTest`, `JwtServiceTest`, `QualificationVerificationIT`) and Jest UI tests.
- CI pipeline that fails the build on Checkstyle violations or JaCoCo coverage below 50%, then builds Docker images.
- Technical report, Git workflow description, issue backlog and this contribution statement.

Git evidence is the commit graph on `main`/`develop`/`feature/*`. After the project is pushed to GitHub, attach screenshots of Actions runs, pull requests and Issues to the printed submission pack.

## Git activity (local evidence)

Use these commands when compiling the appendix:

```bash
git shortlog -sn
git log --author="Lynn" --stat
git log --oneline --graph --all
```

Paste the output of `git shortlog -sn` and two representative pull-request URLs (once the remote exists) as Appendix A.

## Lessons learnt

1. **Pipelines make quality non-negotiable.** Encoding Checkstyle and coverage as Maven `verify` goals means a green build is evidence, not a claim.
2. **Canonical hashing is simpler than a full blockchain** for a coursework system, and still gives a defensible integrity story that can be extended later.
3. **Role design leaks into the UI.** Hiding the register screen from verifiers avoids 403 errors and matches the security model.
4. **CRA on modern Node needs `--legacy-peer-deps`.** Pinning Node 20 in Docker and CI avoided the Node 23 peer-dependency friction seen on the development machine.

## Challenges

- Balancing academic report length (3,000–4,000 words) with a working product in one iteration.
- Achieving a JaCoCo gate without writing brittle tests that only exist to inflate coverage.
- Demonstrating pull requests and issue tracking before a GitHub remote is configured; the local branch history and `docs/ISSUES.md` are the offline equivalent, to be mirrored on GitHub for marking.
- Merge-conflict practice required a deliberate overlapping edit; the resolution is recorded in `docs/CONFLICT_LOG.md`.

The demonstration video is my responsibility as well: I will follow `docs/DEMO_SCRIPT.md`, show the branch graph, a CI (or local `mvn verify`) run, and a live register–search–verify–audit path using the seeded code `QVS-DEMO12345` and a newly registered credential.

If this submission is assessed as a team artefact, remaining members should add their own one-page reports with their `git log --author` evidence. This file records my individual contribution only.
