param([switch]$Offline, [switch]$Preview)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    $versionFile = Join-Path $PSScriptRoot 'version.properties'
    $currentVersion = [int]((Get-Content $versionFile | Where-Object { $_ -match '^APK_VERSION=' }) -replace '^APK_VERSION=', '')
    $builtVersion = [int]((Get-Content $versionFile | Where-Object { $_ -match '^BUILT_VERSION=' }) -replace '^BUILT_VERSION=', '')
    $outputDir = Join-Path $PSScriptRoot 'app/build/outputs/apk/debug'
    $nextVersion = [Math]::Max($currentVersion, $builtVersion + 1)
    if ($Preview) { Write-Output (Join-Path $outputDir "app-debug-v$nextVersion.apk"); return }
    $archiveDir = Join-Path $PSScriptRoot '.local/apk-archive'
    New-Item -ItemType Directory -Force -Path $archiveDir | Out-Null
    Get-ChildItem -LiteralPath $outputDir -Filter 'app-debug-v*.apk' -ErrorAction SilentlyContinue |
        Copy-Item -Destination $archiveDir -Force
    $buildArgs = @('testDebugUnitTest', 'lintDebug', 'assembleDebug', "-PapkVersion=$nextVersion")
    if ($Offline) { $buildArgs += '--offline' }
    & .\gradlew.bat @buildArgs
    if ($LASTEXITCODE -ne 0) { throw 'Build gagal. Nomor versi tidak diubah.' }
    $apk = Join-Path $outputDir "app-debug-v$nextVersion.apk"
    if (-not (Test-Path -LiteralPath $apk)) { throw "APK tidak ditemukan: $apk" }
    [IO.File]::WriteAllText($versionFile, "APK_VERSION=$nextVersion" + [Environment]::NewLine + "BUILT_VERSION=$nextVersion" + [Environment]::NewLine)
    Write-Host "APK siap: $apk"
    Get-FileHash -LiteralPath $apk -Algorithm SHA256
} finally {
    if ($archiveDir -and (Test-Path -LiteralPath $archiveDir)) {
        New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
        Get-ChildItem -LiteralPath $archiveDir -Filter 'app-debug-v*.apk' | ForEach-Object {
            $restorePath = Join-Path $outputDir $_.Name
            if (-not (Test-Path -LiteralPath $restorePath)) { Copy-Item -LiteralPath $_.FullName -Destination $restorePath }
        }
    }
    Pop-Location
}
