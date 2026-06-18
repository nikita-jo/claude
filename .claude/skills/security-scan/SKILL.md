---
name: security-scan
description: Scans code, dependencies, and secrets for vulnerabilities. Returns a structured pass/fail verdict for CI use.
---

# Security Scan Skill

Run a security scan over the repository and emit a structured verdict the agent can act on.

## When to use

The host (CI pipeline or Claude agent) should invoke this skill whenever a "security scan" stage needs to run — typically on every push and pull request, before the deploy stage.

## What to scan

1. **Secrets in diff** — any file changed in this commit/PR. Look for patterns resembling API keys, tokens, private keys, AWS/GCP/Azure credentials, database connection strings with embedded passwords, `.env` values checked in.
2. **Dependencies** — if `pom.xml` / `package.json` / `requirements.txt` / `go.mod` / `Cargo.toml` is present, check declared packages against known-vulnerability lists. Prefer OSV.dev (no auth, no rate-limit drama) via the `osv-scanner` CLI if available; otherwise query the OSV API directly.
3. **Source code** — review the diff for: SQL injection, command injection, path traversal, insecure deserialization, hardcoded credentials, unsafe `eval`/`exec`, weak crypto (`MD5`, `SHA1` for security purposes, `DES`, `ECB` mode), XXE, SSRF, open redirects, missing authorization checks on new endpoints.
4. **IaC / config** — if `*.tf`, `Dockerfile`, `docker-compose*.yml`, `k8s/*.yaml`, or CI config is in the diff, flag: privileged containers, `latest` tags, `:latest` base images, host network, missing resource limits, public S3 buckets, `0.0.0.0/0` ingress on sensitive ports.

Skip findings outside the diff unless a `scan:full` argument is passed.

## Tooling preference

Try in this order, fall back gracefully:

1. `gitleaks detect --no-banner --source .` — secrets.
2. `osv-scanner --recursive .` — dependency CVEs.
3. `trivy fs --severity HIGH,CRITICAL .` — combined secrets + deps + IaC, if installed.
4. Manual diff review against the patterns above — always run this, even if a tool succeeded; tools miss things.

If none of the tools are installed, install the first one that fits the stack and is available without network, or fall back to manual review and say so in the report.

## Output contract

Write the result to stdout (and, if available, append to `$GITHUB_STEP_SUMMARY`) as a single fenced JSON block, then a human-readable summary. **Always** emit both — the JSON is what the agent will parse.

```json
{
  "verdict": "pass" | "fail",
  "scan_scope": "diff" | "full",
  "findings": [
    {
      "id": "GHSA-xxxx-yyyy-zzzz" | "RULE-NAME-N",
      "severity": "CRITICAL" | "HIGH" | "MEDIUM" | "LOW",
      "category": "secret" | "dependency" | "code" | "iac",
      "file": "path/to/file",
      "line": 42,
      "description": "What the issue is, in one sentence.",
      "remediation": "What to do about it, in one sentence."
    }
  ],
  "tools_used": ["gitleaks", "osv-scanner", "manual"],
  "scanned_at": "2026-06-18T12:00:00Z"
}
```

### Verdict rules

- `fail` if **any** finding has `severity` in `{CRITICAL, HIGH}`.
- `fail` if any `secret` finding exists, regardless of severity.
- `fail` if `category=dependency` and a `CRITICAL` or `HIGH` CVE is present.
- `pass` otherwise. Include LOW/MEDIUM findings in the report but do not fail.

## Exit code

- `0` on `pass`.
- `1` on `fail`.

The host CI step should rely on the exit code to fail the pipeline — do not require the agent to do it.

## Notes for the agent

- Always re-read the file and confirm a finding before reporting it. False positives erode trust faster than missed findings.
- For each finding, cite the exact file:line and quote the smallest possible snippet.
- If a tool is missing, install it if network is available; if not, fall back to manual review and note the gap in `tools_used`.
- Never modify source files. This skill is read-only.
- Do not echo secrets you found — mask all but the last 4 characters and the key type (e.g. `AKIA****ABCD (aws-access-token)`).
