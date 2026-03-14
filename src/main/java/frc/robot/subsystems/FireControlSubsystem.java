package frc.robot.subsystems;

import com.revrobotics.RelativeEncoder;
import com.revrobotics.spark.SparkBase.ControlType;
import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.RobotTelemetry;
import frc.robot.constants.Constants.CANConstants;

public class FireControlSubsystem extends SubsystemBase {
  private final SparkMax m_fireMotor;
  private final SparkMaxConfig m_config;
  private final SparkClosedLoopController m_pidController;
  private final RelativeEncoder m_encoder;

  @SuppressWarnings("removal")
  public FireControlSubsystem() {
    m_fireMotor = new SparkMax(CANConstants.MOTOR_FIRE_ID, MotorType.kBrushless);
    m_config = new SparkMaxConfig();

    // Safety Limits
    m_config.smartCurrentLimit(40);

    // Initial PID configs (to be tuned later)
    m_config.closedLoop.pid(0.0001, 0, 0);
    m_config.closedLoop.outputRange(0, 1.0); // positive RPM only

    m_fireMotor.configure(m_config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

    m_pidController = m_fireMotor.getClosedLoopController();
    m_encoder = m_fireMotor.getEncoder();
  }

  /**
   * Sets the shooter to a specific target RPM.
   *
   * @param targetRPM The target RPM for the flywheel.
   */
  public void setShooterRPM(double targetRPM) {
    if (targetRPM <= 0) {
      stop();
      return;
    }
    m_pidController.setReference(
        targetRPM, ControlType.kVelocity, com.revrobotics.spark.ClosedLoopSlot.kSlot0);
  }

  /**
   * Checks if the flywheel is at the target RPM within a given tolerance.
   *
   * @param targetRPM The target RPM.
   * @param tolerance The allowed RPM difference.
   * @return True if the RPM is within the tolerance.
   */
  public boolean isAtRPM(double targetRPM, double tolerance) {
    double currentRPM = m_encoder.getVelocity();
    return Math.abs(currentRPM - targetRPM) <= tolerance;
  }

  /** Stops the fire motor. */
  public void stop() {
    m_fireMotor.set(0);
  }

  @Override
  public void periodic() {
    // Debugging current fire motor speed and RPM
    RobotTelemetry.putNumber("Fire Motor Speed Output", m_fireMotor.get());
    RobotTelemetry.putNumber("Fire Motor RPM", m_encoder.getVelocity());
  }

  @Override
  public void simulationPeriodic() {
    // Broadcast for Python App
    RobotTelemetry.putBoolean("Sim_IsFiring", Math.abs(m_fireMotor.get()) > 0.1);
  }
}
