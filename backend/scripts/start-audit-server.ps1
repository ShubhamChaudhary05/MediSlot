param(
    [Parameter(Mandatory = $true)][string]$AdminEmail,
    [Parameter(Mandatory = $true)][string]$AdminPassword,
    [int]$Port = 8081,
    [ValidateSet("disabled", "development-file", "smtp")][string]$MailMode = "disabled",
    [string]$ResetUrl = "http://localhost:3000/reset-password",
    [string]$DevelopmentOutbox = ".dev-reset-emails"
)

# This process-only key is intentionally not written to .env or source control.
$env:JWT_SECRET = [Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
$env:ADMIN_BOOTSTRAP_EMAIL = $AdminEmail
$env:ADMIN_BOOTSTRAP_PASSWORD = $AdminPassword
$env:ADMIN_BOOTSTRAP_NAME = "MediSlot QA Administrator"
$env:MAIL_MODE = $MailMode
$env:PASSWORD_RESET_URL = $ResetUrl
$env:DEV_RESET_OUTBOX_PATH = $DevelopmentOutbox

$backendRoot = Split-Path $PSScriptRoot -Parent
$process = Start-Process -FilePath "mvn" -ArgumentList @("-q", "-Dmaven.repo.local=target/m2", "spring-boot:run", "-Dspring-boot.run.arguments=--server.port=$Port") -WorkingDirectory $backendRoot -WindowStyle Hidden -RedirectStandardOutput (Join-Path $backendRoot "target\audit-server-$Port.stdout.log") -RedirectStandardError (Join-Path $backendRoot "target\audit-server-$Port.stderr.log") -PassThru
Write-Output $process.Id
