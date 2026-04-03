package frc.robot.subsystems;

import org.littletonrobotics.junction.AutoLog;

public interface IntakeIO {
  @AutoLog
  public static class IntakeIOInputs {
    public double runMotorAppliedVolts = 0.0;
    public double runMotorCurrentAmps = 0.0;
    public double runMotorVelocityRPM = 0.0;

    public double pivotMotorAppliedVolts = 0.0;
    public double pivotMotorCurrentAmps = 0.0;
    public double pivotPositionDeg = 0.0;
  }

  /** Updates the set of loggable inputs. */
  public default void updateInputs(IntakeIOInputs inputs) {}

  /** Run the run motor at the specified voltage. */
  public default void setRunVoltage(double volts) {}

  /** Set the target voltage for the pivot motor. */
  public default void setPivotVoltage(double volts) {}

  /** Stop the motors. */
  public default void stop() {}

  public default void stopPivot() {}
}
