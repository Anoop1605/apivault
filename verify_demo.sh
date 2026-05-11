#!/usr/bin/env bash
# 🎯 SENTINEL PROJECT - PRE-DEMO VERIFICATION CHECKLIST
# Run this script 5 minutes before demo to verify everything is working

echo "🔍 SENTINEL PROJECT - PRE-DEMO VERIFICATION"
echo "==========================================="
echo ""

# Check 1: Docker Services
echo "✓ Checking Docker Services..."
SERVICE_COUNT=$(docker ps | grep sentinel | wc -l)
if [ "$SERVICE_COUNT" -eq 8 ]; then
    echo "  ✅ All 8 services running"
else
    echo "  ⚠️  Only $SERVICE_COUNT services running (should be 8)"
    echo "     Run: docker-compose up -d"
fi
echo ""

# Check 2: Frontend
echo "✓ Checking Frontend..."
if curl -s http://localhost:3000 > /dev/null 2>&1; then
    echo "  ✅ Frontend responsive on http://localhost:3000"
else
    echo "  ⚠️  Frontend not responding"
    echo "     Run in terminal: cd frontend && npm run dev"
fi
echo ""

# Check 3: Policy Engine
echo "✓ Checking Policy Engine (8082)..."
if curl -s http://localhost:8082/actuator/health > /dev/null 2>&1; then
    echo "  ✅ Policy Engine responding"
else
    echo "  ⚠️  Policy Engine not responding"
    echo "     Check: docker logs sentinel-policy-engine | tail -20"
fi
echo ""

# Check 4: Forensics Service
echo "✓ Checking Forensics Service (8083)..."
if curl -s http://localhost:8083/actuator/health > /dev/null 2>&1; then
    echo "  ✅ Forensics Service responding"
else
    echo "  ⚠️  Forensics Service not responding"
    echo "     Check: docker logs sentinel-forensics-service | tail -20"
fi
echo ""

# Check 5: Event Store
echo "✓ Checking Event Store (8081)..."
if curl -s http://localhost:8081/actuator/health > /dev/null 2>&1; then
    echo "  ✅ Event Store responding"
else
    echo "  ⚠️  Event Store not responding"
    echo "     Check: docker logs sentinel-event-store | tail -20"
fi
echo ""

# Check 6: Database
echo "✓ Checking PostgreSQL (5432)..."
if docker exec sentinel-postgres psql -U sentinel -d sentinel_db -c "SELECT 1" > /dev/null 2>&1; then
    echo "  ✅ PostgreSQL responding"
else
    echo "  ⚠️  PostgreSQL not responding"
fi
echo ""

# Check 7: Demo Data
echo "✓ Checking Demo Data..."
SESSION_COUNT=$(docker exec sentinel-postgres psql -U sentinel -d sentinel_db -t -c "SELECT COUNT(*) FROM events;" 2>/dev/null || echo "0")
if [ "$SESSION_COUNT" -gt "0" ]; then
    echo "  ✅ Database has $SESSION_COUNT events"
else
    echo "  ⚠️  No event data found"
    echo "     This is OK - frontend has mock data fallback"
fi
echo ""

echo "==========================================="
echo "📋 DEMO READY CHECKLIST"
echo "==========================================="
echo ""
echo "Frontend Pages to Test:"
echo "  [ ] Dashboard: http://localhost:3000/"
echo "  [ ] Sessions: http://localhost:3000/sessions"
echo "  [ ] Alerts: http://localhost:3000/alerts"
echo "  [ ] ReplayPage: (after clicking session)"
echo ""
echo "Backend Services:"
echo "  [ ] Policy Engine on 8082"
echo "  [ ] Forensics Service on 8083"
echo "  [ ] Event Store on 8081"
echo "  [ ] Gateway on 8080"
echo "  [ ] PostgreSQL on 5432"
echo ""
echo "Demo Flow:"
echo "  1. Show Dashboard (2 min)"
echo "  2. Click Sessions (1 min)"
echo "  3. Select high-risk session (1 min)"
echo "  4. Click 'Replay Session' (6 min) ⭐"
echo "  5. Show What-If Simulation (3 min)"
echo "  6. Show Alerts (2 min)"
echo ""
echo "Total Demo Time: 15-20 minutes"
echo ""
echo "🎯 Ready? Open http://localhost:3000 and START!"
echo ""
