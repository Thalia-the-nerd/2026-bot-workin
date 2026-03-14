package frc.robot.subsystems;

import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.RobotTelemetry;
import frc.robot.constants.Constants.CANConstants;
import frc.robot.constants.SpeedConstants;

/** Subsystem handling the intake/loading system. */
public class IntakeSubsystem extends SubsystemBase {

  private final SparkMax m_intakeMotorMain;
  private final SparkMax m_intakeMotorSecondary;
  private final SparkMaxConfig m_config;

  private boolean m_isStalled = false;
  private final Timer m_stallTimer = new Timer();
  private static final double STALL_CURRENT_THRESHOLD = 30.0; // Amps
  private static final double STALL_TIME_THRESHOLD = 0.5; // Seconds to consider perfectly stalled
  private static final double REVERSE_TIME = 1.0; // Seconds to reverse after a stall

  @SuppressWarnings("removal")
  public IntakeSubsystem() {
    m_intakeMotorMain = new SparkMax(CANConstants.MOTOR_INTAKE_MAIN_ID, MotorType.kBrushless);
    m_intakeMotorSecondary =
        new SparkMax(CANConstants.MOTOR_INTAKE_SECONDARY_ID, MotorType.kBrushless);
    m_config = new SparkMaxConfig();

    // Default to Coast mode or Brake mode depending on team preference.
    // Usually intakes run in Coast so balls/notes aren't crushed on stop.
    m_config.idleMode(SparkMaxConfig.IdleMode.kCoast);

    // Hardware smoothing to limit sharp spikes
    m_config.openLoopRampRate(0.25);

    m_intakeMotorMain.configure(
        m_config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

    // Apply config to secondary motor and follow main motor
    m_config.follow(m_intakeMotorMain);
    m_intakeMotorSecondary.configure(
        m_config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

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
    m_intakeMotorMain.set(adjustedSpeed);
  }

  /** Stops the intake. */
  public void stop() {
    m_intakeMotorMain.stopMotor();
  }

  @Override
  public void periodic() {
    double current = m_intakeMotorMain.getOutputCurrent();
    RobotTelemetry.putNumber("Intake Current (A)", current);

    if (m_isStalled) {
      if (m_stallTimer.hasElapsed(REVERSE_TIME)) {
        m_isStalled = false;
        stop();
      } else {
        // Reverse motor to clear jam
        m_intakeMotorMain.set(-0.5);
      }
    } else {
      if (current > STALL_CURRENT_THRESHOLD && Math.abs(m_intakeMotorMain.get()) > 0.1) {
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
