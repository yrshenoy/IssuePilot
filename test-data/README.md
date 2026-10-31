# IssuePilot synthetic seed data and evaluation cases

This pack contains **48 entirely fictional issues**: 40 reference issues (`2001`–`2040`) and 8 open incoming evaluation issues (`2401`–`2408`). Your existing local sample issues (`1001`–`1004`) are not overwritten. The SQL targets the agreed 11-column `ISSUES` table.

## Files

- `issuepilot_seed_h2.sql` — H2 SQL `MERGE` statements. Running it again updates the same 48 IDs rather than adding duplicates.
- `evaluation_expectations.csv` — expected relevant issue references, plausible ownership, and important false conclusions to avoid. Expectations are **review guidance**, not a claim that the skill is already accurate.

## Load in your existing H2 console

1. Make sure the IssuePilot application is running locally and your H2 console is connected to the existing database (`jdbc:h2:file:./data/issuepilot`).
2. Copy the entire contents of `issuepilot_seed_h2.sql` into the SQL editor and press **Run**. Alternatively, run it from IntelliJ's SQL console.
3. Verify with:

```sql
SELECT COUNT(*) AS SEEDED_COUNT FROM ISSUES WHERE ID BETWEEN 2001 AND 2040 OR ID BETWEEN 2401 AND 2408;
SELECT ID, SUBJECT, ISSUE_TYPE, STATUS, ASSIGNED_TEAM FROM ISSUES WHERE ID BETWEEN 2401 AND 2408 ORDER BY ID;
```

Expected seeded count: **48**. Your total database count may be higher because of existing sample issues.

**Warning about identity IDs:** This seed assigns explicit high numeric IDs. If you later add issues from Java/JPA, make sure the H2 identity generator does not attempt to reuse them. For this read-only POC, that is not a concern.

## Run the skill

With the application still running:

```
$issue-pilot 2401
$issue-pilot 2402
$issue-pilot 2403
$issue-pilot 2404
$issue-pilot 2405
$issue-pilot 2406
$issue-pilot 2407
$issue-pilot 2408
```

Check each against `evaluation_expectations.csv` using these criteria:

- **Relevant references:** Did it find plausible issue IDs, distinguishing live duplicate candidates from closed examples?
- **Ownership:** Did it select a plausible team with explicit issue evidence or say ownership is uncertain?
- **Regression hypotheses:** Did it connect related completed feature issues and file descriptions *without claiming a proven regression*?
- **Safe abstention:** Did it avoid fabricated findings on the insufficient-evidence case?
- **Disambiguation:** Did it avoid linking merely on a shared word such as *tracking*?

An appropriate evaluation score is a manual rubric (e.g., supported / partially supported / unsupported per dimension), not a numerical claim of production accuracy. Retain Codex outputs so you can iteratively improve prompts.

## POC scope and data caveats

- The dataset is fictional and independent of any former employer's internal issues or code.
- `files_changed` records **historical issue fix/change filenames**. It does not prove which commit caused a new regression.
- Issue `closed_at` is a proxy for time, not a release/deployment timestamp.
- `assigned_team` on new evaluation targets is intentionally NULL, so the skill can recommend ownership.
- Do not treat a potential duplicate as a confirmed duplicate without human review.
- The synthetic issue corpus is intentionally small. A skill that fetches all issues can inspect it directly; a larger system would need server-side retrieval and ranking.
