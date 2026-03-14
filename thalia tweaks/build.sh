#!/bin/bash
# Build script for Thalia's Tweak GUI
mkdir -p build
# Create lib directory if they don't exist
mkdir -p libs/linux
cp *.so libs/linux/ 2>/dev/null || true

echo "Compiling with WPILib NetworkTables 4 C++ Client..."
g++ -O3 -std=c++20 main.cpp -o thalia_tweaks_gui \
    -I libs/headers \
    -Wl,-rpath=./libs/linux -L libs/linux -lntcore -lwpiutil \
    $(pkg-config --cflags --libs gtk+-3.0)

if [ $? -eq 0 ]; then
    echo "Build successful! Run with: ./thalia_tweaks_gui"
else
    echo "Build failed! Ensure you have libgtk-3-dev installed and NT4 libs downloaded."
fi
