---
title: Squad Routing
description: Routing table and handoff rules for the tenyks SDLC squad
ms.date: 2026-06-02
ms.topic: reference
---

## Routing Table

| Workflow Stage | Route To | Output |
|----------------|----------|--------|
| Requirements expansion | Peter Gregory | Clarified requirements, assumptions, acceptance criteria, missing info |
| Backlog creation | Erlich | Epic, stories, implementation tasks, QA tasks, traceability links |
| Development | Hendricks (frontend), Gilfoyle (backend), Yang (fullstack) | Code changes, initial tests, implementation notes |
| QA validation | Bighead | Test cases, pass or fail summary, defects, rework recommendations |
| Documentation | Jared Dunn | Session notes, summary artifacts, decision updates |

## External Routing

| Workflow Stage | Route To | Output |
|----------------|----------|--------|
| Intake | Monica | Source requirements normalized into feature request object |
| Prompt building | Russ Hannerman | Authoritative feature brief payload |
| Compliance review | Watchdog | Compliance evidence and Jira issue or comment linkage |

## Handoff Rules

1. Intake always runs before requirements expansion.
2. Prompt builder must produce a single feature brief before squad routing begins.
3. QA fail routes back to Development.
4. QA pass routes to Watchdog for compliance review.
5. Human review is required for backlog-finalisation and compliance-escalation.
6. Scribe runs after each major stage and updates project records.

## Assignment Rules

1. Coordinator routes requirements work only to Peter Gregory.
2. Coordinator routes backlog design only to Erlich.
3. Coordinator routes UI tasks to frontend-specialist, service and data tasks to backend-specialist, and cross-layer tasks to fullstack-specialist.
4. Bighead is the gate for pass or fail from development.
5. Jared Dunn documents decisions, evidence links, and final handoff notes.
