# PowerShell Script to run AgriCoop Java JDBC + MySQL Server
$PSScriptRoot = Split-Path -Parent -Path $MyInvocation.MyCommand.Definition
Set-Location -Path $PSScriptRoot

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "  Starting AgriCoop Java JDBC + MySQL Server" -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "Working Directory: $PSScriptRoot"

if (-not (Test-Path "server-java\target\classes")) {
    Write-Host "[INFO] Compiled classes not found. Building with Maven..." -ForegroundColor Yellow
    mvn -f server-java/pom.xml compile -q
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Maven compilation failed."
        exit $LASTEXITCODE
    }
}

Write-Host "[INFO] Launching AgriCoop Java Server on port 3000..." -ForegroundColor Green
Write-Host "[INFO] Open your browser at: http://localhost:3000" -ForegroundColor Cyan
Write-Host "[INFO] Press Ctrl+C to stop the server." -ForegroundColor Yellow
Write-Host "========================================================" -ForegroundColor Cyan

java -cp "server-java/target/classes;server-java/lib/*" com.agricoop.AgriCoopServer
