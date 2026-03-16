package frc.robot;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for FireControlSubsystem's RPM tolerance logic.
 *
 * <p>isAtRPM(target, tolerance) returns true when |currentRPM − targetRPM| ≤ tolerance. We test
 * this math directly without needing a SparkMax.
 */
public class FireControlIsAtRPMTest {

  /** Mirrors FireControlSubsystem.isAtRPM() without hardware. */
  private static boolean isAtRPM(double currentRPM, double targetRPM, double tolerance) {
    return Math.abs(currentRPM - targetRPM) <= tolerance;
  }

  @Test
  public void testIsAtRPM_exactMatch_isTrue() {
    assertTrue(isAtRPM(3000.0, 3000.0, 50.0), "Exact RPM match should be at target");
  }

  @Test
  public void testIsAtRPM_withinTolerance_isTrue() {
    assertTrue(isAtRPM(2990.0, 3000.0, 50.0), "2990 is within 50 RPM of 3000");
  }

  @Test
  public void testIsAtRPM_atBoundary_isTrue() {
    assertTrue(isAtRPM(2950.0, 3000.0, 50.0), "Exactly 50 RPM below target is at tolerance");
  }

  @Test
  public void testIsAtRPM_justOutside_isFalse() {
    assertFalse(isAtRPM(2949.0, 3000.0, 50.0), "51 RPM below target is outside tolerance");
  }

  @Test
  public void testIsAtRPM_aboveTarget_withinTolerance() {
    assertTrue(isAtRPM(3040.0, 3000.0, 50.0), "3040 is within 50 RPM above 3000");
  }

  @Test
  public void testIsAtRPM_aboveTarget_outsideTolerance() {
    assertFalse(isAtRPM(3051.0, 3000.0, 50.0), "3051 is 51 RPM above 3000, outside tolerance");
  }

  @Test
  public void testIsAtRPM_zeroCurrentRPM_isNotAtTarget() {
    assertFalse(isAtRPM(0.0, 3000.0, 50.0), "0 RPM is not within 50 of 3000");
  }

  @Test
  public void testIsAtRPM_zeroTarget_stoppedIsAtTarget() {
    // When target is 0 and current is 0, we're stopped = at target
    assertTrue(isAtRPM(0.0, 0.0, 50.0), "0 RPM at 0 target is at rest");
  }

  @Test
  public void testIsAtRPM_fireCommandTolerance_50RPM() {
    // FireCommand uses tolerance 50.0 and min threshold 100 RPM before feeding
    double tolerance = 50.0;
    assertTrue(isAtRPM(2500.0, 2500.0, tolerance), "Same RPM, at target for feeding");
    assertFalse(isAtRPM(2400.0, 2500.0, tolerance), "100 RPM below, too far to feed");
  }

  @Test
  public void testIsAtRPM_maxNEO_5000RPM() {
    // At near max NEO speed, still works
    assertTrue(isAtRPM(5000.0, 5000.0, 50.0), "5000 RPM at target remains at target");
    assertFalse(isAtRPM(4900.0, 5000.0, 50.0), "100 RPM below 5000 is outside 50 RPM tolerance");
  }
}
