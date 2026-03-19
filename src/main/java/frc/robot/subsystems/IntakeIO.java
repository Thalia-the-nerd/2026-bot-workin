package frc.robot.subsystems;

import org.littletonrobotics.junction.AutoLog;

public interface IntakeIO {
  @AutoLog
  public static class IntakeIOInputs {
    public double mainMotorAppliedVolts = 0.0;
    public double mainMotorCurrentAmps = 0.0;
    public double mainMotorVelocityRPM = 0.0;
  }

  /** Updates the set of loggable inputs. */
  public default void updateInputs(IntakeIOInputs inputs) {}

  /** Run the main motor at the specified voltage. */
  public default void setVoltage(double volts) {}

  /** Stop the motor. */
  public default void stop() {}
}
