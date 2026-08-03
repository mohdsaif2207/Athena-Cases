# Git Workflow (Guide)

Canonical rules: [docs/architecture/10_Git_Strategy.md](../architecture/10_Git_Strategy.md)

```
feature/<owner>-<ticket>-<slug>  →  PR → develop  →  (release) → main
```

- No direct commits to `main` or `develop`
- Squash merge features
- Stay inside owned paths to avoid conflicts
