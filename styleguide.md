# Code Style Guide

This guide defines the coding standards for the 2026 Robot codebase. Following these conventions ensures consistency, readability, and maintainability across the team.

---

## 1. Naming Conventions

We follow standard Java naming conventions. **Consistency is critical** for readability and collaboration.

### Classes and Interfaces
* **Format:** `PascalCase`
* **Examples:**
  * `SwerveSubsystem`
  * `RobotContainer`
  * `ArmPositionState` (enum)
  * `AutoCommandFactory`

### Methods and Variables
* **Format:** `camelCase`
* **Examples:**
  * Methods: `drive()`, `getPose()`, `resetOdometry()`
  * Variables: `targetVelocity`, `currentPosition`, `xVelocity`

### Constants
* **Format:** `SCREAMING_SNAKE_CASE`
* **Scope:** Use `static final` and keep in `Constants.java`
* **Examples:**
  * `MAX_SPEED_METERS_PER_SECOND`
  * `DRIVE_GEAR_RATIO`
  * `ARM_PIVOT_KP`

### Local Variables
* **Keep short but descriptive.**
* **Loop counters:** Single letters are acceptable (`i`, `j`, `k`)
* **Object references:** Use meaningful names
  * ✅ `moduleState`, `targetPose`, `translation`
  * ❌ `data`, `temp`, `thing`, `x`

---

## 2. Formatting

### Indentation
* **Use 2 spaces** (WPILib/GradleRIO standard)
* **No tabs.** Configure your editor to use spaces.

### Braces
* **K&R style:** Opening brace on the same line.
* **Always use braces,** even for single-line statements.

```java
if (condition) {
  doSomething();
} else {
  doSomethingElse();
}

// Even for single statements
if (robotEnabled) {
  run();
}
```

### Line Length
* **Aim for 100-120 characters max.**
* Break long method chains or expressions into multiple lines for readability.

```java
// Good
drivebase.drive(
  new Translation2d(yVelocity * maximumSpeed, xVelocity * maximumSpeed),
  rotation * Math.PI,
  true
);
```

### Imports
* **No wildcards.** Import explicit classes.
  * ✅ `import edu.wpi.first.math.geometry.Translation2d;`
  * ❌ `import edu.wpi.first.math.geometry.*;`
* **Remove unused imports** before committing.
* Group imports logically (WPILib, vendor libs, java.*, then local).

---

## 3. Documentation

### Javadocs
* **Required for:**
  * All `public` classes and interfaces
  * Public methods in subsystems
  * Complex or non-obvious methods
* **Format:**

```java
/**
 * The RobotContainer class is responsible for instantiating and configuring
 * all robot subsystems, setting up controller bindings, and managing the
 * default and autonomous commands.
 */
public class RobotContainer {
  // ...
}

/**
 * Drives the robot using field-relative control.
 *
 * @param translation The desired X/Y velocity (meters/second)
 * @param rotation The desired rotational velocity (radians/second)
 * @param fieldRelative True for field-oriented, false for robot-oriented
 */
public void drive(Translation2d translation, double rotation, boolean fieldRelative) {
  // ...
}
```

### Inline Comments
* **Explain *why*, not *what*.**
* Code should be self-documenting. Comments clarify intent or reasoning.

```java
// ❌ BAD: Describes what the code does (obvious)
// Set speed to 0
motorSpeed = 0;

// ✅ GOOD: Explains why
// Stop motors to prevent brownout during initialization
motorSpeed = 0;

// ✅ GOOD: Clarifies non-obvious logic
// Invert Y-axis because WPILib uses forward-positive, joystick is forward-negative
double yVelocity = -MathUtil.applyDeadband(controller.getLeftY(), 0.1);
```

### TODO Comments
* Use `TODO:` for incomplete or future work.
* Include your initials or GitHub username if possible.

```java
// TODO(jdoe): Implement PID tuning mode
// TODO: Add current limiting to prevent breaker trips
```

---

## 4. Code Organization

### File Structure
Follow the standard WPILib project layout:

```
src/main/java/frc/robot/
├── Robot.java              # Main robot class
├── RobotContainer.java     # Subsystem/command initialization
├── Constants.java          # Physical constants, IDs, PID gains
├── commands/               # Command classes
│   ├── TeleopDrive.java
│   └── AutoAlign.java
└── subsystems/             # Subsystem classes
    ├── SwerveSubsystem.java
    ├── ArmSubsystem.java
    └── IntakeSubsystem.java
```

### Constants.java
* **Centralize all magic numbers** (motor IDs, PID values, physical dimensions).
* Use nested classes for organization.

```java
public final class Constants {
  public static final class DriveConstants {
    public static final double MAX_SPEED_METERS_PER_SECOND = 4.5;
    public static final double TRACK_WIDTH_METERS = 0.6;
  }

  public static final class CanIDs {
    public static final int FRONT_LEFT_DRIVE = 1;
    public static final int FRONT_LEFT_TURN = 2;
  }
}
```

### Subsystems
* Extend `SubsystemBase`
* Keep subsystems **hardware-agnostic** when possible.
* Use `periodic()` for continuous updates (odometry, telemetry).

### Commands
* Extend `Command` or use factory methods (`Commands.run()`, `Commands.runOnce()`).
* Declare subsystem requirements explicitly.

---

## 5. Best Practices

### Avoid Magic Numbers
* **Never hardcode values in logic.**

```java
// ❌ BAD
motor.set(0.7);

// ✅ GOOD
motor.set(ArmConstants.HOLD_POWER);
```

### Use Deadbands for Joysticks
* Apply `MathUtil.applyDeadband()` to prevent drift.

```java
double xVelocity = -MathUtil.applyDeadband(controller.getLeftX(), 0.1);
```

### Exception Handling
* Fail fast with meaningful error messages.

```java
try {
  swerveDrive = new SwerveParser(directory).createSwerveDrive(maximumSpeed);
} catch (Exception e) {
  throw new RuntimeException("CRITICAL: YAGSL failed to load. Check JSON paths.\n" + e.getMessage());
}
```

### Units
* Use `edu.wpi.first.math.util.Units` for conversions.

```java
double radiansPerSecond = Units.degreesToRadians(180);
```

---

## 6. Version Control

### Commit Messages
* See [`commitguide.md`](commitguide.md) for detailed rules.
* **Quick reference:**
  * Use imperative mood: `[Feat] Add vision processing` (not "Added")
  * Format: `[Type] Description`
  * Types: `Feat`, `Fix`, `Refactor`, `Docs`, `Chore`

### Before Committing
* **Remove commented-out code.** Use version control, not comments, to track history.
* **Remove unused imports.**
* **Test your changes** (build, deploy, or simulate).

---

## 7. Additional Resources

* [WPILib Documentation](https://docs.wpilib.org/)
* [YAGSL Wiki](https://github.com/BroncBotz3481/YAGSL)
* [Contributing Guide](Contribguide.md)

---

**Remember:** Clean code is a team effort. When in doubt, match the style of the existing codebase. If you see something inconsistent, open a discussion or PR to fix it.
