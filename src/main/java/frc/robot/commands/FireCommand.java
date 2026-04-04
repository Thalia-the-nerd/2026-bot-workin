package frc.robot.commands;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.FireControlSubsystem;
import frc.robot.subsystems.LoaderSubsystem;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

public class FireCommand extends Command {
  private final FireControlSubsystem m_fireSubsystem;
  private final LoaderSubsystem m_loaderSubsystem;
  private final DoubleSupplier m_speedSupplier;
  private final BooleanSupplier m_triggerHeldSupplier;

  private final double MAX_RPM = 5000.0; // Assume max RPM is roughly 5000 for a NEO

  /**
   * Creates a new FireCommand that spins up the flywheels based on trigger depth, and only feeds
   * via the Loader when the flywheel is at the target RPM.
   */
  public FireCommand(
      FireControlSubsystem fireSubsystem,
      LoaderSubsystem loaderSubsystem,
      DoubleSupplier speedSupplier,
      BooleanSupplier triggerHeldSupplier) {
    m_fireSubsystem = fireSubsystem;
    m_loaderSubsystem = loaderSubsystem;
    m_speedSupplier = speedSupplier;
    m_triggerHeldSupplier = triggerHeldSupplier;

    // Use addRequirements() here to declare subsystem dependencies.
    addRequirements(fireSubsystem, loaderSubsystem);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    // Initialization handled by execute on a per-tick basis
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    // Math.abs to ensure positive velocity based on Y-axis
    double rawSpeed = Math.abs(m_speedSupplier.getAsDouble());
    // Apply a deadband to ignore slightly noisy inputs, then clamp between 0 and 1
    double speed = MathUtil.clamp(MathUtil.applyDeadband(rawSpeed, 0.1), 0.0, 1.0);

    double targetRPM = speed * frc.robot.constants.SpeedConstants.FIRE_MAX_SPEED;
    m_fireSubsystem.setShooterRPM(targetRPM);

    // Only feed if target RPM is > 0 and the flywheels have reached the target
    if (targetRPM > 100 && m_fireSubsystem.isAtRPM(targetRPM, 50.0)) {
      m_loaderSubsystem.setLoaderSpeed(1.0); // Full speed feed
    } else {
      m_loaderSubsystem.stop();
    }
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    // Finish immediately when trigger is released
    return !m_triggerHeldSupplier.getAsBoolean();
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    // Stop all motors when the command finishes
    m_fireSubsystem.stop();
    m_loaderSubsystem.stop();
  }
}
