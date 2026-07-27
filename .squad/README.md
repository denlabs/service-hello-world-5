---
title: Tenyks AI SDLC
description: Squad setup and operating guide for the AI-assisted SDLC proof of concept
ms.date: 2026-06-02
ms.topic: overview
---

## Purpose

This repository hosts an AI SDLC squad setup for requirements expansion, backlog planning, implementation, QA, and documentation.

## Squad Members

| Role | Agent |
|------|-------|
| Coordinator | Laurie Bream |
| Requirements Specialist | Peter Gregory |
| Planning Specialist | Erlich |
| Frontend Specialist | Hendricks |
| Backend Specialist | Gilfoyle |
| Fullstack Specialist | Jian-Yang |
| QA Specialist | Bighead |
| Scribe | Jared Dunn |
| Work Monitor | Dinesh Chugtai |

External service agents are defined as Intake (Monica), Prompt Builder (Russ Hannerman), and Watchdog (Compliance).

## Using squad.config.ts

The file squad.config.ts is the source of truth for:

* Team composition
* Specialist routing keys
* Memory settings
* Governance gates
* Plugin wiring

Current governance gates require human review for:

* backlog-finalisation
* compliance-escalation

## How To Operate The Squad

1. Intake and Prompt Builder generate one authoritative feature brief.
2. Route the brief to Requirements Specialist.
3. Route clarified requirements to Planning Specialist.
4. Route UI work to frontend-specialist, service work to backend-specialist, and cross-layer work to fullstack-specialist.
5. Route completed implementation to QA Specialist.
6. If QA fails, route back to development.
7. If QA passes, route to Watchdog compliance review.
8. Scribe records outputs at each major stage.

Detailed routing is defined in .squad/routing.md.

## Initial AI SDLC Artifacts

The initial commit should include these implementation artifacts:

1. src/orchestration/graph.py
2. src/orchestration/routing/guards.py
3. specs/tool-gateway.openapi.yaml
4. generated/tool_gateway_client/
5. Workflow state model for checkpointing and re-runs

These align with the design context in .scartch/main_context and keep orchestration, squad roles, and external tools separated.