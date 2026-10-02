$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$projectRoot = Split-Path $PSScriptRoot -Parent
$sourcePath = Join-Path $projectRoot 'app/src/main/res/drawable/ic_launcher.xml'
[xml]$source = [System.IO.File]::ReadAllText($sourcePath)
$androidNamespace = 'http://schemas.android.com/apk/res/android'
$paths = @($source.vector.path)
$utf8 = [System.Text.UTF8Encoding]::new($false)

# The standalone Android vector is the geometric source for every exported asset.
$foreground = $source.Clone()
$foreground.vector.RemoveChild($foreground.vector.path[0]) | Out-Null
[System.IO.File]::WriteAllText((Join-Path $projectRoot 'app/src/main/res/drawable/ic_launcher_foreground.xml'), $foreground.OuterXml + "`n", $utf8)
$svgPaths = foreach ($path in $paths) {
    '  <path fill="' + $path.GetAttribute('fillColor', $androidNamespace) + '" d="' + $path.GetAttribute('pathData', $androidNamespace) + '"/>'
}
$svg = '<svg xmlns="http://www.w3.org/2000/svg" width="1024" height="1024" viewBox="0 0 108 108">' + "`n" + ($svgPaths -join "`n") + "`n</svg>`n"
[System.IO.File]::WriteAllText((Join-Path $PSScriptRoot 'tongge-original-icon.svg'), $svg, $utf8)

function ConvertTo-DrawingPath([string]$data) {
    $tokens = [regex]::Matches($data, '[MLCZ]|-?\d+(?:\.\d+)?') | ForEach-Object { $_.Value }
    $geometry = [System.Drawing.Drawing2D.GraphicsPath]::new()
    $index = 0
    $x = 0.0
    $y = 0.0
    while ($index -lt $tokens.Count) {
        $command = $tokens[$index++]
        switch ($command) {
            'M' {
                $geometry.StartFigure()
                $x = [float]::Parse($tokens[$index++], [Globalization.CultureInfo]::InvariantCulture)
                $y = [float]::Parse($tokens[$index++], [Globalization.CultureInfo]::InvariantCulture)
            }
            'L' {
                $nextX = [float]::Parse($tokens[$index++], [Globalization.CultureInfo]::InvariantCulture)
                $nextY = [float]::Parse($tokens[$index++], [Globalization.CultureInfo]::InvariantCulture)
                $geometry.AddLine([float]$x, [float]$y, $nextX, $nextY)
                $x = $nextX; $y = $nextY
            }
            'C' {
                $coordinates = for ($coordinate = 0; $coordinate -lt 6; $coordinate++) {
                    [float]::Parse($tokens[$index++], [Globalization.CultureInfo]::InvariantCulture)
                }
                $geometry.AddBezier([float]$x, [float]$y, $coordinates[0], $coordinates[1], $coordinates[2], $coordinates[3], $coordinates[4], $coordinates[5])
                $x = $coordinates[4]; $y = $coordinates[5]
            }
            'Z' { $geometry.CloseFigure() }
            default { throw "Unsupported path command: $command" }
        }
    }
    return ,$geometry
}

function Export-Preview([string]$filename, [string]$mask) {
    $bitmap = [System.Drawing.Bitmap]::new(1024, 1024)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    try {
        $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
        $graphics.Clear([System.Drawing.Color]::Transparent)
        $graphics.ScaleTransform([float](1024 / 108), [float](1024 / 108))
        if ($mask -eq 'circle') {
            $clip = [System.Drawing.Drawing2D.GraphicsPath]::new()
            $clip.AddEllipse(0, 0, 108, 108)
        } else {
            $clip = ConvertTo-DrawingPath 'M24,0 L84,0 C97.255,0 108,10.745 108,24 L108,84 C108,97.255 97.255,108 84,108 L24,108 C10.745,108 0,97.255 0,84 L0,24 C0,10.745 10.745,0 24,0 Z'
        }
        try { $graphics.SetClip($clip) } finally { $clip.Dispose() }
        foreach ($path in $paths) {
            $geometry = ConvertTo-DrawingPath $path.GetAttribute('pathData', $androidNamespace)
            $brush = [System.Drawing.SolidBrush]::new([System.Drawing.ColorTranslator]::FromHtml($path.GetAttribute('fillColor', $androidNamespace)))
            try { $graphics.FillPath($brush, $geometry) } finally { $brush.Dispose(); $geometry.Dispose() }
        }
        $bitmap.Save((Join-Path $PSScriptRoot $filename), [System.Drawing.Imaging.ImageFormat]::Png)
    } finally { $graphics.Dispose(); $bitmap.Dispose() }
}
Export-Preview 'tongge-original-icon.png' 'rounded-square'
Export-Preview 'tongge-original-icon-circle.png' 'circle'
Write-Output 'Generated foreground vector, matching SVG, and two 1024px PNG previews.'
