# IssuePilot — Synthetic POC Evaluation Report

**Evaluation date:** October 31, 2026  
**Scope:** Independently developed Java/Vaadin + Spring Boot/H2 issue simulator and locally invoked Codex `issue-pilot` skill  
**Dataset:** The tested local API responses contained **52 records**, including early hand-created demo issues and additional synthetic cases. The complete local dataset and application source are **not yet committed to this repository**.  
**Source of observations:** Eight human-supplied Codex terminal outputs reviewed qualitatively in conversation, not independently rerun or machine-verified for this report.

> **Critical distinction:** These results apply **only to the independent synthetic POC**, not to the original production-used internal Codex skill. No production performance, business impact, routing accuracy, or time savings were measured. This is a small, hand-selected scenario review, **not an 8/8 accuracy benchmark**.

## Purpose

Evaluate whether a locally invoked Codex skill can use a read-only Issues REST API to:
- Find plausible duplicates among *currently open* issues.
- Retrieve relevant *closed* issues and describe previous resolutions.
- Suggest ownership based on related issues' assigned teams.
- Identify feature changes that are plausible regression candidates, **without asserting causation**.
- Find related automated-test issues.
- Explicitly report insufficient evidence, misleading similarities, obsolete workarounds, and human review requirements.

The skill was called by issue ID (e.g., `$issue-pilot 2402`) and used `GET /api/issues/{id}` and `GET /api/issues`. Findings were displayed in the Codex terminal; the Java app itself did not execute the AI analysis.

## Scenario review — initial baseline

| Incoming ID | Intended scenario | Key observed findings | Qualitative outcome |
| --- | --- | --- | --- |
| **2401** | Open-issue duplicate detection | Ranked #2007 as strongest candidate: same `MISSING_CUSTOMS_FORM` error and domestic-label behavior; #2402, #1002, #1003 also surfaced with cautions | **Pass** |
| **2402** | Suspected feature-induced regression | #2010 shipping refactor as strongest candidate; #2009 as supporting context; avoided deprecated `VALIDATE_CUSTOMS_V1`; called for deployment verification | **Pass** |
| **2403** | Database-performance escalation | #2015 historical DBA resolution (index/query plan), #2026 related performance tests; no unsupported Java-code-fix claim | **Pass** |
| **2404** | Historical application code fix | #2021 refund-rounding fix and changed files; #2025 tests; flagged inconsistent #2039 → #2022 duplicate reference | **Pass** |
| **2405** | Automated-test discovery | #2014 concurrency fix (optimistic locking/retry); #2016 concurrency test; cautioned against blaming files shared with unrelated features | **Pass** |
| **2406** | Insufficient evidence | No supported duplicate, history, regression, test issue, or owning team; requested transaction IDs, logs, ledger traces and reproduction | **Pass** |
| **2407** | Team ownership | Identity Engineering backed by #2029/#2030/#2027/#2028; #2030 previous SSO redirect fix; suggested checking deployment | **Pass** |
| **2408** | Misleading keyword similarity | Rejected carrier API timeout #2036 as a duplicate of PDF export problem; prioritized #2031 reporting/DBA precedent; owner routing remains debatable | **Pass with improvement** |

**Summary:** **Eight of eight selected scenarios were judged to meet their primary qualitative objective.** This statement does **not** establish precision, recall, generalization, production readiness, or reliability over a representative sample.

## Highlights from observed outputs

### Evidence quality

- **2401:** Codex used identical error text and workflow context to prioritize an active possible duplicate, rather than treating all related issues equally.
- **2402:** Codex linked a potentially introducing feature to changed files and functional behavior, yet explicitly noted closure time is not deployment time.
- **2404:** Codex detected a contradictory duplicate pointer rather than assuming an issue marked `DUPLICATE` was valid evidence of an implemented resolution.
- **2406:** Codex refused to fabricate a cause for a loyalty-points issue without relevant history.
- **2408:** Codex disambiguated a *reporting-dashboard PDF timeout* from a *carrier tracking API timeout*, despite overlapping keywords.

