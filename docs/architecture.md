# Architecture — conceptual reconstruction of V1

[← README](../README.md) · [V1 case study](v1-production-case-study.md) · [V2 roadmap](v2-roadmap.md)

> The production system was a **Codex skill triggered by issue-notification email**. These diagrams describe its externally explainable workflow without disclosing the actual employer's infrastructure, source code or prompt chain. Design details marked **recommended** below are proposed safeguards, not claims about what was implemented.

## Event-driven workflow

```mermaid
flowchart LR
    A["New ERP production issue"] --> B["Issue notification email"]
    B --> C["Trigger Codex triage skill"]
    C --> D["Check active issues for possible duplicates"]
    C --> E["Search historical issues for ownership and past resolutions"]
    C --> F["Inspect stack traces and code history if needed"]
    D --> G["Evidence-based triage findings"]
    E --> G
    F --> G
    G --> H["Publish summary to team Slack channel"]
    H --> I["Morning engineering review"]
    I --> J["Human decides next action"]
```

[Editable Mermaid source](diagrams/01-v1-event-pipeline.mmd)

The notification email acted as the **trigger**, not the content authority for all investigation. The Codex skill queried the issue-management system for the issue details, **still-open issues that might be duplicates**, and related historical cases. The team's **morning Slack review was the human decision point**, not the time at which automation necessarily ran.

## Triage recommendation logic

```mermaid
flowchart TD
    A["Receive issue and load details"] --> AA["Search still-open issues for possible duplicates"]
    AA --> AB{"Potential duplicate?"}
    AB -->|Yes| AC["Record candidate issue links for human verification"]
    AB -->|No| B["Retrieve and retain similar historical issues"]
    AC --> B
    B --> C{"Most likely owning team?"}
    C -->|Other team| D["Recommend reassignment with historical evidence"]
    C -->|Our team| E{"Historical remediation pattern?"}
    C -->|Uncertain| F["Flag ownership for human investigation"]
    E -->|Customer data correction| G["Recommend expert review of data-fix possibility"]
    E -->|Database performance| H["Recommend performance-team investigation"]
    E -->|Backend configuration| I["Suggest historical switch for human validation"]
    E -->|Unclear| J{"Stack trace available?"}
    J -->|Yes| K["Inspect relevant recent code changes"]
    J -->|No| L["Further investigation required"]
    K --> M{"Credible code regression candidate?"}
    M -->|Yes| N["Report suspected change and supporting evidence"]
    M -->|No| L
    D --> O["Publish advisory findings to Slack"]
    F --> O
    G --> O
    H --> O
    I --> O
    L --> O
    N --> O
    O --> P["Human reviews and decides"]
```

[Editable Mermaid source](diagrams/02-v1-triage-decision.mmd)

The skill also checked for potential duplicates among **live, unresolved issues**. That is distinct from searching **historical issues** to identify previously tried resolutions and likely team ownership. Any duplicate candidates were advisory and subject to engineer validation. A further task was to decide whether there was enough historical evidence to recommend a likely owning team. If the issue appeared to belong to the team, the skill looked for known remediation patterns. If those were inconclusive, it attempted stack-trace and code-history investigation. No step authorized executing a fix automatically.

## Interaction sequence

```mermaid
sequenceDiagram
    participant Issue as Issue management system
    participant Mail as Notification inbox
    participant Trigger as Email trigger
    participant Codex as Codex triage skill
    participant History as Issue search
    participant Repo as Source repository
    participant Slack as Team Slack channel
    actor Engineer as Reviewing engineer
    Issue->>Mail: New issue notification
    Mail->>Trigger: New qualifying email
    Trigger->>Codex: Invoke skill for incoming issue
    Codex->>Issue: Fetch issue details
    Issue-->>Codex: Description and available stack trace
    Codex->>Issue: Search still-open issues for potential duplicates
    Issue-->>Codex: Candidate active issue links and context
    Codex->>History: Search similar past issues
    History-->>Codex: Related issues and ownership history
    opt Historical evidence is insufficient
        Codex->>Repo: Inspect code history relevant to stack trace
        Repo-->>Codex: Candidate changes and references
    end
    Codex->>Slack: Post duplicate candidates, triage summary and evidence
    Engineer->>Slack: Review during morning triage
    Note over Engineer,Slack: Engineers validate recommendations before actions
```

[Editable Mermaid source](diagrams/03-v1-interaction-sequence.mmd)

## Conceptual system boundaries

| Boundary | Purpose | Important limitation |
| --- | --- | --- |
| Notification inbox | Signals that a new issue exists | Email is a trigger, not necessarily the authoritative issue record |
| Issue management system | Original issue details, still-open issue duplicate candidates, historical issues, assigned team | Duplicate similarity is not proof of equivalence; retrieved content can be incomplete |
| Codex skill | Orchestrates investigation and creates an advisory report | Does not have authority to change production systems |
| Source repository | Optional stack-trace and recent-change analysis | A nearby commit is not proof of causation |
| Slack | Makes findings visible to the reviewing team | Human decision process is separate from automated posting |

## Recommended next-step improvements (not claims of original implementation)

1. Deduplicate repeated **notification emails** using a stable issue identity and preserve traceability. This is a separate concern from V1's search for **potentially duplicate open issues**.
2. Include separate lists of **candidate open-issue duplicates** and **historical resolution references**, with links, recommendation confidence categories and source recency.
3. Validate flag or switch existence and deprecation before suggesting investigation paths.
4. Handle partial retrieval or code-search failure with explicit uncertainty.
5. Log review feedback without copying sensitive customer or incident content into downstream analytics.
6. Keep secret-bearing payloads out of Slack and restrict access to issue data and source code.

For the separate proposed V2 architecture, see [the roadmap](v2-roadmap.md).
