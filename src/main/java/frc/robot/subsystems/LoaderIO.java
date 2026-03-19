package frc.robot.subsystems;

import org.littletonrobotics.junction.AutoLog;

public interface LoaderIO {
  @AutoLog
  public static class LoaderIOInputs {
    public double appliedVolts = 0.0;
    public double currentAmps = 0.0;
    public double velocityRPM = 0.0;
  }

  public default void updateInputs(LoaderIOInputs inputs) {}

  public default void setVoltage(double volts) {}

  public default void stop() {}
}
