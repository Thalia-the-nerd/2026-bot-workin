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

  private double m_targetPivotAngle = 0.0;
  private boolean m_isPivotDown = false;

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

  /** Toggles the pivot between up (0 degrees) and down (45 degrees). */
  public void togglePivot() {
    m_isPivotDown = !m_isPivotDown;
    m_targetPivotAngle = m_isPivotDown ? 45.0 : 0.0;
    m_io.setPivotAngle(m_targetPivotAngle);
  }

  /** Sets the pivot to a specific angle. */
  public void setPivotAngle(double degrees) {
    m_targetPivotAngle = degrees;
    m_io.setPivotAngle(degrees);
  }

  /** Stops the intake. */
  public void stop() {
    m_io.stop();
  }

  /** Stops the pivot motor. */
  public void stopPivot() {
    m_io.stopPivot();
  }

  @Override
  public void periodic() {
    m_io.updateInputs(m_inputs);
    Logger.processInputs("Intake", m_inputs);

    double current = m_inputs.mainMotorCurrentAmps;
    RobotTelemetry.putNumber("Intake Current (A)", current);
    RobotTelemetry.putNumber("Intake Pivot Angle", m_inputs.pivotPositionDeg);
    RobotTelemetry.putNumber("Intake Target Angle", m_targetPivotAngle);

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
      if (current > STALL_CURRENT_THRESHOLD && active) {
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
