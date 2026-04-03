# 2026-Bot Controls

This document serves as the master reference for all controller mappings and driver station interactions for both the Primary Driver and the Secondary Operator.

## Primary Driver (Xbox Controller - Port 0)

| Input | Action | Description |
|:---|:---|:---|
| **Left Stick Y-Axis** | Drive Left Sides | Controls the speed of the left track/wheels (Tank Drive). |
| **Right Stick Y-Axis** | Drive Right Sides | Controls the speed of the right track/wheels (Tank Drive). |
| **Left Bumper** | Toggle Precision Mode | Hold to limit maximum speed to 30% for fine adjustments. |
| **Right Bumper** | Toggle Brake/Coast | Toggles the drivetrain between Brake and Coast mode. |
| **Left Trigger** | Unjam / Reverse Intake | Reverses the intake run motor if a piece gets stuck. |
| **Right Trigger** | Fire Override | Allows the driver to shoot without the operator. |
| **Button A** | Toggle Intake | Enables the driver to quickly spin up or stop the intake run motor. |
| **Button Y** | Switch Queued Mode | Toggles the queued shooter state. |

## Secondary Operator (Flight Stick - Port 1)

| Input | Action | Description |
|:---|:---|:---|
| **X-Axis** | Turret Manual Control | Manually rotates the Turret left and right (Capped at 80% speed). |
| **Y-Axis** | Fire Speed | Pushing stick forward determines the target speed of the Fire motor. Can be overridden via `Regression Test Firing Speed Override` on SmartDashboard. |
| **Throttle Slider** | Unassigned | Previously Intake Speed. |
| **Trigger (Top)** | Fire Weapon | Activates feeder/kicker motor at speed determined by `Y-Axis`. Loader 1 & 2 feed at 100% speed. Also activates Loader 3. |
| **Button 2** | Toggle Auto Aim | Activates auto-aim mode for targeting. |
| **Button 6** | Toggle Intake | Toggles the intake run roller on/off at 100% speed. |
| **Button 7** | Toggle Loader | Toggles Loader 1 & 2 on/off at 100% speed. |
| **Button 8** | Unused | Previously Pivot Preset (removed). |
| **Button 9** | Intake Pivot UP | Manually runs the intake pivot motor in the UP direction. |
| **Button 10** | Intake Pivot DOWN | Manually runs the intake pivot motor in the DOWN direction. |
| **Button 12** | Emergency Unjam | Unjams the intake system. |
