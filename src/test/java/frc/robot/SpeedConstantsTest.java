package frc.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import frc.robot.constants.SpeedConstants;
import org.junit.jupiter.api.Test;

public class SpeedConstantsTest {

  private static final double DELTA = 1e-4;

  @Test
  public void testZeroInput() {
    assertEquals(
        0.0,
        SpeedConstants.adjustSpeed(0.0, 100.0, 100.0),
        DELTA,
        "Zero input should yield zero output");
    assertEquals(
        0.0,
        SpeedConstants.adjustSpeed(0.0, 50.0, 0.0),
        DELTA,
        "Zero input should yield zero output regardless of parameters");
  }

  @Test
  public void testMaxInputMaxParams() {
    assertEquals(
        1.0,
        SpeedConstants.adjustSpeed(1.0, 100.0, 100.0),
        DELTA,
        "Max input with 100 speed and sensitivity should yield 1.0");
    assertEquals(
        -1.0,
        SpeedConstants.adjustSpeed(-1.0, 100.0, 100.0),
        DELTA,
        "Min input with 100 speed and sensitivity should yield -1.0");
  }

  @Test
  public void testLinearSensitivity() {
    // With 100% sensitivity, the curve is purely linear
    assertEquals(
        0.5,
        SpeedConstants.adjustSpeed(0.5, 100.0, 100.0),
        DELTA,
        "50% input with 100% sensitivity is 0.5");
    assertEquals(
        -0.5,
        SpeedConstants.adjustSpeed(-0.5, 100.0, 100.0),
        DELTA,
        "-50% input with 100% sensitivity is -0.5");
  }

  @Test
  public void testCubicSensitivity() {
    // With 0% sensitivity, the curve is purely cubic (input^3)
    // 0.5^3 = 0.125
    assertEquals(
        0.125,
        SpeedConstants.adjustSpeed(0.5, 100.0, 0.0),
        DELTA,
        "50% input with 0% sensitivity is purely cubic: 0.125");
    assertEquals(
        -0.125,
        SpeedConstants.adjustSpeed(-0.5, 100.0, 0.0),
        DELTA,
        "-50% input with 0% sensitivity is purely cubic: -0.125");
  }

  @Test
  public void testMixedSensitivity() {
    // With 50% sensitivity: output = input*0.5 + input^3 * 0.5
    // 0.5 input -> 0.5*0.5 + 0.125*0.5 = 0.25 + 0.0625 = 0.3125
    assertEquals(
        0.3125,
        SpeedConstants.adjustSpeed(0.5, 100.0, 50.0),
        DELTA,
        "50% input with 50% sensitivity should be exactly 0.3125");
  }

  @Test
  public void testSpeedLimits() {
    // With 50% max speed, outputs should be exactly half of the 100% speed equivalents
    assertEquals(
        0.5,
        SpeedConstants.adjustSpeed(1.0, 50.0, 100.0),
        DELTA,
        "Max input with 50% speed should yield 0.5");
    assertEquals(
        0.25,
        SpeedConstants.adjustSpeed(0.5, 50.0, 100.0),
        DELTA,
        "50% input with 50% speed (linear) should yield 0.25");
    assertEquals(
        0.0625,
        SpeedConstants.adjustSpeed(0.5, 50.0, 0.0),
        DELTA,
        "50% input with 50% speed (cubic) should yield 0.0625");
  }
}
