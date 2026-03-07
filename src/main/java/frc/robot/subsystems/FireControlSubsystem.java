package frc.robot.subsystems;

import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.RobotTelemetry;
import frc.robot.constants.Constants.CANConstants;
import frc.robot.constants.SpeedConstants;

public class FireControlSubsystem extends SubsystemBase {
  private final SparkMax m_fireMotor;
  private final SparkMaxConfig m_config;

  @SuppressWarnings("removal")
  public FireControlSubsystem() {
    m_fireMotor = new SparkMax(CANConstants.MOTOR_FIRE_ID, MotorType.kBrushless);
    m_config = new SparkMaxConfig();

    // Safety Limits
    m_config.smartCurrentLimit(40);

    m_fireMotor.configure(m_config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
  }

  /**
   * Fires the mechanism at a specific speed based on the Y-axis.
   *
   * @param speed The target speed (absolute value of Y-axis, 0 to 1).
   */
  public void fire(double speed) {
    double adjustedSpeed =
        SpeedConstants.adjustSpeed(
            speed, SpeedConstants.FIRE_MAX_SPEED, SpeedConstants.FIRE_SENSITIVITY);
    m_fireMotor.set(adjustedSpeed);
  }

  /** Stops the fire motor. */
  public void stop() {
    m_fireMotor.set(0);
  }

  @Override
  public void periodic() {
    // Debugging current fire motor speed
    RobotTelemetry.putNumber("Fire Motor Speed Output", m_fireMotor.get());
  }

  @Override
  public void simulationPeriodic() {
    // Broadcast for Python App
    RobotTelemetry.putBoolean("Sim_IsFiring", Math.abs(m_fireMotor.get()) > 0.1);
  }
}
