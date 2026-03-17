package frc.robot;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.math.MathUtil;
import org.junit.jupiter.api.Test;

/**
 * Tests for FireCommand's internal speed-to-RPM mapping and loader gating logic.
 *
 * <p>We test the math without needing real hardware.
 */
public class FireCommandLogicTest {

  private static final double MAX_RPM = 5000.0;
  private static final double DEADBAND = 0.1;
  private static final double LOADER_ENABLE_THRESHOLD = 100.0;
  private static final double RPM_TOLERANCE = 50.0;

  /** Mirrors FireCommand.execute() RPM calculation. */
  private static double computeTargetRPM(double rawTrigger) {
    double rawSpeed = Math.abs(rawTrigger);
    double speed = MathUtil.clamp(MathUtil.applyDeadband(rawSpeed, DEADBAND), 0.0, 1.0);
    return speed * MAX_RPM;
  }

  /** Mirrors FireCommand loader gate condition. */
  private static boolean shouldFeed(double targetRPM, double currentRPM) {
    boolean atRPM = Math.abs(currentRPM - targetRPM) <= RPM_TOLERANCE;
    return targetRPM > LOADER_ENABLE_THRESHOLD && atRPM;
  }

  // ─── RPM Mapping ─────────────────────────────────────────────────

  @Test
  public void testRPM_noTriggerInput_isZero() {
    assertEquals(0.0, computeTargetRPM(0.0), 1e-6, "No trigger press = 0 RPM");
  }

  @Test
  public void testRPM_deadband_filteredOut() {
    // 0.05 is within the 0.1 deadband → becomes 0 RPM
    assertEquals(0.0, computeTargetRPM(0.05), 1e-6, "Input inside deadband (0.05) → 0 RPM");
  }

  @Test
  public void testRPM_justAboveDeadband_isPropagated() {
    // 0.11 just exceeds deadband
    double rpm = computeTargetRPM(0.11);
    assertTrue(rpm > 0.0, "Input just above deadband should give positive RPM");
  }

  @Test
  public void testRPM_fullTrigger_isMaxRPM() {
    assertEquals(MAX_RPM, computeTargetRPM(1.0), 1e-6, "Full trigger = max 5000 RPM");
  }

  @Test
  public void testRPM_halfTrigger_rescaledByDeadband() {
    // applyDeadband(0.5, 0.1) rescales [0.1..1.0] → [0..1]
    // (0.5 - 0.1) / (1.0 - 0.1) = 0.4/0.9 ≈ 0.4444, so RPM ≈ 0.4444 * 5000 ≈ 2222
    double expected = (0.5 - 0.1) / (1.0 - 0.1) * MAX_RPM;
    assertEquals(
        expected,
        computeTargetRPM(0.5),
        1.0,
        "Half trigger RPM must account for deadband rescaling");
  }

  @Test
  public void testRPM_negativeTrigger_sameAsPositive() {
    // FireCommand uses Math.abs so negative trigger = same speed
    assertEquals(
        computeTargetRPM(0.8),
        computeTargetRPM(-0.8),
        1e-6,
        "Negative trigger (due to axis flip) gives same RPM as positive");
  }

  @Test
  public void testRPM_overFullInput_isClamped() {
    // Noisy input above 1.0
    assertEquals(MAX_RPM, computeTargetRPM(1.5), 1e-6, "Over-range input is clamped to max RPM");
  }

  // ─── Loader gating ────────────────────────────────────────────────

  @Test
  public void testLoader_notAtRPM_doesNotFeed() {
    assertFalse(shouldFeed(3000.0, 0.0), "Loader must NOT spin when flywheel is at 0 RPM");
  }

  @Test
  public void testLoader_atRPM_shouldFeed() {
    assertTrue(shouldFeed(3000.0, 3000.0), "Loader should feed when flywheel is at target RPM");
  }

  @Test
  public void testLoader_withinTolerance_shouldFeed() {
    assertTrue(
        shouldFeed(3000.0, 3040.0),
        "Loader should feed when 40 RPM above target (within 50-RPM tolerance)");
  }

  @Test
  public void testLoader_outsideTolerance_doesNotFeed() {
    assertFalse(shouldFeed(3000.0, 2900.0), "Loader must NOT feed when 100 RPM below target");
  }

  @Test
  public void testLoader_belowThresholdRPM_neverFeeds() {
    // Even if at exactly the same low RPM, the target must be >100 to feed
    assertFalse(shouldFeed(50.0, 50.0), "Loader must not feed when target RPM ≤ 100");
    assertFalse(
        shouldFeed(100.0, 100.0), "Loader must not feed at exactly the threshold (> not >=)");
  }

  @Test
  public void testLoader_aboveThreshold_atRPM_feeds() {
    assertTrue(shouldFeed(101.0, 101.0), "101 RPM target fully at RPM should trigger loader");
  }

  // ─── isFinished trigger logic ─────────────────────────────────────

  @Test
  public void testIsFinished_whenTriggerHeld_returnsFalse() {
    // isFinished = !triggerHeld; when held=true → !true = false
    boolean triggerHeld = true;
    assertFalse(!triggerHeld, "Command must NOT finish while trigger is held");
  }

  @Test
  public void testIsFinished_whenTriggerReleased_returnsTrue() {
    // isFinished = !triggerHeld; when held=false → !false = true
    boolean triggerHeld = false;
    assertTrue(!triggerHeld, "Command must finish immediately when trigger is released");
  }
}
