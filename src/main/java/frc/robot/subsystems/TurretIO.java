package frc.robot.subsystems;

import org.littletonrobotics.junction.AutoLog;

public interface TurretIO {
  @AutoLog
  public static class TurretIOInputs {
    public double appliedVolts = 0.0;
    public double currentAmps = 0.0;
    public double positionRotations = 0.0;
    public double velocityRPM = 0.0;
  }

  public default void updateInputs(TurretIOInputs inputs) {}

  public default void setPosition(double positionRotations) {}

  public default void setVoltage(double volts) {}

  public default void stop() {}
}
