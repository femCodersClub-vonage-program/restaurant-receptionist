# Security policy

Restaurant Receptionist handles phone numbers and will use Vonage credentials, so we take security reports seriously even though this is a pilot project using a fictional restaurant and test data.

## Reporting a vulnerability

Do not report vulnerabilities through public issues, pull requests, or Slack.

Report them privately through GitHub: open the repository's **Security** tab and select **Report a vulnerability**. Only repository maintainers can see the report.

A useful report includes:

- What the vulnerability is and where it appears, such as a file, endpoint, webhook, or screen.
- How to reproduce it.
- What an attacker could achieve.

Do not include real personal data in the report. Use a Vonage test number or a redacted example when needed.

## What happens next

We will acknowledge the report, keep the reporter informed while we investigate it, and explain the outcome. If we decide that the report is not a vulnerability, we will explain why.

## Credentials and test data

Credentials must never be committed to the repository. Once local environment configuration is added, `.env` files will remain local and `.env.example` files will contain variable names and placeholder values only.

If a Vonage API key, secret, private key, or test-number configuration is exposed in the repository, its history, a pull request, or a public log, report it privately. We will revoke or replace the affected credential first and then remove it from the repository; removing it from source code alone does not make it safe.

Controlled integration or demo testing may use a consenting team member's phone number or a Vonage test number through local configuration only. Never commit or publish those numbers in repository files, seed data, logs, issues, or screenshots.

## Scope

This policy covers the code and configuration in this repository. If an issue may originate in the Vonage platform, report it here first so we can rule out a cause in our own implementation before escalating it to Vonage.
