# Project coordination workflow

## Purpose and activation

Keep approved MVP work available to the three participants, review pull requests with evidence, and tell the project mentor what needs her decision. This document defines how a coordinator acts; it does not monitor GitHub or start itself. An event-triggered task starts a coordination cycle when pull request activity occurs. A scheduled task may run the same cycle as a backup and provide a daily summary.

Every report and other communication addressed to Gabriela must be in Spanish. Repository content, including issues and pull requests, must be in English.

## Sources and authority

Before acting, consult `AGENTS.md`, `CONTRIBUTING.md`, the MVP specification, business rules, the relevant specs and ADRs, issue and pull request templates, the current GitHub Project, open issues, and pull requests. The current participant roster and GitHub usernames are in the README's Team section. Apply the applicable read-only review profiles in `.agents/`. Approved mentor decisions and repository rules take precedence over this workflow.

Distinguish verified GitHub activity from recommendations, assumptions, and decisions still needed. An issue marked *In progress* does not by itself prove that its assignee is actively working. Do not infer progress that is not observable.

Verify each issue's assignees from its current GitHub issue detail before reporting that it is unassigned. A search result or list that omits assignee data is not evidence that the issue has no assignee. If issue details cannot be fetched, report the assignment as unverified rather than absent.

## Coordination cycle for every pull request event

On each relevant pull request opening, update, review, or merge:

1. Identify the event and refresh the current state of the pull request, linked issue and spec, checks, open issues, assignments, blockers, and Project. Use current state rather than relying only on the event payload.
2. Review an open pull request when it is ready for review or has changed since the last review. After a merge, verify the merge and recalculate what it unblocks.
3. Check **all three participants'** queues, not only the pull request author's. Count only open assigned issues. Aim for two or three per participant: one current issue and the next issues as planned work.
4. If a queue needs replenishing, first use suitable existing approved issues. Create a new issue only when needed, authorized, and supported by approved scope and any required published spec. Assign the suitable next issue, including a blocked successor when appropriate.
5. Tell Gabriela in Spanish what was verified, what action was completed, and what remains for her decision. If no issue needs creating or assigning, say so briefly. Do not wait for the next morning's scheduled run to address a queue shortage found during this cycle.

Do not repeat an assignment or create a duplicate issue. Before any write, refresh the relevant issue and assignees so a second task run does not undo or duplicate another run's work. If an action fails, report it as incomplete.

For a manual run without a triggering pull request, inspect the current open pull requests and review those awaiting mentor review or changed since their last review. Then check all three assignment queues and perform the same replenishment steps if needed. Report which pull requests were checked; do not treat a missing event payload as a reason to skip the review.

## Assignment rules

- Assign related work in dependency order. Participants may work across backend, frontend, and documentation over time, but do not require an area switch after every issue.
- A participant works on one issue at a time. Future issues may already be assigned while blocked; assigned does not mean ready to start. Do not mark a blocked issue as ready or in progress merely because it is assigned.
- After a merge, if the next issue is already assigned to the author, report that it is unblocked rather than assigning it again. If two or three suitable open issues are already assigned, do not add more merely to create activity.
- If no suitable approved work exists, alert Gabriela with the missing decision or spec. Do not invent scope to meet the assignment target.
- Respect the mentor's ownership of priorities and the Project's status conventions. Do not silently change an issue's scope or owner to balance counts.

## Creating issues and documentation

Keep work inside the approved MVP. Check for an existing issue or implemented solution before creating one. Use the repository issue template, relevant labels, verifiable acceptance criteria, expected validation, and accurate dependencies. Link the issue and its required spec in both directions, updating the spec header after GitHub assigns the issue number.

When a required spec or product decision is missing, prepare a concrete proposal for Gabriela. Do not publish an implementation issue as ready until its scope is approved and its required spec is published. Follow repository permissions for creating branches, commits, and documentation changes; authority to create issues is not authority to publish repository changes. Avoid duplicate dependencies, code, and software libraries, speculative features, and unnecessary complexity.

If a participant proposes a change outside an issue's scope, a new dependency, or a significant technical decision, ask that the proposal and reason be recorded in the relevant issue before implementation. The later pull request must explain the agreed decision, changes, and validation. Escalate decisions that require mentor approval.

## Pull request review

Compare each pull request with its issue, spec, repository rules, changed files, validation evidence, and current CI. Apply `docs-reviewer` to every pull request and the applicable UI, accessibility, and design-system profiles to user-facing frontend changes. Report findings with severity and specific evidence, including a file and line when available. State what could not be verified; do not claim an unrun test passed.

Do not recommend approval while blocking findings, required failed or pending checks, or missing affected documentation remain. When documentation need not change, verify that the author explains why. If the pull request is ready, give Gabriela a reasoned recommendation and a constructive draft message for the author. Gabriela approves; the pull request author merges. The coordinator must not approve or merge a pull request.

## Reporting and limits

Notify Gabriela in Spanish about a pull request that needs review, changes, approval, or her decision; work unblocked by a merge; issues actually created or assigned; and a shortage of approved work. Include links and distinguish completed actions from proposals. Avoid repeated alerts with no meaningful change; a concise no-action result is sufficient when the event was checked and all queues are healthy.

Do not send messages to participants or publish comments on Gabriela's behalf without specific authorization. Do not expose secrets or real personal data. GitHub activity can show commits, pull requests, and issue changes; it cannot prove whether someone is working before they publish observable changes.
