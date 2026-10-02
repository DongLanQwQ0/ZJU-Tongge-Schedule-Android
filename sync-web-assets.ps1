param([Parameter(Mandatory=$true)][string]$WebProject)
$ErrorActionPreference = 'Stop'
$sourceRoot = (Resolve-Path -LiteralPath $WebProject).Path
$sharedFiles = @('config.js', 'periods.js', 'ics.js', 'weeks.js', 'compare.js', 'stats.js')
if (-not (Test-Path -LiteralPath "$sourceRoot\public\index.html")) { throw 'Missing public/index.html' }
foreach ($name in $sharedFiles) {
    if (-not (Test-Path -LiteralPath "$sourceRoot\shared\$name")) { throw "Missing shared/$name" }
}
$destination = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot 'app\src\main\assets\web'))
$expected = [IO.Path]::GetFullPath("$PSScriptRoot\app\src\main\assets\web")
if ($destination -ne $expected) { throw 'Unsafe asset destination' }
if (Test-Path -LiteralPath $destination) { Remove-Item -LiteralPath $destination -Recurse -Force }
New-Item -ItemType Directory -Path "$destination\shared" -Force | Out-Null
Copy-Item -Path "$sourceRoot\public\*" -Destination $destination -Recurse -Force
foreach ($name in $sharedFiles) {
    Copy-Item -LiteralPath "$sourceRoot\shared\$name" -Destination "$destination\shared\$name"
}
Write-Output "Embedded web assets synchronized from: $sourceRoot"
