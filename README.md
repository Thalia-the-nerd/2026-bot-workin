# 2026-bot

> *"Aut viam inveniam aut faciam."* — I'll either find a way or make one.

**Team:** Miami Beach Bots (FRC Team 2026)  
**Season:** 2025-2026

## 📊 Project Status

**Current State:** Pre-Alpha / In Development

- ✅ Swerve drive base initialized with YAGSL
- ✅ Basic project structure and dependencies configured
- ⚠️ Swerve base untested
- ⏳ Major subsystems pending implementation

## 🚀 Quick Start

### Prerequisites

Ensure you have the following installed before setting up the project:

- **Java Development Kit (JDK):** Version 17 or higher
- **WPILib Suite:** 2025 release
- **Git:** For version control

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/MiamiBeachBots/2026-bot.git
   cd 2026-bot
   ```

2. **Build the project:**
   ```bash
   ./gradlew build
   ```
   This command downloads all required vendor dependencies and compiles the code.

### Vendor Dependencies

This project uses the following vendor libraries (automatically managed via `vendordeps/`):

- **Phoenix 6** (CTRE) - Motor controllers and sensors
- **Phoenix 5** (CTRE) - Legacy CTRE devices
- **REVLib** (REV Robotics) - Spark MAX motor controllers
- **ReduxLib** - Additional utilities
- **YAGSL** (Yet Another Generic Swerve Library) - Swerve drive framework
- **Studica** - Additional hardware support
- **ThriftyLib** - Encoder support
- **Maple-Sim** - Simulation utilities

## 🔨 Build & Deploy

### Building the Code

Compile the robot code locally:

```bash
./gradlew build
```

### Deploying to Robot

1. **Connect to the robot** via WiFi (10.20.26.1) or Ethernet
2. **Deploy the code:**
   ```bash
   ./gradlew deploy
   ```

### Running Simulation

Test the code without a physical robot:

```bash
./gradlew simulateJava
```

## 🗺️ Project Structure

```
2026-bot/
├── src/main/java/frc/robot/    # Robot source code
│   ├── Robot.java              # Main robot class
│   ├── RobotContainer.java     # Command and subsystem initialization
│   └── subsystems/             # Robot subsystems
├── src/main/deploy/            # Configuration files (YAGSL configs, etc.)
├── vendordeps/                 # Vendor dependency JSON files
├── build.gradle                # Gradle build configuration
└── README.md                   # This file
```

## 📚 Documentation

- **[Contributing Guide](Contribguide.md)** - How to contribute to this project
- **[Style Guide](styleguide.md)** - Code formatting and naming conventions
- **[Commit Guide](commitguide.md)** - Git commit message standards

## 🛠️ Development

### Setting Up Your Environment

**For VS Code Users:**
1. Install the WPILib 2025 suite
2. Open this project folder in VS Code
3. Accept the prompt to import the Gradle project

**For CLI Users:**
1. Ensure JDK 17+ is installed
2. Use `./gradlew build` to compile
3. Use `./gradlew deploy` to deploy to robot

### Testing

Run unit tests (when available):

```bash
./gradlew test
```

## 🗺️ CAN Bus Map

> **TODO:** Hardware configuration pending

## 📋 Roadmap & TODO

- [ ] **Hardware Integration**
  - [ ] Verify swerve module CAN IDs and configurations
  - [ ] Test individual drive motors
  - [ ] Calibrate module offsets
- [ ] **Subsystems**
  - [ ] Complete swerve drive testing
  - [ ] Implement additional mechanisms (TBD based on game)
- [ ] **Autonomous**
  - [ ] Configure PathPlanner
  - [ ] Develop autonomous routines
- [ ] **Vision & Coprocessor**
  - [ ] Set up vision processing (Limelight/PhotonVision)
  - [ ] Implement AprilTag tracking
- [ ] **Driver Station**
  - [ ] Configure controller mappings
  - [ ] Set up driver feedback systems
- [ ] **Documentation**
  - [ ] Complete CAN bus map
  - [ ] Document electrical connections
  - [ ] Add subsystem documentation

## 📄 License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

## 🤝 Contributing

We welcome contributions! Please read our [Contributing Guide](Contribguide.md) and [Style Guide](styleguide.md) before submitting pull requests.

## 👥 Team

**Miami Beach Bots** - FRC Team 2026  
Miami Beach, Florida

---

*Built with ☕ and ⚙️ by Miami Beach Bots*
