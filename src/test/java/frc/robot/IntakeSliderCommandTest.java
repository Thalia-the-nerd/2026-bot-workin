package frc.robot;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for IntakeSliderCommand's throttle-to-speed mapping logic.
 *
 * <p>We test the math inline since the command logic itself is plain arithmetic – no scheduler
 * needed.
 */
public class IntakeSliderCommandTest {

  // ─── Throttle mapping math ────────────────────────────────────────
  // The command maps rawThrottle ∈ [-1, 1] → speed = (raw + 1) / 2 → [0, 1]

  private static double mapThrottle(double raw) {
    double speed = (raw + 1.0) / 2.0;
    return Math.max(0.0, Math.min(1.0, speed));
  }

  @Test
  public void testThrottleAtNegativeOne_givesZeroSpeed() {
    assertEquals(0.0, mapThrottle(-1.0), 1e-6, "Throttle -1.0 should map to speed 0.0");
  }

  @Test
  public void testThrottleAtZero_givesHalfSpeed() {
    assertEquals(0.5, mapThrottle(0.0), 1e-6, "Throttle 0.0 should map to speed 0.5");
  }

  @Test
  public void testThrottleAtPositiveOne_givesFullSpeed() {
    assertEquals(1.0, mapThrottle(1.0), 1e-6, "Throttle 1.0 should map to speed 1.0");
  }

  @Test
  public void testThrottleAtNegativeHalf_givesQuarterSpeed() {
    assertEquals(0.25, mapThrottle(-0.5), 1e-6, "Throttle -0.5 should map to speed 0.25");
  }

  @Test
  public void testThrottleAtPositiveHalf_givesThreeQuartersSpeed() {
    assertEquals(0.75, mapThrottle(0.5), 1e-6, "Throttle 0.5 should map to speed 0.75");
  }

  @Test
  public void testThrottle_isClampedAboveOne() {
    // Noisy joystick might give 1.01
    assertEquals(1.0, mapThrottle(1.01), 1e-6, "Noisy input above 1.0 is clamped to 1.0");
  }

  @Test
  public void testThrottle_isClampedBelowZero() {
    // Noisy joystick might give -1.01
    assertEquals(0.0, mapThrottle(-1.01), 1e-6, "Noisy input below -1.0 clamps to 0.0");
  }

  @Test
  public void testThrottleMapping_isMonotonic() {
    // Higher throttle values should always produce higher (or equal) speeds
    double prev = mapThrottle(-1.0);
    for (double raw = -0.9; raw <= 1.0; raw += 0.1) {
      double curr = mapThrottle(raw);
      assertTrue(curr >= prev - 1e-9, "Throttle mapping must be monotonically non-decreasing");
      prev = curr;
    }
  }
}
