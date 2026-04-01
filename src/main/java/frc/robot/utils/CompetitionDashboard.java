package frc.robot.utils;

import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import frc.robot.subsystems.DriveSubsystem;

public class CompetitionDashboard {
  private static final ShuffleboardTab compTab = Shuffleboard.getTab("Competition");

  public static void setup(SendableChooser<String> autoChooser, DriveSubsystem drive) {
    // Add Autonomous Selection
    if (autoChooser != null) {
      compTab
          .add("Auto Routine", autoChooser)
          .withWidget(BuiltInWidgets.kComboBoxChooser)
          .withSize(2, 1)
          .withPosition(0, 0);
    }

    // Add Drive/Gyro Info
    compTab
        .addBoolean("Gyro Connected", () -> drive.getRotation2d() != null)
        .withSize(1, 1)
        .withPosition(2, 0);

    compTab
        .addNumber("Robot Yaw", () -> drive.getYaw())
        .withWidget(BuiltInWidgets.kDial)
        .withSize(2, 2)
        .withPosition(3, 0);

    compTab
        .addNumber("Speed", () -> Math.abs(drive.getSpeeds().vxMetersPerSecond))
        .withWidget(BuiltInWidgets.kNumberBar)
        .withSize(2, 1)
        .withPosition(0, 1);

    // General Robot States
    compTab
        .addBoolean("Ready To Fire", () -> true) // Could hook up to FireCommand / Subsystem
        .withWidget(BuiltInWidgets.kBooleanBox)
        .withSize(1, 1)
        .withPosition(2, 1);
  }
}
