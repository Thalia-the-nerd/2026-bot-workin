package frc.robot.subsystems;

import com.revrobotics.spark.SparkBase.ControlType;
import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import frc.robot.constants.Constants.CANConstants;

public class FireControlIOSparkMax implements FireControlIO {
  private final SparkMax m_fireMotor;
  private final SparkClosedLoopController m_pidController;

  @SuppressWarnings("removal")
  public FireControlIOSparkMax() {
    m_fireMotor = new SparkMax(CANConstants.MOTOR_FIRE_ID, MotorType.kBrushless);
    SparkMaxConfig config = new SparkMaxConfig();

    config.smartCurrentLimit(40);
    config.closedLoop.pid(0.001, 0, 0);
    config.closedLoop.outputRange(-1.0, 1.0);
    config.inverted(true);
    m_fireMotor.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

    m_pidController = m_fireMotor.getClosedLoopController();
  }

  @Override
  public void updateInputs(FireControlIOInputs inputs) {
    inputs.appliedVolts = m_fireMotor.getAppliedOutput() * m_fireMotor.getBusVoltage();
    inputs.currentAmps = m_fireMotor.getOutputCurrent();
    inputs.velocityRPM = m_fireMotor.getEncoder().getVelocity();
  }

  @SuppressWarnings("removal")
  @Override
  public void setVelocity(double velocityRPM, double feedforwardVolts) {
    m_pidController.setReference(
        velocityRPM,
        ControlType.kVelocity,
        com.revrobotics.spark.ClosedLoopSlot.kSlot0,
        feedforwardVolts,
        SparkClosedLoopController.ArbFFUnits.kVoltage);
  }

  @Override
  public void stop() {
    m_fireMotor.stopMotor();
  }
}
