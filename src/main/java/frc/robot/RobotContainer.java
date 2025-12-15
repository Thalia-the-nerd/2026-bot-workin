package frc.robot;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.subsystems.SwerveSubsystem;
import java.io.File;

public class RobotContainer {

  // The Subsystem
  private final SwerveSubsystem drivebase = new SwerveSubsystem(new File(Filesystem.getDeployDirectory(), "swerve"));

  // The Controller
  final CommandXboxController driverXbox = new CommandXboxController(0);

  // The Auto Chooser
  private final SendableChooser<Command> autoChooser;

  public RobotContainer() {
    // 1. Register Named Commands (for Event Markers in PathPlanner)
    // NamedCommands.registerCommand("shoot", new ShootCommand());

    // 2. Build the Auto Chooser
    // This loads all .path and .auto files from the deploy directory
    autoChooser = AutoBuilder.buildAutoChooser();
    SmartDashboard.putData("Auto Chooser", autoChooser);

    configureBindings();
  }

  private void configureBindings() {
    // Default Teleop Drive
    drivebase.setDefaultCommand(
        Commands.run(
            () -> {
                double yVelocity = -MathUtil.applyDeadband(driverXbox.getLeftY(), 0.1); 
                double xVelocity = -MathUtil.applyDeadband(driverXbox.getLeftX(), 0.1); 
                double rotation  = -MathUtil.applyDeadband(driverXbox.getRightX(), 0.1);

                // Drive method
                drivebase.drive(
                    new Translation2d(yVelocity * drivebase.maximumSpeed, xVelocity * drivebase.maximumSpeed), 
                    // Hardcoded rotation speed to bypass API versioning issues
                    rotation * Math.PI, 
                    true 
                );
            },
            drivebase
        )
    );

    // Zero Gyro
    driverXbox.back().onTrue(Commands.runOnce(drivebase.getSwerveDrive()::zeroGyro));
  }

  public Command getAutonomousCommand() {
    return autoChooser.getSelected();
  }
}
