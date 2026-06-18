# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository status

This repository is in an early state — only `LICENSE` (Apache 2.0), a stub `README.md`, an empty `terraform.yml`, a Java-flavored `.gitignore`, a Spring Boot 3.3 / Java 21 / Maven app under `src/`, and a GitHub Actions pipeline at `.github/workflows/ci.yml` are present. The single tracked branch is `main`.

## Build, test, run

- Build + tests: `mvn -B verify`
- Run locally: `mvn spring-boot:run` (serves on `:8080`)
- Single test: `mvn -B -Dtest=ClassName test`
- Java version: 21 (managed in `pom.xml`)

## CI pipeline (`.github/workflows/ci.yml`)

Four jobs, in dependency order:

1. **build** — `mvn verify`, uploads the JAR artifact. Runs on push and PR.
2. **claude-review** — Anthropic's official action; runs **only on `pull_request`** because the action is PR-only and rejects push events with `Unsupported event type: push`. Requires the `ANTHROPIC_API_KEY` repo secret.
3. **deploy-staging** — `needs: [build, claude-review]`, gated to push on `main`, requires the `staging` environment approval.
4. **deploy-prod** — `needs: [build, claude-review, deploy-staging]`, gated to push on `main`, requires the `production` environment approval.

A skipped `claude-review` (push events) satisfies `needs:`, so deploys proceed normally on direct pushes to main. **There is intentionally no main-branch review job** — adding one would require either the official action (PR-only) or a different invoker (direct API call, OAuth-credentials-file pre-write), which was not worth the complexity at this stage.
