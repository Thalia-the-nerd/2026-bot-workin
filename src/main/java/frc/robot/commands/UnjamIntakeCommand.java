package frc.robot.commands;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.IntakeSubsystem;

/**
 * Overrides the Intake slider to rapidly run the intake backwards for 2 seconds to unjam a
 * note/ball.
 */
public class UnjamIntakeCommand extends Command {
  private final IntakeSubsystem m_intakeSubsystem;
  private final Timer m_timer;
  private static final double UNJAM_SPEED = -1.0; // Reverse full speed
  private static final double UNJAM_DURATION = 2.0; // 2 seconds

  /**
   * Creates a new UnjamIntakeCommand.
   *
   * @param subsystem The intake subsystem used by this command.
   */
  public UnjamIntakeCommand(IntakeSubsystem subsystem) {
    m_intakeSubsystem = subsystem;
    m_timer = new Timer();
    addRequirements(subsystem);
  }

  @Override
  public void initialize() {
    m_timer.restart();
  }

  @Override
  public void execute() {
    // Force the intake into reverse regardless of the flight stick throttle position
    m_intakeSubsystem.setRunSpeed(UNJAM_SPEED);
  }

  @Override
  public void end(boolean interrupted) {
    m_intakeSubsystem.stop();
    m_timer.stop();
  }

  @Override
  public boolean isFinished() {
    // Command finishes automatically when 2 seconds have elapsed
    return m_timer.hasElapsed(UNJAM_DURATION);
  }
}
