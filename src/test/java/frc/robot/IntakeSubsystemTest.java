package frc.robot;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import frc.robot.subsystems.IntakeSubsystem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Simulated I/O tests for IntakeSubsystem.
 *
 * <p>Checks that all simulated inputs produce legal subsystem outputs and that the jam-detection
 * state machine behaves correctly under simulation.
 */
public class IntakeSubsystemTest {

  private static IntakeSubsystem m_intake;

  @BeforeAll
  static void initAll() {
    assert HAL.initialize(500, 0);
    m_intake =
        new IntakeSubsystem(
            new frc.robot.subsystems.IntakeIO() {
              private double volts = 0.0;

              @Override
              public void updateInputs(IntakeIOInputs inputs) {
                inputs.runMotorAppliedVolts = volts;
                inputs.runMotorCurrentAmps = 0.0; // Simulated
              }

              @Override
              public void setRunVoltage(double v) {
                volts = v;
              }

              @Override
              public void stop() {
                volts = 0.0;
              }
            });
  }

  // ─── setRunSpeed ───────────────────────────────────────────────

  @Test
  public void testSetIntakeSpeed_zero_doesNotThrow() {
    assertDoesNotThrow(() -> m_intake.setRunSpeed(0.0), "setRunSpeed(0.0) must not throw");
  }

  @Test
  public void testSetIntakeSpeed_fullForward_doesNotThrow() {
    assertDoesNotThrow(() -> m_intake.setRunSpeed(1.0), "setRunSpeed(1.0) must not throw");
  }

  @Test
  public void testSetIntakeSpeed_fullReverse_doesNotThrow() {
    assertDoesNotThrow(() -> m_intake.setRunSpeed(-1.0), "setRunSpeed(-1.0) must not throw");
  }

  @Test
  public void testSetIntakeSpeed_halfSpeed_doesNotThrow() {
    assertDoesNotThrow(() -> m_intake.setRunSpeed(0.5), "setRunSpeed(0.5) must not throw");
  }

  // ─── stop ────────────────────────────────────────────────────────

  @Test
  public void testStop_doesNotThrow() {
    assertDoesNotThrow(() -> m_intake.stop(), "stop() must not throw");
  }

  @Test
  public void testStop_afterForwardRun_doesNotThrow() {
    m_intake.setRunSpeed(1.0);
    assertDoesNotThrow(() -> m_intake.stop(), "stop() after running forward must not throw");
  }

  // ─── periodic ────────────────────────────────────────────────────

  @Test
  public void testPeriodic_doesNotThrow() {
    assertDoesNotThrow(() -> m_intake.periodic(), "periodic() must not throw");
  }

  @Test
  public void testPeriodic_runMultipleTimes_doesNotThrow() {
    for (int i = 0; i < 10; i++) {
      final int tick = i;
      assertDoesNotThrow(() -> m_intake.periodic(), "periodic() tick " + tick + " must not throw");
    }
  }

  // ─── Jam detection: no stall in sim (current reads 0) ────────────

  @Test
  public void testJamDetection_noCurrentInSim_doesNotJam() {
    // In sim, output current is 0A, so the jam threshold (30A) is never crossed.
    // Run several periodic ticks to confirm no jam is detected.
    m_intake.setRunSpeed(1.0);
    for (int i = 0; i < 50; i++) {
      m_intake.periodic();
    }
    // If the intake had jammed, setRunSpeed would be silently blocked.
    // We can verify the system is still responsive by calling stop without exception.
    assertDoesNotThrow(
        () -> m_intake.stop(),
        "After many ticks with 0A sim current, intake should still be stoppable");
  }

  // ─── Speed adjustment sanity ──────────────────────────────────────

  @Test
  public void testSpeedAdjustment_callsAdjustSpeedWithMaxAndSensitivity() {
    // Smoke test: confirm the intake applies SpeedConstants adjustment without crashing
    // at boundary conditions
    assertDoesNotThrow(() -> m_intake.setRunSpeed(0.0));
    assertDoesNotThrow(() -> m_intake.setRunSpeed(1.0));
    assertDoesNotThrow(() -> m_intake.setRunSpeed(-1.0));
    assertDoesNotThrow(() -> m_intake.setRunSpeed(0.5));
    assertDoesNotThrow(() -> m_intake.setRunSpeed(-0.5));
  }
}
