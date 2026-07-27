---
title: Squad Decisions
description: Initial decisions and next artifacts for the tenyks AI SDLC setup
ms.date: 2026-06-02
ms.topic: reference
---

## Active Decisions

1. Orchestration layer is a LangGraph state machine.
2. Squad handles requirements, planning, development, QA, and documentation.
3. Intake, prompt builder, and compliance watchdog stay outside the squad as service agents.
4. Jira and Confluence integration must go through a task-oriented OpenAPI gateway.

## Initial Commit Additions Needed

1. Add orchestration skeleton at src/orchestration/graph.py.
2. Add guard and transition rules at src/orchestration/routing/guards.py.
3. Add contract at specs/tool-gateway.openapi.yaml.
4. Add generated client target folder generated/tool_gateway_client/.
5. Add workflow state model for checkpointing and stage re-runs.
6. Add one end-of-flow human approval checkpoint.

## Governance

* Human review is required for backlog-finalisation.
* Human review is required for compliance-escalation.
* Keep traceability from Confluence source to Jira work items.
