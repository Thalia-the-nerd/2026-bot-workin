package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.IntakeSubsystem;
import java.util.function.DoubleSupplier;

/**
 * Default command for the IntakeSubsystem. It reads a fuzzy flight stick throttle and maps the
 * [-1.0, 1.0] range to [0.0, 1.0] motor speed.
 */
public class IntakeSliderCommand extends Command {
  private final IntakeSubsystem m_intakeSubsystem;
  private final DoubleSupplier m_throttleSupplier;

  /**
   * Creates a new IntakeSliderCommand.
   *
   * @param subsystem The intake subsystem.
   * @param throttleSupplier A supplier returning the raw throttle axis [-1.0 to 1.0].
   */
  public IntakeSliderCommand(IntakeSubsystem subsystem, DoubleSupplier throttleSupplier) {
    m_intakeSubsystem = subsystem;
    m_throttleSupplier = throttleSupplier;
    addRequirements(subsystem);
  }

  @Override
  public void initialize() {}

  @Override
  public void execute() {
    // Read the raw flight stick value which goes from -1.0 (down) to 1.0 (up)
    // Sometimes joysticks treat pushing forward as negative, so verify physically
    double rawValue = m_throttleSupplier.getAsDouble();

    // The user requested: "fuzzy switch so it goes from -1 to -0.999 all the way to 1
    // so just add 1 and that changes speed"
    // Mathematically: adding 1 makes the range [0.0, 2.0]. Then we divide by 2 to get [0.0, 1.0]
    double speed = (rawValue + 1.0) / 2.0;

    // Ensure we don't accidentally command outside the max speed bounds due to slight noise
    speed = Math.max(0.0, Math.min(1.0, speed));

    m_intakeSubsystem.setRunSpeed(speed);
  }

  @Override
  public void end(boolean interrupted) {
    m_intakeSubsystem.stop();
  }

  @Override
  public boolean isFinished() {
    // As a default command, it never finishes by itself
    return false;
  }
}
