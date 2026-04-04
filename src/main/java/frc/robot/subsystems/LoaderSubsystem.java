package frc.robot.subsystems;

import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.constants.SpeedConstants;
import org.littletonrobotics.junction.Logger;

/** Subsystem handling the 3-motor loader. */
public class LoaderSubsystem extends SubsystemBase {

  private final LoaderIO m_io;
  private final LoaderIOInputsAutoLogged m_inputs = new LoaderIOInputsAutoLogged();
  private final SlewRateLimiter m_speedLimiter;

  public LoaderSubsystem(LoaderIO io) {
    m_io = io;
    // Software Slew Rate Limiter for manual inputs (acceleration cap: full speed in 0.5s)
    m_speedLimiter = new SlewRateLimiter(2.0);
  }

  /**
   * Sets the speed of the loader motor.
   *
   * @param speed Speed from -1.0 to 1.0. positive spins inward.
   */
  public void setLoaderSpeed(double speed) {
    double rawSpeed =
        SpeedConstants.adjustSpeed(
            speed, SpeedConstants.LOADER_1_MAX_SPEED, SpeedConstants.LOADER_1_SENSITIVITY);
    double adjustedSpeed = m_speedLimiter.calculate(rawSpeed);

    m_io.setVoltage(adjustedSpeed * 12.0);
  }

  /** Stops the loader. */
  public void stop() {
    m_io.stop();
    m_speedLimiter.reset(0);
  }

  @Override
  public void periodic() {
    m_io.updateInputs(m_inputs);
    Logger.processInputs("Loader", m_inputs);
  }
}
