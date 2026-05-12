Write-Host "--- Sentinel Real-Time Flow Monitor ---" -ForegroundColor Cyan
Write-Host "Watching Gateway, Policy Engine, and Event Store logs..." -ForegroundColor Yellow
Write-Host "Press Ctrl+C to stop."
Write-Host ""

Start-Job -ScriptBlock { docker logs -f --tail 0 sentinel-gateway }
Start-Job -ScriptBlock { docker logs -f --tail 0 sentinel-policy-engine }
Start-Job -ScriptBlock { docker logs -f --tail 0 sentinel-event-store }
Start-Job -ScriptBlock { docker logs -f --tail 0 sentinel-forensics-service }

while ($true) {
    Get-Job | Receive-Job
    Start-Sleep -Milliseconds 500
}
