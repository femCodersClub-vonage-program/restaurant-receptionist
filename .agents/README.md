# Review profiles

This directory contains portable, read-only review profiles for Restaurant Receptionist. They help contributors and reviewers apply the same project rules consistently before a pull request is merged.

These Markdown files are not GitHub Actions checks and do not replace human review, required CI checks, or the project mentor's approval. When requesting an AI-assisted review, explicitly provide the relevant profile and the pull request, issue, or changed-file context. The reviewer must report evidence and must not edit the branch being reviewed.

## Profiles

| Profile | Apply when |
| --- | --- |
| [docs-reviewer](docs-reviewer.md) | Any pull request: checks issue and spec scope, documentation impact, repository language, and data-safety concerns. |
| [ui-reviewer](ui-reviewer.md) | A pull request changes a user-facing frontend flow, screen, component, or interaction. It reviews functional UI and proportional UX. |
| [accessibility-reviewer](accessibility-reviewer.md) | A pull request changes user-facing frontend markup, controls, forms, or interactive states. |
| [design-system-reviewer](design-system-reviewer.md) | A pull request changes frontend styles, tokens, shared components, or visual variants. |

Apply only the profiles relevant to the pull request. A frontend pull request normally uses all four profiles; a backend-only or documentation-only pull request normally uses `docs-reviewer` only.

## Reporting findings

Use the following severity levels:

- **Blocking:** must be corrected before merge.
- **Important:** should be corrected before merge, unless the mentor records a reasoned exception in the pull request.
- **Suggestion:** useful improvement that does not block the pull request.

Every finding should include an evidence-based explanation and `file:line` when available. A review that could not verify a behavior must say so rather than infer a successful result.
