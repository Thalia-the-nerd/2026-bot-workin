# Project Cerberus Deployment Guide

## 1. Automated Environment Bootstrap
To ensure absolute parity across every developer workstation, automated setup scripts have been written for macOS, Windows, and Linux.

### Windows (Winget)
Execute `setup.bat` as an Administrator. This will identically provision:
- The WPILib Java 17 toolchain
- Python 3.11 with `numpy` arrays
- Rustup (`cargo` and `rustc`) via native Microsoft Winget protocols.

### macOS & Linux (Bash)
Execute `./setup.sh` in the repository root.
- **macOS:** Installs core compilers via Homebrew.
- **Linux / WPILib Pi System:** Installs equivalents via APT.

## 2. Deploying to the Robot
Once the environment is successfully cloned and provisioned, execute the WPILib build cycle:
1. Turn on the robot and connect to the local radio.
2. Ensure you are deeply connected (check ping to `10.TE.AM.2`).
3. Run the deployment wrapper: `./gradlew deploy` (or `gradlew.bat deploy` on Windows).
4. Run `./thalia_tweaks_gui` natively via Rust (`cd thalia-tweaks && cargo run`) to synchronize tuning parameters in real-time.