### Human review and safety

Across the eight reports, findings were presented as recommendations. Codex explicitly stated that it had **not** examined live code diffs, logs, deployments, execution plans, or test results, and that issue records or assignments were not changed.

The outputs also distinguished the existence of an `AUTOMATED_TEST` *issue record* from proof that corresponding tests exist or have passed in a real repository.

## Limitations

1. **Small, designed-for-demonstration sample:** The eight incoming issues were intentionally selected to exercise recognizable behaviors; this is not a random or representative operational distribution.
2. **Human-supplied outputs:** The evaluation relied on copied Codex responses. The original terminal transcripts, exact skill version/hash, execution environment and issue-dataset snapshot are not preserved here.
3. **Changing local data:** Reported API responses contained 52 issue records, including initial manual records. Future seeded environments may have different contents unless the dataset is fixed.
4. **No automated ground-truth scoring:** Pass/fail is qualitative; not all expected matches were checked against a separately frozen answer key during review.
5. **No code or deployment access:** `filesChanged`, descriptions and resolution notes are issue-record evidence, not independent proof of introduced regressions.
6. **No timing/ROI conclusion:** Command durations reported in terminal output are not a measured triage-time improvement or evidence of reduced resolution time.
7. **Ownership ambiguity:** A historically resolving specialist team may differ from the functional owning team; observed in #2408 and potentially #2403.

## Improvements proposed after baseline

| Priority | Improvement | Rationale |
| --- | --- | --- |
| P1 | Distinguish **primary functional owner** from **specialist escalation team**, with conflict/unknown handling | #2408 favored Database Performance based on an earlier DBA fix, but Reporting Engineering may be the more appropriate initial owner |
| P1 | Add consistent evidence labels to duplicate and regression candidate lists (strong / possible / weak) | Makes prioritization easier to review without inventing numeric confidence |
| P1 | Explicitly validate historical `DUPLICATE` references and reject obsolete remedies | #2404 exposed a misleading duplicate pointer; #2401/#2402 exposed deprecated configuration guidance |
| P2 | Separate likely existing-fix deployment gaps from suspected newly introduced regressions | #2407 and #2405 both found relevant older fixes whose deployment was unknown |
| P2 | Produce a compact first-page triage summary with uncertainty, evidence links, and next actions | Supports morning engineer review |
| P2 | Pin a synthetic database snapshot, skill version, evaluation inputs and expected outputs | Makes reruns and comparisons reproducible |
| P2 | Add repeated trials, adversarial/negative cases, and independent reviewer scoring | Moves beyond qualitative anecdote toward measured POC performance |

## Recommended next evaluation protocol

1. Commit the actual independent Java/Spring Boot/Vaadin project, Codex skill instructions, and sanitized seeded data (do **not** copy any employer-owned assets).
2. Freeze a known dataset and explicit ground truth for each test issue. Include intentionally misleading and no-match cases.
3. Record the skill version and execution settings.
4. Run each issue more than once and capture full structured outputs.
5. Score duplicate candidate precision, correct-team suggestions, supporting citation validity, outdated-fix warnings, and appropriate abstention.
6. Have at least one human reviewer independently score a subset, documenting disagreements.
7. Keep **synthetic POC measurements** separate from historical production experience.

## Provenance and scope

This file documents the **independent, synthetic proof of concept**. The original production-used workflow was a distinct, internal email-triggered Codex skill co-developed with a peer engineering manager and integrated into daily Slack-based issue triage for several months. Its original implementation, data and prompts are **not** published here. The planned release-branch analysis and draft merge-request generation remain **V2 proposals, not delivered original functionality**.

Related: [V1 production case study](v1-production-case-study.md) · [Conceptual architecture](architecture.md) · [Evaluation metrics and safeguards](evaluation-and-guardrails.md) · [Planned V2](v2-roadmap.md).
