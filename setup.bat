@echo off
echo ================================================
echo Installing Robotics Dependencies (Windows)
echo ================================================

echo [Windows] Installing via winget...
winget install -e --id Rustlang.Rustup --accept-source-agreements --accept-package-agreements
winget install -e --id Python.Python.3.11 --accept-source-agreements --accept-package-agreements
winget install -e --id Microsoft.OpenJDK.17 --accept-source-agreements --accept-package-agreements

echo Installing Python requirements...
pip install numpy

echo ================================================
echo Success! The developer environment is now ready.
echo ================================================
pause
