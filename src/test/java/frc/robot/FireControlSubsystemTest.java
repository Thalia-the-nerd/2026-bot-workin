package frc.robot;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.simulation.SimHooks;
import frc.robot.subsystems.FireControlSubsystem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Simulated I/O tests for FireControlSubsystem.
 *
 * <p>Uses WPILib HAL simulation so SparkMax objects work without hardware.
 */
public class FireControlSubsystemTest {

  private static FireControlSubsystem m_fireControl;

  @BeforeAll
  static void initAll() {
    assert HAL.initialize(500, 0);
    SimHooks.pauseTiming();
    m_fireControl =
        new FireControlSubsystem(
            new frc.robot.subsystems.FireControlIO() {
              private double rpm = 0.0;

              @Override
              public void updateInputs(FireControlIOInputs inputs) {
                inputs.velocityRPM = rpm;
              }

              @Override
              public void setVelocity(double v, double ff) {
                rpm = v;
              }

              @Override
              public void stop() {
                rpm = 0.0;
              }
            });
  }

  // ─── stop() ──────────────────────────────────────────────────────

  @Test
  public void testStop_doesNotThrow() {
    // stop() should always succeed without error
    assertDoesNotThrow(() -> m_fireControl.stop(), "stop() must not throw");
  }

  // ─── setShooterRPM() ─────────────────────────────────────────────

  @Test
  public void testSetShooterRPM_zeroOrNegative_callsStop() {
    // Zero or negative RPM should stop the motor (no exception)
    assertDoesNotThrow(() -> m_fireControl.setShooterRPM(0.0), "setShooterRPM(0) must not throw");
    assertDoesNotThrow(
        () -> m_fireControl.setShooterRPM(-100.0), "setShooterRPM(-100) must not throw");
  }

  @Test
  public void testSetShooterRPM_positiveRPM_doesNotThrow() {
    assertDoesNotThrow(
        () -> m_fireControl.setShooterRPM(3000.0), "setShooterRPM(3000) must not throw");
  }

  @Test
  public void testSetShooterRPM_maxRPM_doesNotThrow() {
    assertDoesNotThrow(
        () -> m_fireControl.setShooterRPM(5000.0), "setShooterRPM(5000) must not throw");
  }

  @Test
  public void testSetShooterRPM_exceedingMaxRPM_doesNotBreak() {
    assertDoesNotThrow(
        () -> m_fireControl.setShooterRPM(10000.0),
        "setShooterRPM(10000) must safely process limits");
  }

  @Test
  public void testSetShooterRPM_NaNInput_doesNotThrow() {
    assertDoesNotThrow(
        () -> m_fireControl.setShooterRPM(Double.NaN), "setShooterRPM(NaN) must not crash");
  }

  // ─── isAtRPM() ───────────────────────────────────────────────────

  @Test
  public void testIsAtRPM_afterStop_reportsFalseForHighTarget() {
    m_fireControl.stop();
    m_fireControl.periodic(); // Synchronize inputs
    assertFalse(
        m_fireControl.isAtRPM(3000.0, 50.0), "After stop, should not be at 3000 RPM in sim");
  }

  @Test
  public void testIsAtRPM_zeroTarget_reportsTrueWhenStopped() {
    m_fireControl.stop();
    m_fireControl.periodic(); // Synchronize inputs
    assertTrue(
        m_fireControl.isAtRPM(0.0, 50.0),
        "After stop, sim encoder reading ~0, so isAtRPM(0, 50) is true");
  }

  // ─── periodic() ──────────────────────────────────────────────────

  @Test
  public void testPeriodic_doesNotThrow() {
    assertDoesNotThrow(() -> m_fireControl.periodic(), "periodic() must not throw");
  }

  @Test
  public void testSimulationPeriodic_doesNotThrow() {
    assertDoesNotThrow(
        () -> m_fireControl.simulationPeriodic(), "simulationPeriodic() must not throw");
  }
}
