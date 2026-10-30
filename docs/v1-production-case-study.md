# V1 production case study — AI-assisted production issue triage

[← Project README](../README.md) · [Architecture](architecture.md) · [Planned V2](v2-roadmap.md)

> **Publication boundary:** This is a sanitized description of actual professional experience. Identifying employer-specific configurations, exact prompts, internal issue records, real stack traces, and code are deliberately excluded. Do not interpret this document as the original skill or evidence of measured cost savings.

## Executive overview

An engineering team dealing with approximately **40–50 incoming ERP production issues per month** adopted an email-triggered Codex skill to assist initial investigation. The skill was invoked when a new issue-notification email arrived, queried the issue system for details and similar historical issues, and published findings to a team Slack channel. Engineers reviewed the findings during their morning triage routine before making decisions.

The workflow was **used for every incoming issue for several months**. Engineers gave **positive qualitative feedback**, but no rigorous baseline, quantified time savings, routing precision or recommendation acceptance rate was recorded.

## Team and leadership contribution

The workflow was **co-developed with a peer engineering manager and a junior engineer**. This project illustrates cross-role collaboration and introduction of AI-assisted analysis into a repeatable production support process. It would be inaccurate to imply that one person wrote or owned all the implementation or that the skill resolved issues autonomously.

## Problem and goals

**Existing work:** Engineers reviewed new issue descriptions, searched historical cases, identified ownership, checked earlier remedies, and occasionally investigated stack traces and recent code changes.

**Goal:** Supply an evidence-rich first-pass triage report so the engineering team could review suspected ownership, past patterns and likely investigation paths together.

**Explicit non-goal:** Allowing AI to change production data, enable backend settings, reassign tickets, merge fixes, or deploy releases without human approval.

## Production V1 workflow

1. **Email trigger:** A new enterprise issue generated an email notification in the team inbox, which triggered execution of a Codex skill.
2. **Issue context:** The skill retrieved the full issue details from an internal issue-management tool.
3. **Historical search:** It searched for comparable issues and retained a set of relevant historical references.
4. **Ownership recommendation:** It used assigned-team history to recommend the likely responsible team, including a suggested redirect when another team appeared responsible.
5. **Past remediation clues:** For issues plausibly belonging to the team, it looked for prior customer-data corrections, database/performance-team escalations, and backend configuration-switch fixes.
6. **Code investigation:** Where prior issues did not provide a useful resolution, it examined available stack traces and relevant recent code changes, presenting a suspected regression and a possible introducing change when supported by evidence.
7. **Slack report:** It published the triage findings, uncertainty and relevant references to a Slack channel.
8. **Human review:** Engineers reviewed the output as part of their morning issue-triage process, validating, rejecting or escalating recommendations before taking action.

The public [triage decision diagram](architecture.md#triage-recommendation-logic) is a conceptual representation of this flow, not a reproduction of internal orchestration details.

## Observed limitations and operational lessons

| Observed issue | Why it mattered | Learning / improvement opportunity |
| --- | --- | --- |
| Older cases referenced deprecated backend switches | Historical similarity is not proof a fix still applies | Explicit recency and current-configuration validation before suggesting an action |
| Customer production data problems required expert judgment | A historical data fix could be dangerous or context-specific | Keep recommendations advisory; require authorized, domain-expert review |
| Team-routing hints came from historical assignments | Similarity can be misleading or reflect outdated ownership | Retain supporting cases and surface uncertainty instead of automatic reassignment |
| Stack traces and code changes are correlational clues | A recent edit may not be the cause | Cite relevant evidence and frame changes as candidates until verified |
| No robust quality measurements were collected | Qualitative feedback cannot establish precision or productivity gain | Define evaluation baselines, reviewer feedback, and calibration before making quantitative claims |

## Outcomes — responsibly stated

**Observed:** Sustained production usage over several months; processing approximately 40–50 incoming issues monthly; integration into morning Slack review; positive individual feedback from engineers.

**Not measured:** Mean time to triage, hours saved, code-regression identification accuracy, ownership prediction precision, incident resolution time, or SLA improvements attributable to the skill.

**Not delivered in V1:** Automated patch generation, release impact matrices, draft merge requests, autonomous production remediation or deployment.

## Leadership interpretation

The work demonstrates identifying a repetitive operational workflow, collaborating across roles to introduce AI assistance, placing recommendations where engineers already work, and deliberately retaining human authority for production decisions. A public portfolio should describe these contributions without overstating individual ownership or measurable outcomes.

## Retrospective priorities

- Capture reviewer accept/reject feedback and reasons for override.
- Rank historical issues using recency, ownership quality and verified current guidance.
- Check remediation ideas against current configuration and deprecation data.
- Add regression test cases for misleading but superficially similar historical issues.
- Extend to affected-release analysis and draft-fix proposals only with appropriate human gates; see [V2 roadmap](v2-roadmap.md).
