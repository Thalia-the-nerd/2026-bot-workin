package frc.robot;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.littletonrobotics.junction.Logger;

/**
 * Unified telemetry routing class. Replaces direct calls to SmartDashboard and Logger throughout
 * subsystems.
 */
public class RobotTelemetry {

  public static void putData(String key, Sendable data) {
    SmartDashboard.putData(key, data);
  }

  public static void putData(Sendable data) {
    SmartDashboard.putData(data);
  }

  public static void putNumber(String key, double value) {
    SmartDashboard.putNumber(key, value);
    Logger.recordOutput(key, value);
  }

  public static void putBoolean(String key, boolean value) {
    SmartDashboard.putBoolean(key, value);
    Logger.recordOutput(key, value);
  }

  public static void putString(String key, String value) {
    SmartDashboard.putString(key, value);
    Logger.recordOutput(key, value);
  }

  public static void recordOutput(String key, double value) {
    putNumber(key, value);
  }

  public static void recordOutput(String key, boolean value) {
    putBoolean(key, value);
  }

  public static void recordOutput(String key, String value) {
    putString(key, value);
  }

  public static void recordOutput(String key, Pose2d... pose) {
    Logger.recordOutput(key, pose);
  }

  public static void recordOutput(String key, Pose3d... pose) {
    Logger.recordOutput(key, pose);
  }

  public static void recordOutput(String key, Transform3d transform) {
    Logger.recordOutput(key, transform);
  }
}
