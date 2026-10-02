param([switch]$Offline)
$ErrorActionPreference = 'Stop'
# Existing local paths are used only when environment overrides are absent.
if (-not $env:ANDROID_HOME -and (Test-Path 'F:\Android\Sdk')) { $env:ANDROID_HOME = 'F:\Android\Sdk' }
if (-not $env:ANDROID_USER_HOME -and (Test-Path 'F:\Android\user-home')) { $env:ANDROID_USER_HOME = 'F:\Android\user-home' }
if (-not $env:GRADLE_USER_HOME -and (Test-Path 'F:\Android\GradleCache')) { $env:GRADLE_USER_HOME = 'F:\Android\GradleCache' }
Push-Location $PSScriptRoot
try {
    $buildArgs = @(':app:assembleRelease', ':app:lintRelease', '--no-daemon', '--console=plain')
    if ($Offline) { $buildArgs += '--offline' }
    & ./gradlew.bat @buildArgs
    if ($LASTEXITCODE -ne 0) { throw "Android build failed: $LASTEXITCODE" }
    $metadata = Get-Content -LiteralPath 'app\build\outputs\apk\release\output-metadata.json' -Raw | ConvertFrom-Json
    New-Item -ItemType Directory -Path 'dist' -Force | Out-Null
    $apk = "dist\Tongge-$($metadata.elements[0].versionName).apk"
    Copy-Item -LiteralPath 'app\build\outputs\apk\release\app-release.apk' -Destination $apk -Force
    Copy-Item -LiteralPath $apk -Destination 'dist\Tongge.apk' -Force
    Write-Output "APK: $PSScriptRoot\$apk"
} finally { Pop-Location }
