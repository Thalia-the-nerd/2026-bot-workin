package frc.robot;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import frc.robot.utils.AutoAimCalculations;
import frc.robot.utils.AutoAimCalculations.AimResult;
import org.junit.jupiter.api.Test;

/** Tests for AutoAimCalculations - all pure math, no HAL needed. */
public class AutoAimCalculationsTest {

  private static final double DELTA = 1e-4;

  // Target pose at (5.0, 0.0, 2.5) metres – directly ahead, 2.5 m up
  private final Pose3d TARGET_AHEAD = new Pose3d(5.0, 0.0, 2.5, new Rotation3d());

  // Robot stationary at origin, facing 0 degrees
  private final Pose2d ROBOT_ORIGIN = new Pose2d(0, 0, new Rotation2d(0));

  // ─── RPM clamping ────────────────────────────────────────────────

  @Test
  public void testRPM_isNeverNegative() {
    AimResult result =
        AutoAimCalculations.calculateLead(new ChassisSpeeds(), ROBOT_ORIGIN, TARGET_AHEAD, 0.5);
    assertTrue(result.targetRPM >= 0, "targetRPM must never be negative");
  }

  @Test
  public void testRPM_isNeverAbove5500() {
    // Extreme height target to push RPM high
    Pose3d veryHighTarget = new Pose3d(1.0, 0.0, 50.0, new Rotation3d());
    AimResult result =
        AutoAimCalculations.calculateLead(new ChassisSpeeds(), ROBOT_ORIGIN, veryHighTarget, 0.0);
    assertTrue(result.targetRPM <= 5500, "targetRPM must be clamped to 5500");
  }

  // ─── Yaw direction ───────────────────────────────────────────────

  @Test
  public void testYaw_targetDirectlyAhead_isNearZero() {
    // Target is at (5, 0) – directly ahead, so yaw to aim should be ~0 radians
    AimResult result =
        AutoAimCalculations.calculateLead(new ChassisSpeeds(), ROBOT_ORIGIN, TARGET_AHEAD, 0.5);
    assertEquals(0.0, result.desiredYaw, 0.15, "Yaw for target directly ahead should be ~0 rad");
  }

  @Test
  public void testYaw_targetToTheLeft_isPositive() {
    // Target at (0, 5) – directly to the left (positive Y), yaw should be positive (~PI/2)
    Pose3d leftTarget = new Pose3d(0.0, 5.0, 1.0, new Rotation3d());
    AimResult result =
        AutoAimCalculations.calculateLead(new ChassisSpeeds(), ROBOT_ORIGIN, leftTarget, 0.5);
    assertTrue(result.desiredYaw > 0, "Target on the left should give positive yaw");
  }

  @Test
  public void testYaw_targetToTheRight_isNegative() {
    // Target at (0, -5) – to the right (negative Y)
    Pose3d rightTarget = new Pose3d(0.0, -5.0, 1.0, new Rotation3d());
    AimResult result =
        AutoAimCalculations.calculateLead(new ChassisSpeeds(), ROBOT_ORIGIN, rightTarget, 0.5);
    assertTrue(result.desiredYaw < 0, "Target on the right should give negative yaw");
  }

  // ─── Moving robot lead compensation ──────────────────────────────

  @Test
  public void testLead_movingRobot_differentYawThanStationary() {
    ChassisSpeeds movingRight = new ChassisSpeeds(3.0, 0, 0);
    Pose2d robotFacingRight = new Pose2d(0, 0, new Rotation2d(0));
    Pose3d sideTarget = new Pose3d(5.0, 5.0, 1.0, new Rotation3d());

    AimResult stationary =
        AutoAimCalculations.calculateLead(new ChassisSpeeds(), robotFacingRight, sideTarget, 0.5);
    AimResult moving =
        AutoAimCalculations.calculateLead(movingRight, robotFacingRight, sideTarget, 0.5);

    // When moving, lead compensation should shift the yaw
    assertNotEquals(
        stationary.desiredYaw,
        moving.desiredYaw,
        0.05,
        "Moving robot should produce different yaw than stationary");
  }

  @Test
  public void testLead_highSpeedRobot_stillClampsRPM() {
    ChassisSpeeds fastRobot = new ChassisSpeeds(10.0, 0, 0);
    Pose2d robotPose = new Pose2d(0, 0, new Rotation2d(0));
    Pose3d farTarget = new Pose3d(20.0, 0.0, 5.0, new Rotation3d());

    AimResult result = AutoAimCalculations.calculateLead(fastRobot, robotPose, farTarget, 0.5);
    assertTrue(result.targetRPM >= 0, "RPM must be non-negative even at high speed");
    assertTrue(result.targetRPM <= 5500, "RPM must be clamped even at high speed");
  }

  // ─── Turret height factor ─────────────────────────────────────────

  @Test
  public void testRPM_higherTarget_requiresMoreRPM() {
    Pose3d lowTarget = new Pose3d(5.0, 0.0, 1.0, new Rotation3d());
    Pose3d highTarget = new Pose3d(5.0, 0.0, 4.0, new Rotation3d());

    AimResult lowResult =
        AutoAimCalculations.calculateLead(new ChassisSpeeds(), ROBOT_ORIGIN, lowTarget, 0.5);
    AimResult highResult =
        AutoAimCalculations.calculateLead(new ChassisSpeeds(), ROBOT_ORIGIN, highTarget, 0.5);

    assertTrue(
        highResult.targetRPM > lowResult.targetRPM,
        "Higher target should require more RPM than lower target");
  }

  @Test
  public void testRPM_sameTargetDifferentTurretHeight_affectsRPM() {
    AimResult lowTurret =
        AutoAimCalculations.calculateLead(new ChassisSpeeds(), ROBOT_ORIGIN, TARGET_AHEAD, 0.0);
    AimResult highTurret =
        AutoAimCalculations.calculateLead(new ChassisSpeeds(), ROBOT_ORIGIN, TARGET_AHEAD, 2.0);

    assertNotEquals(
        lowTurret.targetRPM,
        highTurret.targetRPM,
        1.0,
        "Different turret heights should produce different RPMs");
  }
}
