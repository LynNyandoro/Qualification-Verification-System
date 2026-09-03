# Collaboration and Git workflow

## Branching strategy (GitHub Flow + `develop`)

```
main          production-ready, protected, CI must pass
 └── develop  integration branch
      ├── feature/auth-and-roles
      ├── feature/qualification-api
      ├── feature/verification-audit
      └── feature/react-ui
```

- Feature work starts from `develop`.
- Pull requests target `develop`; a release PR promotes `develop` → `main`.
- Commit messages use the imperative mood and explain why a change exists.
- At least one review is required before merge (see the demonstration video).
- `main` is never committed to directly after the first release.

## Merge conflict practice

A documented conflict was resolved on `docs/CONFLICT_LOG.md` by merging two feature branches that both edited the same paragraph. The resolution kept both the hashing description and the audit-trail sentence.

## Pull request template

Title: `feat: <summary>`

Body:

- What changed and why
- How it was tested (`mvn verify`, `npm test`)
- Screenshots for UI
- Linked issue number
