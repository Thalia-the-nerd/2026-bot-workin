package frc.robot.subsystems;

import org.littletonrobotics.junction.AutoLog;

public interface FireControlIO {
  @AutoLog
  public static class FireControlIOInputs {
    public double appliedVolts = 0.0;
    public double currentAmps = 0.0;
    public double velocityRPM = 0.0;
  }

  public default void updateInputs(FireControlIOInputs inputs) {}

  public default void setVelocity(double velocityRPM, double feedforwardVolts) {}

  public default void stop() {}
}
