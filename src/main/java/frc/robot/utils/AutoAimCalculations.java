package frc.robot.utils;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;

public class AutoAimCalculations {
  public static final double GRAVITY = 9.81;
  public static final double PROJECTILE_SPEED = 20.0; // Fixed horizontal launch speed

  public static class AimResult {
    public final double desiredYaw; // in radians, field-relative
    public final double targetRPM;

    public AimResult(double desiredYaw, double targetRPM) {
      this.desiredYaw = desiredYaw;
      this.targetRPM = targetRPM;
    }
  }

  /**
   * Calculates the required turret yaw and shooter RPM to hit a target while moving.
   *
   * @param robotSpeeds The current chassis speeds (vx, vy, omega) of the robot.
   * @param robotPose The current field-relative pose of the robot.
   * @param targetPose The field-relative pose of the target.
   * @param robotTurretHeight The height of the turret off the ground in meters.
   * @return AimResult containing the desired field-relative turret yaw and required shooter RPM.
   */
  public static AimResult calculateLead(
      ChassisSpeeds robotSpeeds, Pose2d robotPose, Pose3d targetPose, double robotTurretHeight) {

    // For a differential drive, all velocity is along the heading.
    double v_long = robotSpeeds.vxMetersPerSecond;
    double robot_vx = v_long * robotPose.getRotation().getCos();
    double robot_vy = v_long * robotPose.getRotation().getSin();

    // Field coordinates
    double rx = robotPose.getX();
    double ry = robotPose.getY();

    double tx = targetPose.getX();
    double ty = targetPose.getY(); // Y is equivalent to Z in the Rust 3D sim
    double tz = targetPose.getZ(); // Z is height in FRC Pose3d

    // Distance from robot to target
    double dx = tx - rx;
    double dy = ty - ry;

    // Quadratic lead solver for time of flight (t)
    double v_muzzle = PROJECTILE_SPEED;
    double a = v_muzzle * v_muzzle - (robot_vx * robot_vx) - (robot_vy * robot_vy);
    double b = 2.0 * (dx * robot_vx + dy * robot_vy);
    double c = -(dx * dx + dy * dy);

    double best_t = 0.1;
    double discriminant = b * b - 4.0 * a * c;

    double desired_yaw = 0.0;

    if (discriminant >= 0.0 && a != 0.0) {
      double t1 = (-b + Math.sqrt(discriminant)) / (2.0 * a);
      double t2 = (-b - Math.sqrt(discriminant)) / (2.0 * a);

      // Earliest positive hit
      if (t1 > 0.0 && t2 > 0.0) {
        best_t = Math.min(t1, t2);
      } else if (t1 > 0.0) {
        best_t = t1;
      } else if (t2 > 0.0) {
        best_t = t2;
      }

      if (best_t > 0.0) {
        // Compute target-relative exit velocity direction
        double req_vx_rel = (dx / best_t) - robot_vx;
        double req_vy_rel = (dy / best_t) - robot_vy;
        desired_yaw = Math.atan2(req_vy_rel, req_vx_rel);
      }
    } else {
      // If mathematical lock is impossible, fallback to pointing directly at target
      desired_yaw = Math.atan2(dy, dx);
      double dist = Math.hypot(dx, dy);
      best_t = dist / v_muzzle;
      if (best_t <= 0) best_t = 0.1;
    }

    // Vertical component (height/Z in WPILib)
    double heightDiff = tz - robotTurretHeight;
    double vz_launch = (heightDiff + 0.5 * GRAVITY * best_t * best_t) / best_t;

    // Calculate RPM
    double fire_speed_total = Math.sqrt(v_muzzle * v_muzzle + vz_launch * vz_launch);
    // Multiply by roughly 215 to map to a 5000 RPM 1:1 NEO mechanism (scaling from Rust sim)
    double targetRPM = fire_speed_total * 215.0;

    // Clamp
    targetRPM = Math.max(0, Math.min(targetRPM, 5500));

    return new AimResult(desired_yaw, targetRPM);
  }
}
