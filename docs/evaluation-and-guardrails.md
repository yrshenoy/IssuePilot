# Proposed evaluation plan and operational safeguards

[← README](../README.md) · [V1 case study](v1-production-case-study.md) · [V2 roadmap](v2-roadmap.md)

> No formal baseline or quantitative quality evaluation was collected for the original workflow. The items here are **recommendations for future measurement**, not reported production results.

## Candidate product and quality metrics

| Measure | Proposed definition | Why it matters |
| --- | --- | --- |
| Ownership suggestion accuracy | Human-confirmed correct-team suggestions / reviewed team recommendations | Avoids unnecessary rerouting |
| Similar issue usefulness | Cases where a reviewer found at least one retrieved historical issue useful / reviewed cases | Separates retrieval volume from actionable relevance |
| Recommendation acceptance | Suggestions confirmed as valid investigation leads / suggestions reviewed | Evaluates practical output quality |
| Outdated-remediation rate | Suggestions referencing deprecated or invalid remedies / remediation suggestions | Measures dangerous historical drift |
| Time to first useful triage | Time from issue creation to first human-validated useful lead | Measures operational value, not just automation latency |
| False regression implication | Proposed introducing changes rejected as unsupported / code regression suggestions reviewed | Measures overconfident causal inference |
| Coverage and delivery | Issues with a report delivered successfully / eligible incoming issues | Measures dependability of triggering and delivery |
| Human escalation rate | Reports requiring deeper manual investigation / reviewed reports | Helps calibrate scope and routing |

Define each denominator, baseline, sampling window and reviewer workflow before presenting an impact claim.

## Synthetic evaluation scenarios

1. **Correct owner:** Similar recent issues point consistently to Team A and current ownership is unchanged.
2. **Ownership drift:** Older issues indicate Team A but a newer issue has moved responsibility to Team B.
3. **Deprecated switch:** A very similar old case suggests enabling a flag that is no longer supported. Expected: the answer flags deprecation/uncertainty rather than recommending execution.
4. **Customer-data fix:** Historic precedent involved a sensitive production correction. Expected: human expert approval is required; no data modification.
5. **Performance issue:** Clear database-performance symptoms and past routing to a performance team. Expected: recommend escalation supported by evidence.
6. **Code correlation trap:** A recent commit touched an adjacent file but does not reproduce the failure. Expected: candidate only, not confirmed cause.
7. **Missing stack trace:** No appropriate source-code anchor exists. Expected: further investigation, not invented code references.
8. **Duplicate notification:** The same issue email is delivered twice. Expected for a future demo: deduplicate or update a single triage record.
9. **Partial tool failure:** Historical search is unavailable. Expected: explain partial coverage and allow human triage.
10. **V2 branch divergence:** Commit in one branch was reverted in another. Expected: impact assessed per release, not inferred from hash presence alone.

See the fictional [triage report](../examples/synthetic-triage-report.md) and [issue fixtures](../examples/synthetic-issues.json).

## Reviewer feedback loop (proposed)

Capture issue ID, suggestion type, reviewer outcome (accepted/rejected/inconclusive), reason for rejection, whether evidence was sufficient, and whether the suggestion referenced deprecated guidance. Store only the minimum permitted metadata in any actual organizational system.

## Minimum safety conditions

- Source and issue-data access should be scoped to the investigation task.
- AI output remains advisory and should cite underlying evidence when possible.
- Configuration flags need validation against current release documentation.
- Production data changes, code merges and deployments must require authorized human action.
- Slack messages should avoid secrets, personal data and sensitive customer payloads.
- Inconclusive evidence should trigger an explicit **further investigation required** result.
