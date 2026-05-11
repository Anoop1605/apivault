@echo off
echo Starting Sentinel Microservices with Docker Compose...
docker-compose up -d --build

echo.
echo Starting Frontend Application...
cd frontend

echo Installing Node Modules (if needed)...
call npm install

echo Starting Vite Dev Server...
start npm run dev

echo.
echo ========================================================
echo Sentinel Project is running!
echo Frontend: http://localhost:3000
echo ========================================================