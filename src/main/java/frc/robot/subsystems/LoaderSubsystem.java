package frc.robot.subsystems;

import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.constants.Constants.CANConstants;
import frc.robot.constants.SpeedConstants;

/** Subsystem handling the 3-motor loader. */
public class LoaderSubsystem extends SubsystemBase {

  private final SparkMax m_loaderMotor1;
  private final SparkMax m_loaderMotor2;
  private final SparkMax m_loaderMotor3;
  private final SparkMaxConfig m_config;

  private final SlewRateLimiter m_speedLimiter;

  public LoaderSubsystem() {
    m_loaderMotor1 = new SparkMax(CANConstants.MOTOR_LOADER_1_ID, MotorType.kBrushless);
    m_loaderMotor2 = new SparkMax(CANConstants.MOTOR_LOADER_2_ID, MotorType.kBrushless);
    m_loaderMotor3 = new SparkMax(CANConstants.MOTOR_LOADER_3_ID, MotorType.kBrushless);
    m_config = new SparkMaxConfig();

    // Default to Coast mode or Brake mode depending on team preference.
    m_config.idleMode(SparkMaxConfig.IdleMode.kCoast);

    // Hardware-level Torque Smoothing for voltage spikes
    m_config.openLoopRampRate(0.25);

    // Apply configuration to main motor
    m_loaderMotor1.configure(
        m_config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

    // Set secondary motors to follow the primary motor
    m_config.follow(m_loaderMotor1);

    m_loaderMotor2.configure(
        m_config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

    m_loaderMotor3.configure(
        m_config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

    // Software Slew Rate Limiter for manual inputs (acceleration cap: full speed in 0.5s)
    m_speedLimiter = new SlewRateLimiter(2.0);
  }

  /**
   * Sets the speed of the loader motor.
   *
   * @param speed Speed from -1.0 to 1.0. positive spins inward.
   */
  public void setLoaderSpeed(double speed) {
    double adjustedSpeed =
        m_speedLimiter.calculate(
            SpeedConstants.adjustSpeed(
                speed, SpeedConstants.LOADER_1_MAX_SPEED, SpeedConstants.LOADER_1_SENSITIVITY));
    m_loaderMotor1.set(adjustedSpeed);
  }

  /** Stops the loader. */
  public void stop() {
    m_loaderMotor1.stopMotor();
    m_speedLimiter.reset(0);
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }
}
