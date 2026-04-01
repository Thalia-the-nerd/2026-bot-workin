package frc.robot.subsystems;

import com.revrobotics.spark.SparkBase.ControlType;
import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import frc.robot.constants.Constants;
import frc.robot.constants.Constants.CANConstants;

public class IntakeIOSparkMax implements IntakeIO {
  private final SparkMax m_intakeMotorMain;
  private final SparkMax m_intakeMotorSecondary;
  private final SparkClosedLoopController m_pivotPidController;

  @SuppressWarnings("removal")
  public IntakeIOSparkMax() {
    m_intakeMotorMain = new SparkMax(CANConstants.MOTOR_INTAKE_DRIVE_ID, MotorType.kBrushless);
    m_intakeMotorSecondary = new SparkMax(CANConstants.MOTOR_INTAKE_PIVOT_ID, MotorType.kBrushless);

    SparkMaxConfig config = new SparkMaxConfig();
    config.idleMode(SparkMaxConfig.IdleMode.kCoast);
    config.openLoopRampRate(0.25);
    config.smartCurrentLimit(40);

    m_intakeMotorMain.configure(
        config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

    SparkMaxConfig pivotConfig = new SparkMaxConfig();
    pivotConfig.idleMode(SparkMaxConfig.IdleMode.kBrake);
    pivotConfig.smartCurrentLimit(30);
    pivotConfig.closedLoop.pid(0.1, 0, 0); // Basic placeholder P gain
    pivotConfig.closedLoop.outputRange(-1.0, 1.0);
    // Configure encoder to output in degrees based on the gear ratio
    pivotConfig.encoder.positionConversionFactor(360.0 / Constants.INTAKE_PIVOT_GEAR_RATIO);

    m_intakeMotorSecondary.configure(
        pivotConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

    m_pivotPidController = m_intakeMotorSecondary.getClosedLoopController();
    m_intakeMotorSecondary.getEncoder().setPosition(0.0); // Assume starting position is 0
  }

  @Override
  public void updateInputs(IntakeIOInputs inputs) {
    inputs.mainMotorAppliedVolts =
        m_intakeMotorMain.getAppliedOutput() * m_intakeMotorMain.getBusVoltage();
    inputs.mainMotorCurrentAmps = m_intakeMotorMain.getOutputCurrent();
    inputs.mainMotorVelocityRPM = m_intakeMotorMain.getEncoder().getVelocity();

    inputs.pivotMotorAppliedVolts =
        m_intakeMotorSecondary.getAppliedOutput() * m_intakeMotorSecondary.getBusVoltage();
    inputs.pivotMotorCurrentAmps = m_intakeMotorSecondary.getOutputCurrent();
    inputs.pivotPositionDeg = m_intakeMotorSecondary.getEncoder().getPosition();
  }

  @Override
  public void setVoltage(double volts) {
    m_intakeMotorMain.setVoltage(volts);
  }

  @SuppressWarnings("removal")
  @Override
  public void setPivotAngle(double degrees) {
    m_pivotPidController.setReference(
        degrees,
        ControlType.kPosition,
        com.revrobotics.spark.ClosedLoopSlot.kSlot0,
        0.0,
        SparkClosedLoopController.ArbFFUnits.kVoltage);
  }

  @Override
  public void stop() {
    m_intakeMotorMain.stopMotor();
  }

  @Override
  public void stopPivot() {
    m_intakeMotorSecondary.stopMotor();
  }
}
