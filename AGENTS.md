# AGENTS.md — AI Agent Operating Instructions
# Nexgradia — Intelligent Campus Placement & Career Intelligence Platform

> **Scope:** This file contains operating instructions for AI coding agents (e.g., Antigravity/Gemini).
> It is not a general contributor guide. Human contributors should read `README.md` and `docs/project-blueprint/project-charter.md`.

---

## 1. Project Identity

| Field | Value |
|---|---|
| **Formal title** | Nexgradia — Intelligent Campus Placement & Career Intelligence Platform |
| **Tagline** | Your Next Step Starts Here |
| **Working name status** | "Nexgradia" is a working name. Domain and trademark checks are **[PENDING]**. |
| **Current stage** | Step 0.3 — Documentation Repository Setup |
| **Application development** | **NOT YET AUTHORIZED** |
| **GitHub remote** | Exists at `https://github.com/harshthakur45s/Nexgradia.git` — **NOT YET CONNECTED** to local repo |

---

## 2. Approved Facts

Use these facts exactly. Do not paraphrase, expand, or infer beyond what is stated here.

- The project is a campus placement and career intelligence platform.
- The team has **six members**. Individual names and assignments are **[PENDING CONFIRMATION]**.
- The original capstone proposal is `Updated_Capstone_Project_Proposal.pdf` (at workspace root and in `docs/project-blueprint/`).
- The Project Charter, SRS, and Technical Design will each be authoritative within their respective scopes — **once approved**. None are approved yet.
- Technology choices are **not final** until formally approved via the ADR and Technical Design processes.

---

## 3. What Agents Must Never Do

| Prohibition | Reason |
|---|---|
| Modify or overwrite `Updated_Capstone_Project_Proposal.pdf` at the root (read-only) | It is the immutable source document |
| Overwrite `docs/project-blueprint/Updated_Capstone_Project_Proposal.pdf` without explicit instruction | Verified copy of the immutable source |
| Invent team member names, roles, or assignments | Not yet confirmed |
| Invent requirements, features, or acceptance criteria | Not yet approved |
| Invent technical decisions (framework, DB, AI stack, deployment) | Not yet approved through ADR/TDD process |
| Invent test results, completed work, or deployment records | Would falsify project history |
| Create or configure a Git remote | Requires separate explicit approval |
| Run `git commit` | Requires separate explicit approval |
| Run `git push` | Requires separate explicit approval |
| Connect to or interact with `https://github.com/harshthakur45s/Nexgradia.git` | Requires separate explicit approval |
| Write application source code | Application development not yet authorized |
| Install dependencies or configure frameworks/services/databases | Application development not yet authorized |
| Mark any `[PENDING]` item as resolved without explicit human confirmation | Would create false project state |

---

## 4. Documentation Integrity Rules

1. **The proposal is never edited.** Approved updates go in separate documents.
2. **All unresolved items remain explicitly marked `[PENDING]`.** Never silently assume an answer.
3. **`CHANGELOG.md` is append-only.** Never delete or revise existing entries.
4. **This file (`AGENTS.md`) reflects only what is approved** — never aspirations, plans, or guesses.
5. **ADRs record rejected options too.** Do not re-open or re-debate decisions already marked `Accepted`.
6. **Stub documents remain marked `DRAFT — PENDING APPROVAL`** until explicitly approved by the team.

---

## 5. Pending Decisions (as of 2026-10-19)

These items are unresolved. Do not invent answers for any of them.

| # | Decision | Status |
|---|---|---|
| PD-001 | Domain and trademark status of "Nexgradia" | **[PENDING]** |
| PD-002 | Team member names and individual role assignments | **[PENDING]** |
| PD-003 | Git branching strategy (feature branches vs. direct commits to main) | **[PENDING]** |
| PD-004 | Remote platform confirmed and remote connection created | **[PENDING]** |
| PD-005 | Frontend framework selection | **[PENDING]** |
| PD-006 | Backend framework selection | **[PENDING]** |
| PD-007 | Database technology selection | **[PENDING]** |
| PD-008 | AI/ML stack selection | **[PENDING]** |
| PD-009 | Authentication strategy | **[PENDING]** |
| PD-010 | Deployment and hosting strategy | **[PENDING]** |
| PD-011 | Whether earlier capstone proposal version needs to be preserved | **[PENDING]** |
| PD-012 | Whether to rename default branch from `master` to `main` before first push | **Resolved** (`main` adopted) |
| PD-013 | Whether to add `.gitkeep` placeholder to `docs/testing/test-results/` | **Resolved** (added) |

---

## 6. File Modification Permissions

| File / Path | Agent may read? | Agent may edit? | Notes |
|---|---|---|---|
| `Updated_Capstone_Project_Proposal.pdf` (root) | ✅ Yes | ❌ Never | Read-only, immutable source |
| `docs/project-blueprint/Updated_Capstone_Project_Proposal.pdf` | ✅ Yes | ❌ Never | Verified copy — immutable |
| `README.md` | ✅ Yes | ✅ With approval | Update only confirmed facts |
| `AGENTS.md` | ✅ Yes | ✅ With approval | Update only when new decisions are confirmed |
| `TASKS.md` | ✅ Yes | ✅ With approval | Reflect actual task state only |
| `CHANGELOG.md` | ✅ Yes | ✅ Append only | Never delete entries |
| `docs/project-blueprint/*.md` | ✅ Yes | ✅ With approval | Mark drafts clearly |
| `docs/system-design/*.md` | ✅ Yes | ✅ With approval | No confirmed tech decisions yet |
| `docs/decisions/ADR-*.md` | ✅ Yes | ✅ With approval | Follow ADR template; record all options |
| `docs/development-log/` | ✅ Yes | ✅ Append per session | Do not backfill fabricated history |
| `docs/testing/` | ✅ Yes | ✅ With approval | No invented test results |
| Application source code | ✅ Yes | ❌ Not yet | Development not authorized |

---

## 7. When in Doubt

- Stop and ask the user for clarification.
- Do not guess, assume, or proceed with an undecided item.
- Report ambiguities explicitly; do not silently choose one path.

---

*Last updated: 2026-10-19 | Stage: Step 0.3 — Documentation Repository Setup*
