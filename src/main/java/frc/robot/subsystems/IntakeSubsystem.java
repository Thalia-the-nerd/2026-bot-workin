package frc.robot.subsystems;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.RobotTelemetry;
import frc.robot.constants.SpeedConstants;
import org.littletonrobotics.junction.Logger;

/** Subsystem handling the intake/loading system. */
public class IntakeSubsystem extends SubsystemBase {

  private final IntakeIO m_io;
  private final IntakeIOInputsAutoLogged m_inputs = new IntakeIOInputsAutoLogged();

  private boolean m_isStalled = false;
  private final Timer m_stallTimer = new Timer();
  private static final double STALL_CURRENT_THRESHOLD = 30.0; // Amps
  private static final double STALL_TIME_THRESHOLD = 0.5; // Seconds to consider perfectly stalled
  private static final double REVERSE_TIME = 1.0; // Seconds to reverse after a stall

  @SuppressWarnings("removal")
  public IntakeSubsystem(IntakeIO io) {
    m_io = io;
    m_stallTimer.start();
  }

  /**
   * Sets the speed of the intake motor.
   *
   * @param speed Speed from -1.0 to 1.0. positive spins intake inward.
   */
  public void setIntakeSpeed(double speed) {
    if (m_isStalled) return; // Prevent normal operation if clearing a jam

    double adjustedSpeed =
        SpeedConstants.adjustSpeed(
            speed, SpeedConstants.INTAKE_MAIN_MAX_SPEED, SpeedConstants.INTAKE_MAIN_SENSITIVITY);
    m_io.setVoltage(adjustedSpeed * 12.0);
  }

  /** Stops the intake. */
  public void stop() {
    m_io.stop();
  }

  @Override
  public void periodic() {
    m_io.updateInputs(m_inputs);
    Logger.processInputs("Intake", m_inputs);

    double current = m_inputs.mainMotorCurrentAmps;
    RobotTelemetry.putNumber("Intake Current (A)", current);

    if (m_isStalled) {
      if (m_stallTimer.hasElapsed(REVERSE_TIME)) {
        m_isStalled = false;
        stop();
      } else {
        // Reverse motor to clear jam
        m_io.setVoltage(-6.0);
      }
    } else {
      boolean active = Math.abs(m_inputs.mainMotorAppliedVolts) > 1.2;
      if (frc.robot.constants.TweakConstants.ENABLE_STALL_DETECTION
          && current > STALL_CURRENT_THRESHOLD
          && active) {
        if (m_stallTimer.hasElapsed(STALL_TIME_THRESHOLD)) {
          // Jam detected!
          m_isStalled = true;
          m_stallTimer.restart();
          RobotTelemetry.putBoolean("Intake Jammed", true);
        }
      } else {
        m_stallTimer.restart();
        RobotTelemetry.putBoolean("Intake Jammed", false);
      }
    }
  }
}
