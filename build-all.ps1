# Builds every folder in versions/ and collects the jars into dist/.
# Pass version names to build only some: .\build-all.ps1 26.2 1.21.11
param([string[]]$Only)

$root = $PSScriptRoot
if (-not $env:JAVA_HOME) {
	$jdk = Get-ChildItem "$env:USERPROFILE\.jdks" -Directory -Filter "jdk-25*" -ErrorAction SilentlyContinue | Select-Object -First 1
	if ($jdk) { $env:JAVA_HOME = $jdk.FullName }
}

# Java's downloader times out over IPv6 on this machine.
$env:JAVA_OPTS = "-Djava.net.preferIPv4Stack=true"

$dist = Join-Path $root "dist"
New-Item -ItemType Directory -Force $dist | Out-Null
$failed = @()

foreach ($dir in Get-ChildItem "$root\versions" -Directory) {
	if ($Only -and $Only -notcontains $dir.Name) { continue }
	Write-Host "=== Building for Minecraft $($dir.Name)" -ForegroundColor Cyan
	& "$root\gradlew.bat" -p $dir.FullName build --no-daemon -q
	if ($LASTEXITCODE -ne 0) { $failed += $dir.Name; continue }
	Get-ChildItem "$($dir.FullName)\build\libs\*.jar" | Where-Object { $_.Name -notmatch "sources|dev" } |
		Copy-Item -Destination $dist -Force
}

Get-ChildItem $dist | Select-Object Name
if ($failed) { Write-Host "FAILED: $($failed -join ', ')" -ForegroundColor Red; exit 1 }
