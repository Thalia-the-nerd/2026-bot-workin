package frc.robot;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for TurretSubsystem angle conversion math.
 *
 * <p>Angles are: (rotations / gearRatio) * 360 for degrees, * 2π for radians. TURRET_GEAR_RATIO =
 * 10.0
 */
public class TurretAngleConversionTest {

  private static final double GEAR_RATIO = 10.0;
  private static final double DELTA = 1e-6;

  /** Mirrors getTurretAngleDegrees() logic. */
  private static double toDegrees(double rotations) {
    return (rotations / GEAR_RATIO) * 360.0;
  }

  /** Mirrors getTurretAngleRadians() logic. */
  private static double toRadians(double rotations) {
    return (rotations / GEAR_RATIO) * 2.0 * Math.PI;
  }

  // ─── Degrees ─────────────────────────────────────────────────────

  @Test
  public void testDegrees_zeroRotations_isZero() {
    assertEquals(0.0, toDegrees(0.0), DELTA, "0 rotations is 0 degrees");
  }

  @Test
  public void testDegrees_oneFullMotorRotation_isThirtySixDegrees() {
    // 1 rotation / 10 gear ratio = 1/10 of a full turn = 36 degrees
    assertEquals(36.0, toDegrees(1.0), DELTA, "1 motor rotation = 36 degrees turret");
  }

  @Test
  public void testDegrees_tenMotorRotations_isFullCircle() {
    // 10 rotations / 10 gear ratio = 1 full turret rotation = 360 degrees
    assertEquals(360.0, toDegrees(10.0), DELTA, "10 motor rotations = 360 degrees");
  }

  @Test
  public void testDegrees_negativeRotations() {
    assertEquals(-36.0, toDegrees(-1.0), DELTA, "-1 motor rotation = -36 degrees");
  }

  @Test
  public void testDegrees_halfMotorRotation() {
    assertEquals(18.0, toDegrees(0.5), DELTA, "0.5 motor rotations = 18 degrees");
  }

  // ─── Radians ─────────────────────────────────────────────────────

  @Test
  public void testRadians_zeroRotations_isZero() {
    assertEquals(0.0, toRadians(0.0), DELTA, "0 rotations is 0 radians");
  }

  @Test
  public void testRadians_tenMotorRotations_isTwoPi() {
    assertEquals(2 * Math.PI, toRadians(10.0), DELTA, "10 motor rotations = 2π radians");
  }

  @Test
  public void testRadians_fiveMotorRotations_isPi() {
    assertEquals(Math.PI, toRadians(5.0), DELTA, "5 motor rotations = π radians");
  }

  @Test
  public void testRadians_negativeHalfTurn() {
    assertEquals(-Math.PI, toRadians(-5.0), DELTA, "-5 motor rotations = -π radians");
  }

  // ─── Degrees vs Radians consistency ──────────────────────────────

  @Test
  public void testDegreesAndRadians_areConsistent() {
    for (double r = -10.0; r <= 10.0; r += 0.5) {
      double deg = toDegrees(r);
      double rad = toRadians(r);
      assertEquals(Math.toRadians(deg), rad, DELTA, "degrees->radians must match direct radians");
    }
  }

  // ─── isAtAngle logic ─────────────────────────────────────────────

  /** Mirrors TurretSubsystem.isAtAngle() */
  private static boolean isAtAngle(
      double currentDegrees, double targetDegrees, double toleranceDegrees) {
    return Math.abs(currentDegrees - targetDegrees) <= toleranceDegrees;
  }

  @Test
  public void testIsAtAngle_exactMatch_isTrue() {
    assertTrue(isAtAngle(45.0, 45.0, 2.0), "Exact match should return true");
  }

  @Test
  public void testIsAtAngle_withinTolerance_isTrue() {
    assertTrue(isAtAngle(44.0, 45.0, 2.0), "1 degree off within 2-degree tolerance");
  }

  @Test
  public void testIsAtAngle_atBoundary_isTrue() {
    assertTrue(isAtAngle(43.0, 45.0, 2.0), "Exactly at tolerance boundary is true");
  }

  @Test
  public void testIsAtAngle_justOutsideTolerance_isFalse() {
    assertFalse(isAtAngle(42.9, 45.0, 2.0), "Just outside tolerance should be false");
  }

  @Test
  public void testIsAtAngle_zeroTolerance_requiresExactMatch() {
    assertTrue(isAtAngle(90.0, 90.0, 0.0), "Zero tolerance: exact match");
    assertFalse(isAtAngle(90.001, 90.0, 0.0), "Zero tolerance: not even 0.001 off");
  }

  @Test
  public void testIsAtAngle_unwindingThreshold_25Degrees() {
    // The subsystem unwinds back to 0 once within 25 degrees (see periodic)
    assertTrue(isAtAngle(0.0, 0.0, 25.0), "0 deg is within 25 deg of 0");
    assertTrue(isAtAngle(24.9, 0.0, 25.0), "24.9 deg is within 25 deg of 0");
    assertFalse(isAtAngle(25.1, 0.0, 25.0), "25.1 deg is outside 25 deg tolerance of 0");
  }
}
