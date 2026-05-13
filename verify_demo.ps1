# 🎯 SENTINEL PROJECT - PRE-DEMO VERIFICATION (Windows PowerShell)
# Run this script 5 minutes before demo to verify everything is working

Write-Host "🔍 SENTINEL PROJECT - PRE-DEMO VERIFICATION" -ForegroundColor Cyan
Write-Host "==========================================="
Write-Host ""

# Check 1: Docker Services
Write-Host "✓ Checking Docker Services..." -ForegroundColor Yellow
$serviceCount = (docker ps | Select-String "sentinel" | Measure-Object).Count
if ($serviceCount -eq 8) {
    Write-Host "  ✅ All 8 services running" -ForegroundColor Green
} else {
    Write-Host "  ⚠️  Only $serviceCount services running (should be 8)" -ForegroundColor Red
    Write-Host "     Run: docker-compose up -d"
}
Write-Host ""

# Check 2: Frontend
Write-Host "✓ Checking Frontend..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:3000" -TimeoutSec 3 -ErrorAction Stop
    Write-Host "  ✅ Frontend responsive on http://localhost:3000" -ForegroundColor Green
} catch {
    Write-Host "  ⚠️  Frontend not responding" -ForegroundColor Red
    Write-Host "     Run in terminal: cd frontend && npm run dev"
}
Write-Host ""

# Check 3: Policy Engine
Write-Host "✓ Checking Policy Engine (8082)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8082/actuator/health" -TimeoutSec 3 -ErrorAction Stop
    Write-Host "  ✅ Policy Engine responding" -ForegroundColor Green
} catch {
    Write-Host "  ⚠️  Policy Engine not responding" -ForegroundColor Red
}
Write-Host ""

# Check 4: Forensics Service
Write-Host "✓ Checking Forensics Service (8083)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8083/actuator/health" -TimeoutSec 3 -ErrorAction Stop
    Write-Host "  ✅ Forensics Service responding" -ForegroundColor Green
} catch {
    Write-Host "  ⚠️  Forensics Service not responding" -ForegroundColor Red
}
Write-Host ""

# Check 5: Event Store
Write-Host "✓ Checking Event Store (8081)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/actuator/health" -TimeoutSec 3 -ErrorAction Stop
    Write-Host "  ✅ Event Store responding" -ForegroundColor Green
} catch {
    Write-Host "  ⚠️  Event Store not responding" -ForegroundColor Red
}
Write-Host ""

# Check 6: Database
Write-Host "✓ Checking PostgreSQL (5432)..." -ForegroundColor Yellow
try {
    docker exec sentinel-postgres psql -U sentinel -d sentinel_db -c "SELECT 1" > $null 2>&1
    Write-Host "  ✅ PostgreSQL responding" -ForegroundColor Green
} catch {
    Write-Host "  ⚠️  PostgreSQL not responding" -ForegroundColor Red
}
Write-Host ""

# Check 7: Demo Data
Write-Host "✓ Checking Demo Data..." -ForegroundColor Yellow
try {
    $eventCount = docker exec sentinel-postgres psql -U sentinel -d sentinel_db -t -c "SELECT COUNT(*) FROM events;" 2>$null
    $eventCount = $eventCount.Trim()
    if ([int]$eventCount -gt 0) {
        Write-Host "  ✅ Database has $eventCount events" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  No event data found" -ForegroundColor Yellow
        Write-Host "     This is OK - frontend has mock data fallback"
    }
} catch {
    Write-Host "  ⚠️  Could not check event data" -ForegroundColor Yellow
    Write-Host "     This is OK - frontend has mock data fallback"
}
Write-Host ""

Write-Host "==========================================="
Write-Host "📋 DEMO READY CHECKLIST" -ForegroundColor Cyan
Write-Host "==========================================="
Write-Host ""
Write-Host "Frontend Pages to Test:"
Write-Host "  [ ] Dashboard: http://localhost:3000/"
Write-Host "  [ ] Sessions: http://localhost:3000/sessions"
Write-Host "  [ ] Alerts: http://localhost:3000/alerts"
Write-Host "  [ ] ReplayPage: (after clicking session)"
Write-Host ""
Write-Host "Backend Services:"
Write-Host "  [ ] Policy Engine on 8082"
Write-Host "  [ ] Forensics Service on 8083"
Write-Host "  [ ] Event Store on 8081"
Write-Host "  [ ] Gateway on 8080"
Write-Host "  [ ] PostgreSQL on 5432"
Write-Host ""
Write-Host "Demo Flow:"
Write-Host "  1. Show Dashboard (2 min)"
Write-Host "  2. Click Sessions (1 min)"
Write-Host "  3. Select high-risk session (1 min)"
Write-Host "  4. Click 'Replay Session' (6 min) ⭐"
Write-Host "  5. Show What-If Simulation (3 min)"
Write-Host "  6. Show Alerts (2 min)"
Write-Host ""
Write-Host "Total Demo Time: 15-20 minutes"
Write-Host ""
Write-Host "🎯 Ready? Open http://localhost:3000 and START!" -ForegroundColor Green
Write-Host ""
