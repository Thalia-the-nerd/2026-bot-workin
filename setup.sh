#!/bin/bash
# 2026-bot-fork Cross-Platform Dependency Installer (Linux/macOS)

echo "================================================"
echo "Installing Robotics Dependencies (macOS/Linux)"
echo "================================================"

if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "[macOS] Installing via Homebrew..."
    if ! command -v brew &> /dev/null; then
        echo "Homebrew not found. Installing..."
        /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
    fi
    brew install python rust openjdk@17
else
    echo "[Linux] Installing via apt..."
    sudo apt-get update
    sudo apt-get install -y python3 python3-pip curl openjdk-17-jdk
    echo "[Linux] Installing Rustup..."
    curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y
    source "$HOME/.cargo/env"
fi

echo "Installing Python requirements..."
pip3 install numpy --break-system-packages 2>/dev/null || pip3 install numpy

echo "================================================"
echo "Success! The developer environment is now ready."
echo "================================================"
