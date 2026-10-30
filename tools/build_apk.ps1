$ErrorActionPreference = "Stop"
$jdkPath = (Resolve-Path "$PSScriptRoot\jdk-17").Path
$env:JAVA_HOME = $jdkPath
$env:PATH = "$jdkPath\bin;$env:PATH"
Set-Location "$PSScriptRoot\..\launcher"
Write-Host "Using JAVA_HOME: $env:JAVA_HOME"
& .\gradlew.bat --no-daemon assembleRelease
