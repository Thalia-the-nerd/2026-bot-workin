package frc.robot;

import static org.junit.jupiter.api.Assertions.*;

import frc.robot.utils.HelperFunctions;
import org.junit.jupiter.api.Test;

/** Tests for HelperFunctions utility. */
public class HelperFunctionsTest {

  // ─── inDeadzone ──────────────────────────────────────────────────

  @Test
  public void testInDeadzone_zeroIsInDeadzone() {
    assertTrue(HelperFunctions.inDeadzone(0.0, 0.1), "0.0 should be inside a 0.1 deadzone");
  }

  @Test
  public void testInDeadzone_positiveInsideDeadzone() {
    assertTrue(HelperFunctions.inDeadzone(0.05, 0.1), "0.05 is inside 0.1 deadzone");
  }

  @Test
  public void testInDeadzone_negativeInsideDeadzone() {
    assertTrue(HelperFunctions.inDeadzone(-0.05, 0.1), "-0.05 is inside 0.1 deadzone");
  }

  @Test
  public void testInDeadzone_exactBoundaryIsOutside() {
    assertFalse(
        HelperFunctions.inDeadzone(0.1, 0.1), "Exactly on the deadzone boundary is NOT inside");
  }

  @Test
  public void testInDeadzone_valueOutsideDeadzone() {
    assertFalse(HelperFunctions.inDeadzone(0.5, 0.1), "0.5 is outside a 0.1 deadzone");
  }

  @Test
  public void testInDeadzone_fullInput_outsideDeadzone() {
    assertFalse(HelperFunctions.inDeadzone(1.0, 0.1), "Full 1.0 input is outside deadzone");
  }

  @Test
  public void testInDeadzone_negativeFullInput_outsideDeadzone() {
    assertFalse(HelperFunctions.inDeadzone(-1.0, 0.1), "Full -1.0 input is outside deadzone");
  }

  @Test
  public void testInDeadzone_zeroDeadzone_noValueIsInside() {
    // inDeadzone uses strict < so Math.abs(0.0) < 0.0 is false – even 0.0 is outside
    assertFalse(
        HelperFunctions.inDeadzone(0.0, 0.0), "0.0 is NOT inside a zero deadzone (strict <)");
    assertFalse(HelperFunctions.inDeadzone(0.001, 0.0), "Any non-zero is outside a zero deadzone");
  }

  // ─── inRange ─────────────────────────────────────────────────────

  @Test
  public void testInRange_targetEqualsValue_isInRange() {
    assertTrue(HelperFunctions.inRange(45.0, 45.0, 1.0), "Exact match is always in range");
  }

  @Test
  public void testInRange_withinTolerance() {
    assertTrue(HelperFunctions.inRange(45.0, 44.5, 1.0), "44.5 is within 1.0 of 45.0");
  }

  @Test
  public void testInRange_exactBoundaryIsOutside() {
    assertFalse(
        HelperFunctions.inRange(45.0, 44.0, 1.0), "Exactly 1.0 away is NOT within tolerance");
  }

  @Test
  public void testInRange_outsideTolerance() {
    assertFalse(HelperFunctions.inRange(45.0, 43.0, 1.0), "43.0 is outside 1.0 of 45.0");
  }

  @Test
  public void testInRange_negativeOffset_withinTolerance() {
    assertTrue(HelperFunctions.inRange(0.0, -0.05, 0.1), "-0.05 is within 0.1 of 0.0");
  }

  @Test
  public void testInRange_largeValues() {
    assertTrue(HelperFunctions.inRange(5000.0, 4990.0, 25.0), "4990 is within 25 of 5000");
    assertFalse(HelperFunctions.inRange(5000.0, 4970.0, 25.0), "4970 is NOT within 25 of 5000");
  }
}
