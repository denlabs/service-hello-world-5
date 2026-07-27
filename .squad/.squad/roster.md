# Team Roster

> AI SDLC squad for requirements, planning, implementation, QA, and documentation.

## Coordinator

| Name | Role | Notes |
|------|------|-------|
| Laurie Bream | Coordinator | Routes work, enforces handoffs and reviewer gates. Does not generate domain artifacts. |

## Members

| Name | Role | Charter | Status |
|------|------|---------|--------|
| Peter Gregory | Requirements Specialist | `.squad/agents/requirements-specialist/charter.md` | ✅ Active |
| Erlich | Planning Specialist | `.squad/agents/planning-specialist/charter.md` | ✅ Active |
| Hendricks | Frontend Specialist | `.squad/agents/frontend-specialist/charter.md` | ✅ Active |
| Gilfoyle | Backend Specialist | `.squad/agents/backend-specialist/charter.md` | ✅ Active |
| Jian-Yang | Fullstack Specialist | `.squad/agents/fullstack-specialist/charter.md` | ✅ Active |
| Bighead | QA Specialist | `.squad/agents/qa-specialist/charter.md` | ✅ Active |
| Jared Dunn | Scribe | `.squad/agents/scribe/charter.md` | 📋 Silent |
| Dinesh Chugtai | Work Monitor | `.squad/agents/dinesh-chugtai/charter.md` | 🔄 Monitor |

## Coding Agent

<!-- copilot-auto-assign: false -->

| Name | Role | Charter | Status |
|------|------|---------|--------|
| @copilot | Coding Agent | — | 🤖 Coding Agent |

### Capabilities

**🟢 Good fit — auto-route when enabled:**
- Bug fixes with clear reproduction steps
- Test coverage (adding missing tests, fixing flaky tests)
- Lint/format fixes and code style cleanup
- Dependency updates and version bumps
- Small isolated features with clear specs
- Boilerplate/scaffolding generation
- Documentation fixes and README updates

**🟡 Needs review — route to @copilot but flag for squad member PR review:**
- Medium features with clear specs and acceptance criteria
- Refactoring with existing test coverage
- API endpoint additions following established patterns
- Migration scripts with well-defined schemas

**🔴 Not suitable — route to squad member instead:**
- Architecture decisions and system design
- Multi-system integration requiring coordination
- Ambiguous requirements needing clarification
- Security-critical changes (auth, encryption, access control)
- Performance-critical paths requiring benchmarking
- Changes requiring cross-team discussion

## Project Context

* **Owner:** Geronimo San Pascual
* **Stack:** TypeScript, Markdown, Squad configuration
* **Description:** Squad governance and operating docs for AI-assisted SDLC workflows
* **Created:** 2026-06-02
