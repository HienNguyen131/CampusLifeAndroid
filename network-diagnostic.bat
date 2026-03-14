@echo off
REM Network Diagnostic Script for CampusLife Android App
REM This script helps diagnose network connectivity issues

echo ================================================
echo CampusLife Network Diagnostic Tool
echo ================================================
echo.

echo [1] Checking your machine's IP addresses...
echo.
ipconfig | findstr /C:"IPv4"
echo.

echo [2] Checking if port 8080 is in use...
echo.
netstat -ano | findstr ":8080"
if %ERRORLEVEL% EQU 0 (
    echo Port 8080 is ACTIVE
) else (
    echo Port 8080 is NOT active - Backend may not be running!
)
echo.

echo [3] Testing localhost connectivity...
echo.
curl -s -o nul -w "Status: %%{http_code}\n" http://localhost:8080 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Cannot connect to http://localhost:8080
    echo Make sure your backend server is running!
) else (
    echo Successfully connected to localhost:8080
)
echo.

echo [4] Firewall Check...
echo Checking if port 8080 is allowed in Windows Firewall...
netsh advfirewall firewall show rule name=all | findstr /C:"8080" >nul
if %ERRORLEVEL% EQU 0 (
    echo Firewall rules found for port 8080
) else (
    echo No firewall rules found for port 8080
    echo You may need to add a firewall exception
)
echo.

echo ================================================
echo RECOMMENDATIONS
echo ================================================
echo.
echo For Android Emulator:
echo   - Use IP: 10.0.2.2:8080
echo   - In build.gradle: buildConfigField "String", "BASE_URL", "\"http://10.0.2.2:8080/\""
echo.
echo For Physical Android Device (same WiFi):
echo   - Use one of the IPv4 addresses shown above
echo   - Example: buildConfigField "String", "BASE_URL", "\"http://YOUR_IP:8080/\""
echo   - Make sure to add the IP to network_security_config.xml
echo.
echo Backend Server Requirements:
echo   - Must be running on port 8080
echo   - Should bind to 0.0.0.0:8080 (not just 127.0.0.1:8080)
echo   - Firewall must allow inbound connections on port 8080
echo.

echo ================================================
echo To add firewall exception (Run as Administrator):
echo netsh advfirewall firewall add rule name="Backend API Port 8080" dir=in action=allow protocol=TCP localport=8080
echo ================================================
echo.

pause
