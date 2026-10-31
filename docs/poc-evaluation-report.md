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

1. **Completed:** Commit the independent Java/Spring Boot/Vaadin project, Codex skill instructions, and sanitized seeded data (without employer-owned assets).
2. Freeze a known dataset and explicit ground truth for each test issue. Include intentionally misleading and no-match cases.
3. Record the skill version and execution settings.
4. Run each issue more than once and capture full structured outputs.
5. Score duplicate candidate precision, correct-team suggestions, supporting citation validity, outdated-fix warnings, and appropriate abstention.
6. Have at least one human reviewer independently score a subset, documenting disagreements.
7. Keep **synthetic POC measurements** separate from historical production experience.

## Revised V1 skill — complete eight-scenario regression review (October 31, 2026)

Following the initial baseline, the independent POC skill was revised to add explicit checks for obsolete fixes and contradictory duplicate links, evidence-ranked matches, deployment uncertainty, and functional-owner versus specialist-team routing. These refinements are **skill instructions**, not new integrations or autonomous capabilities.

**Method:** The user ran `$issue-pilot <id>` locally on the revised skill and pasted each generated report for a qualitative review. The revised local API returned **48 records**, matching the committed synthetic SQL fixture. The earlier baseline responses described **52 records**, because the original local environment included four additional manually created demo issues. These are **not fully controlled, identical-corpus trials**. Raw machine-readable outputs, run configurations and independent grading are not retained here.

| ID | Retest objective and observed evidence | Manual outcome |
| --- | --- | --- |
| **2401** | Ranked #2007 as strongest domestic-customs duplicate; #2402 related; rejected removed `VALIDATE_CUSTOMS_V1`; found #2010/#2009 and missing domestic test coverage in #2011 | **Pass** |
| **2402** | Identified #2010 shared-validator refactor as strongest potential regression; #2009 supporting; flagged missing exact error code in candidate comparison, obsolete flag and deployment uncertainty | **Pass** |
| **2403** | Retrieved DBA precedent #2015 and performance-test record #2026; suggested **Database Performance** as primary with Inventory Engineering consultation, grounded in slow SQL symptoms | **Pass** |
| **2404** | Retrieved historical refund rounding fix #2021 and automated-test record #2025; found contradictory #2039 → #2022 duplicate reference and excluded CLOSED #2039 from active duplicates | **Pass** |
| **2405** | Retrieved optimistic-locking fix #2014 and concurrency-test record #2016; rejected unrelated inventory issues and did not infer causation from shared filenames | **Pass** |
| **2406** | Found no supported loyalty-point duplicate, past fix, regression, test issue, or owner; explicitly rejected routing to Billing Engineering based on “credits” alone | **Pass** |
| **2407** | Recommended **Identity Engineering** using #2030/#2028/#2029; identified plausible #2029 duplicate and prior callback redirect fix #2030; advised deployment check | **Pass** |
| **2408** | Rejected carrier API timeout #2036 as false match; found reporting precedent #2031; recommended **Reporting Engineering** as functional owner and Database Performance as collaborator | **Pass** |

**Result:** **8/8 designed scenarios provisionally passed their intended qualitative review**, including both cases that exercised revised ownership routing (#2403 and #2408). This is **not** a verified model accuracy, pass-rate estimate for real-world issues, or evidence of productivity savings. One sample per scenario, cherry-picked scenario coverage and reviewer subjectivity constrain generalization.

### Before-and-after observations

- **#2408 ownership:** The baseline suggested Database Performance as initial owner. The revision suggested Reporting Engineering as functional owner with Database Performance support. This is a better fit for a reporting export whose bottleneck has not yet been localized.
- **#2403 ownership:** The revision still suggested Database Performance as initial owner when the incoming issue specifically described degraded SQL performance, with Inventory Engineering involved. This avoids a blanket “functional team always owns” rule.
- **Obsolete and contradictory history:** Revised outputs for #2401/#2402 continued to reject the removed customs flag; #2404 explicitly challenged the misleading duplicate pointer.
- **Appropriate abstention:** Revised #2406 did not infer a loyalty points owner from terminology alone.
- **Consistent uncertainty:** Reports noted that code, deployment, logs and executable tests had not been examined.

### Remaining limitations and follow-up

- The revised skill was **not evaluated in controlled repeated trials** or on independent real issue datasets.
- The historical comparison used **different corpus sizes (52 vs 48)**, so it cannot isolate the skill change as the sole cause of differences.
- The skill's report format lists seven sections while Codex sometimes produces eight by splitting ownership and next steps; this is a formatting inconsistency, not a substantive triage failure.
- Next: preserve exact sanitized outputs and skill commit SHA; automate evidence-based checks; run repeated and adversarial tests; independently grade a sample.
- The Spring Boot integration tests verify seeded data/API behavior; they do **not** test Codex's inference accuracy.

**Code status:** The Java/Vaadin POC, local skill, SQL fixtures and integration tests are committed in this public repository. The original production skill remains private and separate. Proposed V2 branch/fix/merge-request workflows remain unimplemented.

## Provenance and scope

This file documents the **independent, synthetic proof of concept**. The original production-used workflow was a distinct, internal email-triggered Codex skill co-developed with a peer engineering manager and integrated into daily Slack-based issue triage for several months. Its original implementation, data and prompts are **not** published here. The planned release-branch analysis and draft merge-request generation remain **V2 proposals, not delivered original functionality**.

Related: [V1 production case study](v1-production-case-study.md) · [Conceptual architecture](architecture.md) · [Evaluation metrics and safeguards](evaluation-and-guardrails.md) · [Planned V2](v2-roadmap.md).
