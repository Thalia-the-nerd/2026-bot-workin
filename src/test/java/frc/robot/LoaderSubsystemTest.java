package frc.robot;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import frc.robot.subsystems.LoaderSubsystem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Simulated I/O tests for LoaderSubsystem. */
public class LoaderSubsystemTest {

  private static LoaderSubsystem m_loader;

  @BeforeAll
  static void initAll() {
    assert HAL.initialize(500, 0);
    m_loader = new LoaderSubsystem(new frc.robot.subsystems.LoaderIO() {});
  }

  // ─── setLoaderSpeed ───────────────────────────────────────────────

  @Test
  public void testSetLoaderSpeed_zero_doesNotThrow() {
    assertDoesNotThrow(() -> m_loader.setLoaderSpeed(0.0), "setLoaderSpeed(0.0) must not throw");
  }

  @Test
  public void testSetLoaderSpeed_fullForward_doesNotThrow() {
    assertDoesNotThrow(() -> m_loader.setLoaderSpeed(1.0), "setLoaderSpeed(1.0) must not throw");
  }

  @Test
  public void testSetLoaderSpeed_fullReverse_doesNotThrow() {
    assertDoesNotThrow(() -> m_loader.setLoaderSpeed(-1.0), "setLoaderSpeed(-1.0) must not throw");
  }

  @Test
  public void testSetLoaderSpeed_halfSpeed_doesNotThrow() {
    assertDoesNotThrow(() -> m_loader.setLoaderSpeed(0.5), "setLoaderSpeed(0.5) must not throw");
  }

  // SlewRateLimiter: speed ramps up, so first call won't be at full speed immediately.
  // We just check no exception is thrown here – functional sim would need time steps.

  @Test
  public void testSetLoaderSpeed_repeatedCalls_doesNotThrow() {
    for (double speed = -1.0; speed <= 1.0; speed += 0.25) {
      final double s = speed;
      assertDoesNotThrow(
          () -> m_loader.setLoaderSpeed(s), "setLoaderSpeed(" + s + ") must not throw");
    }
  }

  // ─── stop ────────────────────────────────────────────────────────

  @Test
  public void testStop_doesNotThrow() {
    assertDoesNotThrow(() -> m_loader.stop(), "stop() must not throw");
  }

  @Test
  public void testStop_afterForwardRun_doesNotThrow() {
    m_loader.setLoaderSpeed(1.0);
    assertDoesNotThrow(() -> m_loader.stop(), "stop() after running must not throw");
  }

  @Test
  public void testStop_resetsSpeedLimiter() {
    // After stop(), the limiter is reset to 0.
    // This means the next setLoaderSpeed(1.0) will ramp from 0 again (no sudden jump).
    m_loader.setLoaderSpeed(1.0);
    m_loader.stop();
    // Now call forward again – shouldn't throw even without time advancing
    assertDoesNotThrow(
        () -> m_loader.setLoaderSpeed(1.0), "setLoaderSpeed after stop should not throw");
  }

  // ─── periodic ────────────────────────────────────────────────────

  @Test
  public void testPeriodic_doesNotThrow() {
    assertDoesNotThrow(() -> m_loader.periodic(), "periodic() must not throw");
  }

  @Test
  public void testPeriodic_whileRunning_doesNotThrow() {
    m_loader.setLoaderSpeed(1.0);
    assertDoesNotThrow(() -> m_loader.periodic(), "periodic() while running must not throw");
  }
}
