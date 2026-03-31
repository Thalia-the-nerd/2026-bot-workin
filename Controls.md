# 2026-Bot Controls

This document serves as the master reference for all controller mappings and driver station interactions for both the Primary Driver and the Secondary Operator.

## Primary Driver (Xbox Controller - Port 0)

| Input | Action | Description |
|:---|:---|:---|
| **Left Stick Y-Axis** | Drive Left Sides | Controls the speed of the left track/wheels (Tank Drive). |
| **Right Stick Y-Axis** | Drive Right Sides | Controls the speed of the right track/wheels (Tank Drive). |
| **Left Bumper** | Toggle Precision Mode | Hold to limit maximum speed to 30% for fine adjustments. |
| **Right Bumper** | Toggle Brake/Coast | Toggles the drivetrain between Brake and Coast mode. |
| **Left Trigger** | Unjam / Reverse Intake | Reverses the intake if a piece gets stuck. |
| **Right Trigger** | Fire Override | Allows the driver to shoot without the operator. |
| **Button A** | Toggle Intake | Enables the driver to quickly spin up or stop the intake. |
| **Button Y** | Switch Queued Mode | Toggles the queued shooter state. |

## Secondary Operator (Flight Stick - Port 1)

| Input | Action | Description |
|:---|:---|:---|
| **X-Axis** | Turret Manual Control | Manually rotates the Turret left and right (Capped at 80% speed). |
| **Y-Axis** | Loader Speed | Pushing stick forward spins Loader 1 & 2 inward. |
| **Throttle Slider** | Intake Speed | Automatically scales the Intake speed. |
| **Trigger (Top)** | Fire Weapon | Activates feeder/kicker motor at configured `Regression Test Firing Speed` (Default 100%). Also activates Loader 3. |
| **Button 2** | Toggle Auto Aim | Activates auto-aim mode for targeting. |
| **Button 6** | Manual Intake | Spins the intake at 100% speed. |
| **Button 7** | Manual Loader | Spins Loader 1 & 2 at 100% speed. |
| **Buttons 8-11** | Turret Presets | Rotates the turret to predefined angular positions (0, 45, 90, 180 degrees). |
| **Button 12** | Emergency Unjam | Unjams the intake system. |
