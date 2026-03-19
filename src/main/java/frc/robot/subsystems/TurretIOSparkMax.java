package frc.robot.subsystems;

import com.revrobotics.spark.SparkBase.ControlType;
import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import frc.robot.constants.Constants.CANConstants;

public class TurretIOSparkMax implements TurretIO {
  private final SparkMax m_turretMotor;
  private final SparkClosedLoopController m_pidController;

  @SuppressWarnings("removal")
  public TurretIOSparkMax() {
    m_turretMotor = new SparkMax(CANConstants.MOTOR_TURRET_ID, MotorType.kBrushless);
    SparkMaxConfig config = new SparkMaxConfig();

    config.smartCurrentLimit(25);
    config.idleMode(SparkMaxConfig.IdleMode.kBrake);

    config.closedLoop.pid(0.05, 0.0, 0.0);
    config.closedLoop.outputRange(-1.0, 1.0);

    config.closedLoop.maxMotion.maxVelocity(1000);
    config.closedLoop.maxMotion.maxAcceleration(500);
    config.closedLoop.maxMotion.allowedClosedLoopError(0.5);

    m_turretMotor.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

    m_pidController = m_turretMotor.getClosedLoopController();
  }

  @Override
  public void updateInputs(TurretIOInputs inputs) {
    inputs.appliedVolts = m_turretMotor.getAppliedOutput() * m_turretMotor.getBusVoltage();
    inputs.currentAmps = m_turretMotor.getOutputCurrent();
    inputs.positionRotations = m_turretMotor.getEncoder().getPosition();
    inputs.velocityRPM = m_turretMotor.getEncoder().getVelocity();
  }

  @SuppressWarnings("removal")
  @Override
  public void setPosition(double positionRotations) {
    m_pidController.setReference(
        positionRotations,
        ControlType.kMAXMotionPositionControl,
        com.revrobotics.spark.ClosedLoopSlot.kSlot0);
  }

  @Override
  public void setVoltage(double volts) {
    m_turretMotor.setVoltage(volts);
  }

  @Override
  public void stop() {
    m_turretMotor.stopMotor();
  }
}
