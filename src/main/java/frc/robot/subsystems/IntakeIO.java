package frc.robot.subsystems;

import org.littletonrobotics.junction.AutoLog;

public interface IntakeIO {
  @AutoLog
  public static class IntakeIOInputs {
    public double mainMotorAppliedVolts = 0.0;
    public double mainMotorCurrentAmps = 0.0;
    public double mainMotorVelocityRPM = 0.0;

    public double pivotMotorAppliedVolts = 0.0;
    public double pivotMotorCurrentAmps = 0.0;
    public double pivotPositionDeg = 0.0;
  }

  /** Updates the set of loggable inputs. */
  public default void updateInputs(IntakeIOInputs inputs) {}

  /** Run the main motor at the specified voltage. */
  public default void setVoltage(double volts) {}

  /** Set the target angle in degrees for the pivot motor. */
  public default void setPivotAngle(double degrees) {}

  /** Stop the motors. */
  public default void stop() {}

  public default void stopPivot() {}
}
