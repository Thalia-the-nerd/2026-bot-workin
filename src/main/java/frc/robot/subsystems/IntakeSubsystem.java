package frc.robot.subsystems;

import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.CANConstants;

/** Subsystem handling the intake/loading system. */
public class IntakeSubsystem extends SubsystemBase {

  private final SparkMax m_intakeMotorMain;
  private final SparkMax m_intakeMotorSecondary;
  private final SparkMaxConfig m_config;

  public IntakeSubsystem() {
    m_intakeMotorMain = new SparkMax(CANConstants.MOTOR_INTAKE_MAIN_ID, MotorType.kBrushless);
    m_intakeMotorSecondary =
        new SparkMax(CANConstants.MOTOR_INTAKE_SECONDARY_ID, MotorType.kBrushless);
    m_config = new SparkMaxConfig();

    // Default to Coast mode or Brake mode depending on team preference.
    // Usually intakes run in Coast so balls/notes aren't crushed on stop.
    m_config.idleMode(SparkMaxConfig.IdleMode.kCoast);

    m_intakeMotorMain.configure(
        m_config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

    // Apply config to secondary motor and follow main motor
    m_config.follow(m_intakeMotorMain);
    m_intakeMotorSecondary.configure(
        m_config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
  }

  /**
   * Sets the speed of the intake motor.
   *
   * @param speed Speed from -1.0 to 1.0. positive spins intake inward.
   */
  public void setIntakeSpeed(double speed) {
    m_intakeMotorMain.set(speed);
  }

  /** Stops the intake. */
  public void stop() {
    m_intakeMotorMain.stopMotor();
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }
}
