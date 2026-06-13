# One-step bootstrap for Tessera on Windows.
# Usage:  powershell -ExecutionPolicy Bypass -File .\bootstrap.ps1
$ErrorActionPreference = "Stop"

Write-Host ">> Tessera bootstrap"

function Get-JavaMajor([string]$javaExe) {
    # java -version writes to stderr; PS5.1 2>&1 wraps it as ErrorRecord, so stringify first.
    try {
        $out = & $javaExe -version 2>&1 | ForEach-Object { $_.ToString() } | Select-Object -First 1
        if ($out -match 'version "(\d+)') { return [int]$Matches[1] }
    } catch { }
    return 0
}

function Get-MajorFromDirName([string]$name) {
    if ($name -match 'jdk-?(\d+)') { return [int]$Matches[1] }
    return 0
}

# 1. Find a JDK in the supported range (17 through 23).
$jdkHome = $null
if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    $m = Get-JavaMajor "$env:JAVA_HOME\bin\java.exe"
    if ($m -ge 17 -and $m -le 23) { $jdkHome = $env:JAVA_HOME }
}
if (-not $jdkHome) {
    $searchRoots = @(
        "C:\Program Files\Microsoft",
        "C:\Program Files\Java",
        "C:\Program Files\Eclipse Adoptium",
        "C:\Program Files\Eclipse Foundation",
        "C:\Program Files\Microsoft\",
        "$env:LOCALAPPDATA\Programs"
    )
    $candidates = Get-ChildItem -Path $searchRoots -Directory -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -match 'jdk' }
    foreach ($c in $candidates) {
        if (-not (Test-Path "$($c.FullName)\bin\java.exe")) { continue }
        # Parse version from directory name first (avoids PS5.1 stderr quirk on slow machines).
        $m = Get-MajorFromDirName $c.Name
        if ($m -eq 0) { $m = Get-JavaMajor "$($c.FullName)\bin\java.exe" }
        if ($m -ge 17 -and $m -le 23) { $jdkHome = $c.FullName; break }
    }
}
if (-not $jdkHome) {
    Write-Error "No JDK in range 17-23 found. Install a JDK 17 (LTS) and retry."
    exit 1
}
$env:JAVA_HOME = $jdkHome
Write-Host ".. using JDK at $jdkHome"

# 2. Install pre-commit hooks if available.
if (Get-Command pre-commit -ErrorAction SilentlyContinue) {
    pre-commit install
    Write-Host ".. pre-commit hooks installed"
} else {
    Write-Host "!! pre-commit not found. Install it (pip install pre-commit) to enable local hooks."
}

# 3. Seed keystore.properties from the example if absent.
if (-not (Test-Path keystore.properties)) {
    Copy-Item keystore.properties.example keystore.properties
    Write-Host ".. created keystore.properties from example (edit before release signing)"
}

# 4. Generate a debug keystore if absent.
$debugKs = Join-Path $env:USERPROFILE ".android\debug.keystore"
if (-not (Test-Path $debugKs) -and (Test-Path "$jdkHome\bin\keytool.exe")) {
    New-Item -ItemType Directory -Force (Split-Path $debugKs) | Out-Null
    & "$jdkHome\bin\keytool.exe" -genkeypair -v -keystore $debugKs -storepass android -keypass android `
        -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 `
        -dname "CN=Android Debug,O=Android,C=US" | Out-Null
    Write-Host ".. generated debug keystore"
}

# 5. Prove a clean FOSS debug build.
Write-Host ">> assembleFossDebug"
.\gradlew.bat assembleFossDebug
Write-Host ">> bootstrap complete"
