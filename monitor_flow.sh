echo "--- Sentinel Real-Time Flow Monitor ---"
echo "Watching Gateway, Policy Engine, and Event Store logs..."
echo "Press Ctrl+C to stop."
echo ""

docker logs -f --tail 0 sentinel-gateway & 
docker logs -f --tail 0 sentinel-policy-engine & 
docker logs -f --tail 0 sentinel-event-store & 
docker logs -f --tail 0 sentinel-forensics-service &

wait
