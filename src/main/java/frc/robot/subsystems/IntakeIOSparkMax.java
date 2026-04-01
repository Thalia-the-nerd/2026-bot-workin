package frc.robot.subsystems;

import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import frc.robot.constants.Constants.CANConstants;

public class IntakeIOSparkMax implements IntakeIO {
  private final SparkMax m_intakeMotorMain;
  private final SparkMax m_intakeMotorSecondary;

  @SuppressWarnings("removal")
  public IntakeIOSparkMax() {
    m_intakeMotorMain = new SparkMax(CANConstants.MOTOR_INTAKE_DRIVE_ID, MotorType.kBrushless);
    m_intakeMotorSecondary = new SparkMax(CANConstants.MOTOR_INTAKE_PIVOT_ID, MotorType.kBrushless);

    SparkMaxConfig config = new SparkMaxConfig();
    config.idleMode(SparkMaxConfig.IdleMode.kCoast);
    config.openLoopRampRate(0.25);

    m_intakeMotorMain.configure(
        config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

    config.follow(m_intakeMotorMain);
    m_intakeMotorSecondary.configure(
        config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
  }

  @Override
  public void updateInputs(IntakeIOInputs inputs) {
    inputs.mainMotorAppliedVolts =
        m_intakeMotorMain.getAppliedOutput() * m_intakeMotorMain.getBusVoltage();
    inputs.mainMotorCurrentAmps = m_intakeMotorMain.getOutputCurrent();
    inputs.mainMotorVelocityRPM = m_intakeMotorMain.getEncoder().getVelocity();
  }

  @Override
  public void setVoltage(double volts) {
    m_intakeMotorMain.setVoltage(volts);
  }

  @Override
  public void stop() {
    m_intakeMotorMain.stopMotor();
  }
}
