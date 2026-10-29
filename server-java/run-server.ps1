# PowerShell script to run server from server-java folder
$scriptPath = Join-Path (Split-Path -Parent -Path $MyInvocation.MyCommand.Definition) "..\run-server.ps1"
& $scriptPath
