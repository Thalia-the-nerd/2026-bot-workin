package frc.robot;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.subsystems.SwerveSubsystem;
import java.io.File;
import edu.wpi.first.wpilibj.Filesystem;

/**
 * The RobotContainer class is responsible for instantiating and configuring
 * all robot subsystems, setting up controller bindings, and managing the
 * default and autonomous commands. This class serves as the central hub
 * for organizing the robot's command-based structure.
 */
public class RobotContainer {

  // The Subsystem
  // We use the "swerve" directory we deployed earlier
  private final SwerveSubsystem drivebase = new SwerveSubsystem(new File(Filesystem.getDeployDirectory(), "swerve"));

  // The Controller (Port 0 is usually the first USB controller plugged in)
  final CommandXboxController driverXbox = new CommandXboxController(0);

  public RobotContainer() {
    configureBindings();
  }

  private void configureBindings() {
    // DEFAULT COMMAND: The "Idle" state of the robot.
    // If no other button is pressed, do this.
    drivebase.setDefaultCommand(
        // We create a "RunCommand" (runs repeatedly)
        Commands.run(
            () -> {
                // 1. Get Joystick Inputs (Inverted because Y is up-negative in computer graphics)
                // MathUtil.applyDeadband ignores tiny drift when the stick is centered
                double yVelocity = -MathUtil.applyDeadband(driverXbox.getLeftY(), 0.1); 
                double xVelocity = -MathUtil.applyDeadband(driverXbox.getLeftX(), 0.1); 
                double rotation  = -MathUtil.applyDeadband(driverXbox.getRightX(), 0.1);

                // 2. Drive
                drivebase.drive(
                    new Translation2d(yVelocity * drivebase.maximumSpeed, xVelocity * drivebase.maximumSpeed),
                    rotation * Math.PI,
                    true // Field Relative (True = Standard, False = Robot Oriented)
                );
            },
            drivebase // REQUIRE the subsystem so no other command can interrupt this one
        )
    );

    // Map "Back" button to zero the gyro (reset field orientation)
    driverXbox.back().onTrue(Commands.runOnce(drivebase.getSwerveDrive()::zeroGyro, drivebase));
  }

  /**
   * Returns the command to run during the autonomous period.
   *
   * @return the autonomous command to execute
   */
  public Command getAutonomousCommand() {
    return Commands.print("No Auto Configured Yet!");
  }
}
