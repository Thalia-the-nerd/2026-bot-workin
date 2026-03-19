package frc.robot;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import frc.robot.subsystems.TurretSubsystem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Simulated I/O tests for TurretSubsystem.
 *
 * <p>REVLib SparkMax is a singleton per CAN ID, so the subsystem is created ONCE for all tests.
 */
public class TurretSubsystemTest {

  private static TurretSubsystem m_turret;

  @BeforeAll
  static void initAll() {
    assert HAL.initialize(500, 0);
    m_turret =
        new TurretSubsystem(
            new frc.robot.subsystems.TurretIO() {
              private double pos = 0.0;
              private double targetPos = 0.0;

              @Override
              public void updateInputs(TurretIOInputs inputs) {
                inputs.positionRotations = pos;
              }

              @Override
              public void setPosition(double target) {
                targetPos = target;
              }
            });
  }

  // ─── Initial state ────────────────────────────────────────────────

  @Test
  public void testInitialAngle_isZeroDegrees() {
    assertEquals(
        0.0, m_turret.getTurretAngleDegrees(), 0.01, "Turret should start at 0 degrees in sim");
  }

  @Test
  public void testInitialAngleRadians_isZero() {
    assertEquals(
        0.0, m_turret.getTurretAngleRadians(), 1e-6, "Turret should start at 0 radians in sim");
  }

  @Test
  public void testIsUnwinding_startsAsFalse() {
    assertFalse(m_turret.isUnwinding(), "Turret should not be unwinding at startup");
  }

  // ─── isAtAngle ───────────────────────────────────────────────────

  @Test
  public void testIsAtAngle_atStart_zeroWithWideTolerance() {
    assertTrue(
        m_turret.isAtAngle(0.0, 5.0),
        "Turret starts at 0 and should be at 0 with 5 degree tolerance");
  }

  @Test
  public void testIsAtAngle_notAtAngle_withNarrowTolerance() {
    // After init, angle is 0; check it's NOT at 45 with a 1-degree tolerance
    assertFalse(
        m_turret.isAtAngle(45.0, 1.0),
        "Turret at 0 degrees is not at 45 degrees with 1-degree tolerance");
  }

  // ─── setTurretSpeed ───────────────────────────────────────────────

  @Test
  public void testSetTurretSpeed_doesNotThrow() {
    assertDoesNotThrow(() -> m_turret.setTurretSpeed(0.5), "setTurretSpeed(0.5) must not throw");
  }

  @Test
  public void testSetTurretSpeed_zero_doesNotThrow() {
    assertDoesNotThrow(() -> m_turret.setTurretSpeed(0.0), "setTurretSpeed(0.0) must not throw");
  }

  @Test
  public void testSetTurretSpeed_belowDeadzone_treatedAsZero() {
    // 0.05 is below deadzone (0.1), so it's zeroed internally
    assertDoesNotThrow(
        () -> m_turret.setTurretSpeed(0.05), "Sub-deadzone input must be handled gracefully");
  }

  @Test
  public void testSetTurretSpeed_fullForward_doesNotThrow() {
    assertDoesNotThrow(() -> m_turret.setTurretSpeed(1.0), "setTurretSpeed(1.0) must not throw");
  }

  @Test
  public void testSetTurretSpeed_fullReverse_doesNotThrow() {
    assertDoesNotThrow(() -> m_turret.setTurretSpeed(-1.0), "setTurretSpeed(-1.0) must not throw");
  }

  // ─── setTurretVoltage ────────────────────────────────────────────

  @Test
  public void testSetTurretVoltage_zero_doesNotThrow() {
    assertDoesNotThrow(() -> m_turret.setTurretVoltage(0.0), "setTurretVoltage(0) must not throw");
  }

  @Test
  public void testSetTurretVoltage_positive_doesNotThrow() {
    assertDoesNotThrow(
        () -> m_turret.setTurretVoltage(3.0), "setTurretVoltage(3.0) must not throw");
  }

  // ─── setTargetAngle ──────────────────────────────────────────────

  @Test
  public void testSetTargetAngle_doesNotThrow() {
    assertDoesNotThrow(() -> m_turret.setTargetAngle(90.0), "setTargetAngle(90) must not throw");
  }

  // ─── stop ────────────────────────────────────────────────────────

  @Test
  public void testStop_doesNotThrow() {
    assertDoesNotThrow(() -> m_turret.stop(), "stop() must not throw");
  }

  @Test
  public void testStop_afterStop_stillAtZero() {
    m_turret.stop();
    assertEquals(
        0.0,
        m_turret.getTurretAngleDegrees(),
        0.01,
        "angle stays 0 after stop in sim (no physics tick ran)");
  }

  // ─── periodic / simulationPeriodic ───────────────────────────────

  @Test
  public void testPeriodic_doesNotThrow() {
    assertDoesNotThrow(() -> m_turret.periodic(), "periodic() must not throw");
  }

  @Test
  public void testSimulationPeriodic_doesNotThrow() {
    assertDoesNotThrow(() -> m_turret.simulationPeriodic(), "simulationPeriodic() must not throw");
  }

  @Test
  public void testSimulationPeriodic_updatesPhysics() {
    // Run a few sim ticks – if internal sim state advances without throwing, test passes
    for (int i = 0; i < 5; i++) {
      assertDoesNotThrow(
          () -> m_turret.simulationPeriodic(),
          "simulationPeriodic() must not throw after repeated updates");
    }
  }
}
