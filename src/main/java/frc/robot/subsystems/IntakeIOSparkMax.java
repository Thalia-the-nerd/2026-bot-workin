package frc.robot.subsystems;

import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import frc.robot.constants.Constants;
import frc.robot.constants.Constants.CANConstants;

public class IntakeIOSparkMax implements IntakeIO {
  private final SparkMax m_intakeMotorRun;
  private final SparkMax m_intakeMotorPivot;

  @SuppressWarnings("removal")
  public IntakeIOSparkMax() {
    m_intakeMotorRun = new SparkMax(CANConstants.MOTOR_INTAKE_DRIVE_ID, MotorType.kBrushless);
    m_intakeMotorPivot = new SparkMax(CANConstants.MOTOR_INTAKE_PIVOT_ID, MotorType.kBrushless);

    SparkMaxConfig config = new SparkMaxConfig();
    config.idleMode(SparkMaxConfig.IdleMode.kCoast);
    config.openLoopRampRate(0.25);
    config.smartCurrentLimit(40);

    m_intakeMotorRun.configure(
        config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

    SparkMaxConfig pivotConfig = new SparkMaxConfig();
    pivotConfig.idleMode(SparkMaxConfig.IdleMode.kBrake);
    pivotConfig.smartCurrentLimit(30);
    // Configure encoder to output in degrees based on the gear ratio
    pivotConfig.encoder.positionConversionFactor(360.0 / Constants.INTAKE_PIVOT_GEAR_RATIO);

    m_intakeMotorPivot.configure(
        pivotConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

    m_intakeMotorPivot.getEncoder().setPosition(0.0); // Assume starting position is 0
  }

  @Override
  public void updateInputs(IntakeIOInputs inputs) {
    inputs.runMotorAppliedVolts =
        m_intakeMotorRun.getAppliedOutput() * m_intakeMotorRun.getBusVoltage();
    inputs.runMotorCurrentAmps = m_intakeMotorRun.getOutputCurrent();
    inputs.runMotorVelocityRPM = m_intakeMotorRun.getEncoder().getVelocity();

    inputs.pivotMotorAppliedVolts =
        m_intakeMotorPivot.getAppliedOutput() * m_intakeMotorPivot.getBusVoltage();
    inputs.pivotMotorCurrentAmps = m_intakeMotorPivot.getOutputCurrent();
    inputs.pivotPositionDeg = m_intakeMotorPivot.getEncoder().getPosition();
  }

  @Override
  public void setRunVoltage(double volts) {
    m_intakeMotorRun.setVoltage(volts);
  }

  @Override
  public void setPivotVoltage(double volts) {
    m_intakeMotorPivot.setVoltage(volts);
  }

  @Override
  public void stop() {
    m_intakeMotorRun.stopMotor();
  }

  @Override
  public void stopPivot() {
    m_intakeMotorPivot.stopMotor();
  }
}
