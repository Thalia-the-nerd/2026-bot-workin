package frc.robot.subsystems;

import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.RobotTelemetry;
import org.littletonrobotics.junction.Logger;

public class FireControlSubsystem extends SubsystemBase {
  private final FireControlIO m_io;
  private final FireControlIOInputsAutoLogged m_inputs = new FireControlIOInputsAutoLogged();
  private final SimpleMotorFeedforward m_feedforward;
  private final edu.wpi.first.math.filter.SlewRateLimiter m_spinDownLimiter;

  public FireControlSubsystem(FireControlIO io) {
    m_io = io;

    // Approximate feedforward constants for a NEO flywheel (Volts, V*s/rad, V*s^2/rad)
    m_feedforward = new SimpleMotorFeedforward(0.1, 0.12, 0.01);

    // Limits deceleration to 5000 RPM per second
    m_spinDownLimiter = new edu.wpi.first.math.filter.SlewRateLimiter(5000.0);
  }

  /**
   * Sets the shooter to a specific target RPM.
   *
   * @param targetRPM The target RPM for the flywheel.
   */
  public void setShooterRPM(double targetRPM) {
    if (targetRPM <= 0) {
      targetRPM = m_spinDownLimiter.calculate(0);
      if (targetRPM < 50) {
        stop();
        return;
      }
    } else {
      m_spinDownLimiter.reset(targetRPM);
    }
    // Convert target RPM to target revs/second for Feedforward
    double feedforwardVoltage = m_feedforward.calculate(targetRPM / 60.0);

    m_io.setVelocity(targetRPM, feedforwardVoltage);
  }

  /**
   * Checks if the flywheel is at the target RPM within a given tolerance.
   *
   * @param targetRPM The target RPM.
   * @param tolerance The allowed RPM difference.
   * @return True if the RPM is within the tolerance.
   */
  public boolean isAtRPM(double targetRPM, double tolerance) {
    double currentRPM = m_inputs.velocityRPM;
    return Math.abs(currentRPM - targetRPM) <= tolerance;
  }

  /** Stops the fire motor. */
  public void stop() {
    m_spinDownLimiter.reset(0);
    m_io.stop();
  }

  @Override
  public void periodic() {
    m_io.updateInputs(m_inputs);
    Logger.processInputs("FireControl", m_inputs);

    // Debugging current fire motor speed and RPM
    RobotTelemetry.putNumber("Fire Motor Speed Output", m_inputs.appliedVolts / 12.0);
    RobotTelemetry.putNumber("Fire Motor RPM", m_inputs.velocityRPM);
  }

  @Override
  public void simulationPeriodic() {
    // Broadcast for Python App
    RobotTelemetry.putBoolean("Sim_IsFiring", Math.abs(m_inputs.appliedVolts) > 1.2);
  }
}
