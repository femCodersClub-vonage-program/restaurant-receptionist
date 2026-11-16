# Vonage MCP and CLI setup

This guide covers local developer tools for issue #27, not the application runtime. Each teammate configures the tools on her own machine and uses her own Vonage account. Never share credentials or commit them.

The API key and API secret are account-level credentials. The key/secret used for your training demo belong to the Vonage account you used there; they are not shared team credentials or fixed project credentials. For this setup, each teammate uses her own account. If the team later creates a project account, it will have its own credentials.

Neither tool automatically loads credentials from the project's `.env`; configure each tool as described below.

## 1. Configure the Vonage MCP server

Each AI agent reads its own MCP configuration format and location; there is no universal MCP config file. Follow your agent's setup instructions. For VS Code Copilot, the personal config is `~/.copilot/mcp-config.json` (Windows: `%USERPROFILE%\.copilot\mcp-config.json`). See Vonage's [Tooling MCP installation guide](https://developer.vonage.com/en/mcp-server/how-to-guides/install-tooling-mcp-server); the client-specific config format may differ.

Do not add a repository-root `.mcp.json`. Keep the MCP configuration in your personal agent config.

### Open or create the Copilot config

**Windows PowerShell**

```powershell
$directory = Join-Path $HOME ".copilot"
$file = Join-Path $directory "mcp-config.json"
New-Item -ItemType Directory -Force $directory | Out-Null
if (-not (Test-Path $file)) {
  Set-Content -Path $file -Value '{"mcpServers":{}}' -Encoding ASCII
}
notepad $file
```

**macOS Terminal**

```bash
mkdir -p "$HOME/.copilot"
[ -f "$HOME/.copilot/mcp-config.json" ] || printf '{"mcpServers":{}}\n' > "$HOME/.copilot/mcp-config.json"
open -e "$HOME/.copilot/mcp-config.json"
```

If the file already has other servers, preserve them. Add this entry inside its top-level `mcpServers` object. Fill in your account API key and secret; keep the other values empty for now.

```json
{
  "mcpServers": {
    "vonage-mcp-server-api-bindings": {
      "type": "stdio",
      "command": "npx",
      "args": ["-y", "@vonage/vonage-mcp-server-api-bindings"],
      "env": {
        "VONAGE_APPLICATION_ID": "",
        "VONAGE_PRIVATE_KEY64": "",
        "VONAGE_API_KEY": "<YOUR_VONAGE_API_KEY>",
        "VONAGE_API_SECRET": "<YOUR_VONAGE_API_SECRET>",
        "VONAGE_VIRTUAL_NUMBER": "",
        "VONAGE_WHATSAPP_NUMBER": ""
      }
    }
  }
}
```

For the balance check, only the API key and secret are needed. The other fields stay in the example for tools that may need them later:

- `VONAGE_APPLICATION_ID` and `VONAGE_PRIVATE_KEY64` belong to a Vonage Application. The `private.key` in your `vonage` folder is from the training demo, not a Restaurant Receptionist app. Leave both blank. If the team creates an application, its matching ID and key will be agreed then; do not use the training-demo pair.
- `VONAGE_VIRTUAL_NUMBER` and `VONAGE_WHATSAPP_NUMBER` are not needed for this setup; leave them blank. RCS is outside the MVP, so there is no RCS variable.

The screenshots show where account API credentials and an Application ID appear in the Dashboard. The application shown is an example, not the team's app.

![Vonage API Settings showing where the API key and secret are; values are redacted.](images/vonage-api-settings.png)

![Vonage Application details showing the Application ID location; identifiers are redacted.](images/vonage-application-details.png)

The screenshots are repository files and will be available to teammates after the pull request is merged.

Keep the API secret only in your personal config, not in source control or shared files. If a secret is exposed, create a replacement, update the tools that use it, then revoke the exposed secret in Vonage Dashboard.

## 2. Check the MCP connection

Save the config and restart/reload the AI agent. Ask the configured agent:

> Use the Vonage MCP server to check my account balance.

The agent should call the MCP `balance` tool. This is a read-only check. There is no shell command that performs this MCP tool call directly; `vonage balance` below checks the CLI only, not MCP. A Dashboard balance view also does not verify the MCP connection.

## 3. Configure and check the Vonage CLI

The CLI is a separate required setup in issue #27. It does not read the MCP config; configure it on your machine with your own account API key and secret.

Install on Windows or macOS:

```shell
npm install -g @vonage/cli
```

Configure it with your account API key and secret. Vonage's [CLI guide](https://developer.vonage.com/en/vonage-cli/getting-started) documents `--api-secret` as a command-line option for `vonage auth set`. The prompts below hide the secret while you type and keep its literal value out of shell history, but it is passed to the CLI as a process argument and may be visible to local process-inspection tools. Run this only on your trusted personal computer, not a shared machine, and do not record the command session.

**Windows PowerShell**

```powershell
$apiKey = Read-Host "Vonage API key"
$apiSecretSecure = Read-Host "Vonage API secret" -AsSecureString
$apiSecret = [System.Net.NetworkCredential]::new("", $apiSecretSecure).Password
vonage auth set --api-key $apiKey --api-secret $apiSecret
Remove-Variable apiKey, apiSecretSecure, apiSecret
```

**macOS Terminal**

```bash
read -r -p "Vonage API key: " apiKey
read -r -s -p "Vonage API secret: " apiSecret
printf '\n'
vonage auth set --api-key "$apiKey" --api-secret "$apiSecret"
unset apiKey apiSecret
```

Check the CLI on either operating system:

```shell
vonage balance
```

This read-only command confirms the CLI credentials and displays your personal account balance; do not share its output. The CLI saves its configuration separately from the MCP config (`%USERPROFILE%\.vonage\config.json` on Windows, `~/.vonage/config.json` on macOS).

If `vonage auth set` reports **Invalid API Key or Secret**, check that the key and active secret belong to the same API Key entry in Dashboard > API Settings. A failed `auth set` does not save the CLI config, so `vonage balance` will then report that no config was found.

## Remove saved credentials if you stop using the tools

- Remove only the Vonage server entry from your personal agent config; preserve any other servers.
- The CLI credentials are in the config file listed above. Delete that file only if you no longer need any Vonage CLI configuration. This removes saved CLI credentials; it does not uninstall the CLI.

## Scope of the checks

Issue #27 authorizes read-only balance checks. Application or number creation/linking changes account state; sending SMS or placing calls may consume credits. None of those operations were used, and they are outside this issue.

## Validation status

- CLI `@vonage/cli@3.3.1` installed and version/help checked on Windows. A teammate confirmed that `vonage auth set` and `vonage balance` succeeded with her account.
- MCP `@vonage/vonage-mcp-server-api-bindings` `balance` call through an AI agent succeeded; do not share the returned account balance.
- macOS-specific commands have not been tested.
- A second participant has not yet followed the guide; this check is planned after the pull request is merged.
