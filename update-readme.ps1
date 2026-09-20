param(
    [Parameter(Mandatory = $true)][ValidateRange(1, 999999)][int]$Version,
    [string]$ReadmePath = (Join-Path $PSScriptRoot 'README.md'),
    [string]$ChangelogPath = (Join-Path $PSScriptRoot 'CHANGELOG.md'),
    [string]$ApkPath,
    [switch]$ValidateOnly
)
$ErrorActionPreference = 'Stop'
$readme = [IO.File]::ReadAllText($ReadmePath)
$changelog = [IO.File]::ReadAllText($ChangelogPath)
$pattern = '(?ms)^<!-- LATEST_RELEASE_START -->.*?^<!-- LATEST_RELEASE_END -->'
if ([regex]::Matches($readme, $pattern).Count -ne 1) { throw 'README harus memiliki tepat satu blok LATEST_RELEASE.' }
$notesPattern = '(?ms)^## 1\.0\.' + $Version + '\b[^\r\n]*\r?\n(?<notes>.*?)(?=^## |\z)'
$notes = [regex]::Match($changelog, $notesPattern)
if (-not $notes.Success -or -not $notes.Groups['notes'].Value.Trim()) { throw "Tambahkan catatan 1.0.$Version pada CHANGELOG.md sebelum rilis." }
if ($ValidateOnly) { return }
if (-not $ApkPath) { $ApkPath = Join-Path $PSScriptRoot "app/build/outputs/apk/debug/app-debug-v$Version.apk" }
if (-not (Test-Path -LiteralPath $ApkPath -PathType Leaf)) { throw 'APK belum tersedia. README tidak diubah.' }
$hash = (Get-FileHash -LiteralPath $ApkPath -Algorithm SHA256).Hash
$releaseNotes = $notes.Groups['notes'].Value.Trim()
$url = "https://github.com/Asricky/lux-wallet/raw/refs/heads/main/app/build/outputs/apk/debug/app-debug-v$Version.apk"
$block = @"
<!-- LATEST_RELEASE_START -->
## Download APK terbaru

[**Download app-debug-v$Version.apk**]($url)

Versi terbaru: **v$Version (1.0.$Version)**. Android 10 atau lebih baru.

Lokasi file: ``lux-wallet\app\build\outputs\apk\debug\app-debug-v$Version.apk``.

Semua versi sebelumnya tersedia di [folder APK debug](app/build/outputs/apk/debug). Nama launcher tetap **Lux Wallet**.

SHA-256: ``$hash``

### Pembaruan v$Version

$releaseNotes
<!-- LATEST_RELEASE_END -->
"@
$updated = [regex]::Replace($readme, $pattern, [System.Text.RegularExpressions.MatchEvaluator]{ param($match) $block })
[IO.File]::WriteAllText($ReadmePath, $updated, [System.Text.UTF8Encoding]::new($false))
