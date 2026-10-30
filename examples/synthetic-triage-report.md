# Fictional triage report — for demonstration only

> Every identifier, team, switch, branch, and observation below is invented. This is not an extract from the former employer's issue tool, codebase or Slack channel.

**Issue:** `DEMO-1420` — Error during invoice preview in a fictional ERP  
**Trigger:** New issue email received  
**Status:** AI-generated suggestions awaiting engineer review

## Similar historical issues

| Fictional issue | Similarity clue | Human-review note |
| --- | --- | --- |
| `DEMO-0931` | Similar stack-trace component | Older issue; resolution may no longer apply |
| `DEMO-1220` | Same invoice-preview path | More recent; candidate team owner: Documents |
| `DEMO-1388` | Related configuration behavior | Flag reference appears deprecated |

## Suggested ownership

**Documents Engineering** — tentative. Recent historical ownership supports this more strongly than the oldest related issue. An engineer must validate current routing.

## Resolution patterns

An older issue used a fictional switch called `LEGACY_PREVIEW_MODE`. **Do not enable it:** the synthetic repository marks it deprecated. No recommendation is made to modify customer data automatically.

## Code investigation

The fictional stack trace points to `PreviewRenderer.render()`. A code change in a mock release branch is a **candidate** contributing factor, not confirmed root cause. Reproduction and branch-specific checks are required.

## Recommended human actions

- Verify the owning team with current team boundaries.
- Check whether the issue reproduces in the affected release.
- Do not apply the historical deprecated switch.
- Investigate the candidate code path; escalate if evidence remains insufficient.

## Planned V2 output — not produced by V1

A future version might attach an affected-release matrix and a link to a **draft** merge request generated in an isolated workspace. Neither capability was implemented in the original production skill.
