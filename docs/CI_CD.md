# CI/CD Guide

This project uses GitHub Actions for CI/CD with four workflows:

- `ci.yml`: build and test on every push and pull request
- `security.yml`: dependency review and CodeQL checks
- `qodana_code_quality.yml`: JetBrains Qodana static analysis
- `docker-release.yml`: build, scan, and publish Docker images to GHCR
- `deploy.yml`: manual deployment to `staging` or `production`

## Pipeline Flow

1. Open a pull request -> `CI`, `Security`, and `Qodana Code Quality` run automatically.
2. Merge to `main` -> `CI`, `Security`, `Qodana Code Quality`, and `Docker Release` run.
3. Run `Deploy` workflow manually and choose `staging` or `production`.

## Required Secrets

Configure these in GitHub repository settings.

### For Docker publish

- No extra secret required when publishing to GHCR in same repository. The workflow uses `GITHUB_TOKEN`.

### For deployment (`deploy.yml`)

- `DEPLOY_HOST`: target server hostname or IP.
- `DEPLOY_USER`: SSH username.
- `DEPLOY_SSH_KEY`: private key for SSH (prefer deploy key).
- `GHCR_USERNAME`: account that can pull from GHCR.
- `GHCR_TOKEN`: token with `read:packages`.
- `DEPLOY_KNOWN_HOSTS` (optional): static known hosts entry to avoid keyscan at runtime.

## Required Environment Configuration

Create GitHub Environments:

- `staging`
- `production`

Recommended settings:

- Require reviewers before deployment.
- Add environment variable `APP_PORT` if remote host should expose a different port than `8080`.

## Docker Image Tags

Published image:

- `ghcr.io/<owner>/<repo>:latest` (only default branch)
- `ghcr.io/<owner>/<repo>:sha-<commit_sha>`
- `ghcr.io/<owner>/<repo>:v*` (when pushing git tags)

## Local Validation Before Push

Run these commands locally before opening PR:

```bash
mvn -B -ntp clean verify
docker build -t smart-pc-store:local .
```

## Notes

- Trivy in `docker-release.yml` fails the workflow on HIGH/CRITICAL vulnerabilities.
- CodeQL and Qodana reports are attached to workflow results.
- Dependabot is enabled in `.github/dependabot.yml` for Maven and GitHub Actions updates.
