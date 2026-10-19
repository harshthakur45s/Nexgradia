# API Contracts — DRAFT
# Nexgradia — Intelligent Campus Placement & Career Intelligence Platform

> **Status: DRAFT — PENDING APPROVAL**
> No API endpoints, protocols, or schemas are confirmed.
> Backend framework and API style (REST / GraphQL / other) are not yet decided.

---

## Purpose

This document will define the API contracts between Nexgradia's frontend and backend,
including endpoint definitions, request/response schemas, authentication requirements,
and versioning strategy.

It will be populated after:
1. The SRS is approved (functional requirements confirmed)
2. The backend framework is selected (ADR-006)
3. The authentication strategy is decided (ADR-004)
4. The Technical Design Document is approved

---

## API Style

> **[PENDING]** — REST, GraphQL, or hybrid — not yet decided. Requires ADR.

---

## Authentication

> **[PENDING]** — Authentication mechanism not yet decided (ADR-004).
> Possible options: JWT, OAuth 2.0, session-based — all **[PENDING]**.

---

## Versioning Strategy

> **[PENDING]** — API versioning approach not yet decided.

---

## Endpoint Groups (Illustrative — Not Confirmed)

Based on the capstone proposal, the following endpoint groups are anticipated:

| Group | Description | Status |
|---|---|---|
| `/auth` | Registration, login, logout, token refresh | **[PENDING]** |
| `/students` | Student profile management | **[PENDING]** |
| `/recruiters` | Recruiter profile and posting management | **[PENDING]** |
| `/opportunities` | Job / internship listing and search | **[PENDING]** |
| `/applications` | Application submission and tracking | **[PENDING]** |
| `/coordinator` | Coordinator dashboard and reports | **[PENDING]** |
| `/admin` | System administration | **[PENDING]** |
| `/ai` | AI/ML inference endpoints | **[PENDING]** |

> These endpoint groups are illustrative only and are not confirmed requirements.

---

*Status: DRAFT — all API design requires SRS, stack decisions, and Technical Design approval.*
