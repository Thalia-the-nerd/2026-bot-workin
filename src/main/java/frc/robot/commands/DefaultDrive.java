// Copyright (c) Jack Nelson & Miami Beach Bots

package frc.robot.commands;

import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.constants.Constants;
import frc.robot.subsystems.DriveSubsystem;
import java.util.function.DoubleSupplier;

/** The default drive command that uses the drive subsystem. */
public class DefaultDrive extends Command {
  private final DriveSubsystem m_driveSubsystem;
  private final DoubleSupplier m_left_y; // this gives us the left y axis for current controller
  private final DoubleSupplier m_right_y; // this gives us the right y axis for current controller
  private final java.util.function.BooleanSupplier m_precision_mode; // Precision Mode Toggle

  private final SlewRateLimiter m_leftLimiter = new SlewRateLimiter(4.0);
  private final SlewRateLimiter m_rightLimiter = new SlewRateLimiter(4.0);

  /**
   * Creates a new DefaultDrive command.
   *
   * @param d_subsystem The drive subsystem used by this command.
   * @param shooterState The shooter state for queued modes
   * @param xbox_left_y A function that returns the value of the left y axis for the joystick.
   * @param xbox_right_y A function that returns the value of the right Y axis for the joystick.
   * @param precision_mode A function returning true if precision mode should be active.
   */
  public DefaultDrive(
      DriveSubsystem d_subsystem,
      DoubleSupplier xbox_left_y,
      DoubleSupplier xbox_right_y,
      java.util.function.BooleanSupplier precision_mode) {
    m_driveSubsystem = d_subsystem;
    m_left_y = xbox_left_y;
    m_right_y = xbox_right_y;
    m_precision_mode = precision_mode;
    // Use addRequirements() here to declare subsystem dependencies.
    addRequirements(d_subsystem);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {}

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    // we include a limit on the drivers speed for safety.
    m_driveSubsystem.setReducedSpeed(false);

    double leftRaw = m_left_y.getAsDouble();
    double rightRaw = m_right_y.getAsDouble();

    // Apply deadband individually to each axis
    double leftDeadbanded =
        edu.wpi.first.math.MathUtil.applyDeadband(leftRaw, Constants.CONTROLLER_DEAD_ZONE);
    double rightDeadbanded =
        edu.wpi.first.math.MathUtil.applyDeadband(rightRaw, Constants.CONTROLLER_DEAD_ZONE);

    if (leftDeadbanded != 0.0 || rightDeadbanded != 0.0) {
      this.m_driveSubsystem.tankDrive(
          Constants.MAX_SPEED * leftDeadbanded, Constants.MAX_SPEED * rightDeadbanded);
    } else {
      // Must explicitly stop if within deadzone or else motors will coast at last value
      this.m_driveSubsystem.tankDrive(0, 0);
    }
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    m_driveSubsystem.stop(); // We might not want this
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}
