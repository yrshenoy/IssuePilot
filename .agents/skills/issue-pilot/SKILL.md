---
name: issue-pilot
description: Analyze an incoming IssuePilot ERP issue using the local Issues REST API. Identify potential duplicate open issues, historical resolutions, and possible regression candidates.
---

# IssuePilot — AI-Assisted Issue Triage

You are assisting an engineer with production issue triage.

## Input

The user provides an IssuePilot issue ID.

Example: `$issue-pilot 1002`

## Data source

The local IssuePilot Java application exposes:

- GET http://localhost:8080/api/issues/{id}
- GET http://localhost:8080/api/issues

Use these endpoints to retrieve issue information.

Do not modify any issue records.

## Investigation workflow

### 1. Retrieve the incoming issue

Fetch the requested issue by ID.

If the issue does not exist or the API is unavailable,
report the problem and stop.

### 2. Identify potential duplicates

Search other issues with status OPEN.

Compare issue subjects and descriptions.

Exclude the incoming issue itself.

Report likely duplicates with supporting evidence.

Do not assume similar issues are confirmed duplicates.

### 3. Search historical defects

Find relevant CLOSED issues with type DEFECT.

Analyze:
- Subject and description
- Resolution type
- Resolution notes
- Files changed

Explain how historical resolutions might help.

### 4. Investigate possible regressions

Search recently closed FEATURE issues.

Compare:
- Feature description
- Resolution notes
- Files changed
- Creation and closure dates
- Functionality described by the incoming defect

Identify features that might have introduced
changes relevant to the new problem.

File-name similarity alone is insufficient to
confirm a regression.

### 5. Identify relevant automated tests

Search issues with type AUTOMATED_TEST.

Identify tests that may cover the affected behavior.

### 6. Generate a triage report

Output the following sections:

1. Incoming issue summary
2. Potential duplicate open issues
3. Similar historical defects and resolutions
4. Potential regression-introducing features
5. Relevant automated tests
6. Recommended owning engineering team (with supporting issue IDs)
7. Recommended investigation steps
8. Limitations and uncertainties

Include issue IDs as references.

Do not invent issue data, commits, code changes,
test results, or resolution outcomes.

All recommendations require human review.
Never change production systems or issue records.


### Recommend owning engineering team

Use the assignedTeam field from related issues
to identify the most likely owning engineering team.

Consider:
- Similarity between issue descriptions
- Assigned teams of relevant historical issues
- Assigned teams of potentially duplicate open issues
- Whether the evidence is consistent or conflicting

Recommend the most likely engineering team.

Include the issue IDs supporting the recommendation.

If the evidence is insufficient or conflicting,
state that ownership requires human investigation.

Do not automatically change an issue's assigned team.
