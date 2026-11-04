## What does this pull request change and why?

<!-- Explain the change and why it is needed in one or two clear sentences. -->

## Out of scope

<!-- State what this pull request intentionally does not implement. -->

## Links

<!-- Use N/A with a short reason only for an approved exception. -->

- Closes #
- Spec:

## How to test

<!-- List the commands, manual checks, or scenarios you ran. State what could not be tested and why. -->

1.

## Screenshots

<!-- Add screenshots or a short recording when visual evidence helps a reviewer verify a user-facing change. Include it for changed layout, visual styling, or visible states. Delete this section when it does not apply. Use fictional data only. -->

## Checklist

> Complete this checklist before requesting review, or before marking a draft pull request ready for review. Checking a box states that the work has already been completed. If an item does not apply, write `N/A` and the reason in **Notes for the reviewer**.

**Every pull request**

- [ ] I ran `docs-reviewer` on this branch and addressed its blocking and important findings.
- [ ] Changes stay within the scope of the linked issue and spec.
- [ ] I ran the relevant validation or explained why it could not run.
- [ ] No secrets, Vonage credentials, real personal data, or real `.env` files are included.
- [ ] Repository-facing content, including code, comments, documentation, commit messages, and pull request content, is in English.

**If applicable**

- [ ] Tests were added or updated from the spec's acceptance criteria.
- [ ] Documentation was updated in this pull request: relevant project documents, the linked spec if implementation changed it, setup instructions, architecture diagrams, guides, or ADRs as needed.
- [ ] An ADR was added or updated for a significant architecture, dependency, or security decision.
- [ ] A new dependency is justified in this pull request, including why it is needed and why the approved stack does not already solve the problem.
- [ ] UI: I ran `ui-reviewer`, `accessibility-reviewer`, and `design-system-reviewer`; addressed their blocking and important findings; and reported Tab/Shift+Tab navigation, visible-focus results, and any checks not verified.
- [ ] Database migration: the Flyway migration follows `CONTRIBUTING.md`, the issue and pull request state the proposed filename, and I checked for version collisions after updating from `develop`.
- [ ] Vonage: behavior was checked against current official documentation, and tests use test doubles or verified local mechanisms rather than real credits by default.

## Notes for the reviewer

<!-- Record profile results, checks that could not be verified, relevant decisions, dependencies, trade-offs, or follow-up work. -->
