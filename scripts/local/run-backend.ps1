# Loads cases-MT/.env into the process environment, then starts Spring Boot.
#
# Why: Spring Boot does not read .env files. Placeholders like ${DB_USER} only
# resolve from real OS environment variables (or a secret manager in prod).
#
# Usage (from repo root):
#   powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\local\run-backend.ps1

$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$mtDir = Join-Path $repoRoot "cases-MT"
$envFile = Join-Path $mtDir ".env"

if (-not (Test-Path $envFile)) {
    Write-Error "Missing $envFile. Copy cases-MT/.env.example to cases-MT/.env and fill in DB_URL, DB_USER, DB_PASSWORD."
}

Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()
    if ($line -eq "" -or $line.StartsWith("#")) {
        return
    }
    $parts = $line -split "=", 2
    if ($parts.Count -ne 2) {
        return
    }
    $name = $parts[0].Trim()
    $value = $parts[1].Trim().Trim('"').Trim("'")
    Set-Item -Path "Env:$name" -Value $value
}

$required = @("DB_URL", "DB_USER", "DB_PASSWORD")
foreach ($key in $required) {
    $val = [Environment]::GetEnvironmentVariable($key)
    if ([string]::IsNullOrWhiteSpace($val)) {
        Write-Error "$key is missing or empty after loading .env. Set it in $envFile."
    }
    if ($val -match '\$\{') {
        Write-Error "$key still looks like an unresolved placeholder. Check $envFile."
    }
}

Write-Host "Loaded $envFile"
Write-Host "Verified DB_URL / DB_USER / DB_PASSWORD are set (values not printed)."
Write-Host "Starting cases-MT..."
Set-Location $mtDir
& .\mvnw.cmd -DskipTests spring-boot:run
