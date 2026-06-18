# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository status

This repository is in an early state — only `LICENSE` (Apache 2.0), a stub `README.md`, an empty `terraform.yml`, and a Java-flavored `.gitignore` are present. There is no source code, no build system, no tests, and no Cursor/Copilot rules. The single tracked branch is `main`.

When adding content, update this file with concrete build/test commands and architecture notes once those exist.

## Conventions observed in tracked files

- `.gitignore` is the stock Java template (compiled classes, JARs/WARs/EARs, logs, J2ME/Maven cruft). Drop entries that don't apply when the project's actual stack is chosen.
- `terraform.yml` exists at the repo root and is empty — treat its purpose as undefined until the user clarifies whether it is a Terraform plan, a CI workflow, or something else. Do not assume.
- `README.md` is a placeholder (`# claude`). Replace it once there is something to document.
