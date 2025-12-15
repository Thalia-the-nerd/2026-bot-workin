# Commit Guide

## Message Structure

We use a simplified Conventional Commits format:

```
[Type] Description
```

### Allowed Types

| Type | When to Use | Example |
|------|-------------|---------|
| `[Feat]` | New feature or capability | `[Feat] Add field-oriented drive` |
| `[Fix]` | Bug fix or correction | `[Fix] Correct module 3 encoder offset` |
| `[Refactor]` | Code cleanup without changing behavior | `[Refactor] Simplify PID controller logic` |
| `[Docs]` | Documentation changes only | `[Docs] Update wiring diagram` |
| `[Chore]` | Build, deps, or tool updates | `[Chore] Update Phoenix6 to v25.0.0` |
| `[Test]` | Adding or fixing tests | `[Test] Add unit tests for shooter` |
| `[Perf]` | Performance improvements | `[Perf] Optimize path planning algorithm` |
| `[Style]` | Formatting, whitespace, naming | `[Style] Format DriveSubsystem.java` |
| `[Build]` | Build system or dependency changes | `[Build] Update Gradle to 8.5` |
| `[CI]` | CI/CD configuration changes | `[CI] Add automated testing workflow` |
| `[Revert]` | Reverting a previous commit | `[Revert] Undo turret control changes` |
| `[Config]` | Robot or YAGSL configuration files | `[Config] Update swerve module offsets` |

## Writing the Description

### Use Imperative Mood

✅ **Good:**
- `[Feat] Add vision processing`
- `[Fix] Invert left drive motors`
- `[Refactor] Extract constants to Constants.java`

❌ **Bad:**
- `[Feat] Added vision processing` (past tense)
- `[Feat] Adds vision processing` (present tense)
- `[Fix] fixing motors` (gerund, lowercase)

### Be Specific But Concise

- **Keep it under 50 characters** when possible
- Say *what* changed, not *how* you changed it
- Name the component if it's not obvious

✅ **Good:**
- `[Fix] Correct inverted shooter motor direction`
- `[Feat] Implement auto-balance command`

❌ **Bad:**
- `[Fix] Fix bug` (too vague)
- `[Feat] Add code that makes the robot auto-balance by using gyro pitch data to drive backwards or forwards` (way too long)

### Add Context When Needed

For complex changes, add a blank line after the subject and write a body paragraph:

```
[Feat] Implement vision-based targeting

Added AprilTag detection using PhotonVision. The turret now
auto-aims at detected tags within 4 meters. Falls back to
manual control if no tags are visible.
```

## Examples

```
[Feat] Add swerve drive kinematics
[Feat] Implement intake subsystem
[Feat] Create 3-piece auto routine
[Fix] Correct module angle offsets
[Fix] Resolve brownout during arm extension
[Refactor] Move magic numbers to Constants.java
[Refactor] Consolidate PID tuning methods
[Docs] Add CAN bus wiring diagram
[Docs] Update build instructions for 2025 season
[Chore] Update YAGSL to latest version
[Chore] Add REVLib vendor dependency
[Test] Add trajectory following unit tests
[Perf] Reduce loop cycle time in autonomous
[Style] Apply standard formatting to all subsystems
[Build] Migrate to Gradle 8.5
[CI] Add deploy-on-push workflow
[Revert] Undo experimental arm kinematics
[Config] Update Phoenix tuner CAN IDs
```

## Atomic Commits

Do not bundle multiple unrelated changes into one commit. Each commit should represent one logical change.

❌ **Bad:**
```
[Feat] Add shooter subsystem and fix drive motor inversions
```
This is two unrelated changes. Split them up.

✅ **Good:**
```
[Fix] Invert left-side drive motors
[Feat] Add shooter subsystem
```

If the shooter code has bugs, you can revert it without losing the motor fix.

# "History is a set of lies agreed upon" — Napoleon Bonaparte
