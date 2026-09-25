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
$url = "https://github.com/Asricky/lumi/raw/refs/heads/main/app/build/outputs/apk/debug/app-debug-v$Version.apk"
$block = @"
<!-- LATEST_RELEASE_START -->
## Download aplikasi

[**Download Lumi - app-debug-v$Version.apk**]($url)

**v$Version / 1.0.$Version** &nbsp; | &nbsp; Android 10+ &nbsp; | &nbsp; [Semua versi](app/build/outputs/apk/debug)

### Yang baru

$releaseNotes

<details>
<summary>Lokasi file &amp; verifikasi unduhan</summary>

File: ``app/build/outputs/apk/debug/app-debug-v$Version.apk``

SHA-256:

``````text
$hash
``````

</details>
<!-- LATEST_RELEASE_END -->
"@
$updated = [regex]::Replace($readme, $pattern, [System.Text.RegularExpressions.MatchEvaluator]{ param($match) $block })
[IO.File]::WriteAllText($ReadmePath, $updated, [System.Text.UTF8Encoding]::new($false))
