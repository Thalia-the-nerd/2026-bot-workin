package frc.robot.utils;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.DriveConstants;
import frc.robot.constants.PIDConstants;
import frc.robot.constants.SpeedConstants;
import java.lang.reflect.Field;

/**
 * A centralized debug system that synchronizes all robot constants and speeds to the
 * SmartDashboard. Setting "Debug/TUNING_MODE" to true allows overriding values live on the field.
 */
public class DebugDashboard {
  private static boolean initialized = false;

  public static void sync() {
    boolean tuningMode = SmartDashboard.getBoolean("Debug/TUNING_MODE", false);

    if (!initialized) {
      SmartDashboard.putBoolean("Debug/TUNING_MODE", false);

      syncClass(SpeedConstants.class, "Speed", false);
      syncClass(PIDConstants.class, "PID", false);
      syncClass(DriveConstants.class, "Drive", false);

      initialized = true;
    } else if (tuningMode) {
      syncClass(SpeedConstants.class, "Speed", true);
      syncClass(PIDConstants.class, "PID", true);
      syncClass(DriveConstants.class, "Drive", true);
    }
  }

  private static void syncClass(Class<?> clazz, String prefix, boolean readFromDashboard) {
    for (Field field : clazz.getDeclaredFields()) {
      if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
        try {
          // Skip internal state fields
          if (field.getName().equals("TUNING_MODE") || field.getName().equals("initialized"))
            continue;

          String key = prefix + "/" + field.getName();
          if (field.getType() == double.class) {
            if (readFromDashboard) {
              field.setDouble(null, SmartDashboard.getNumber(key, field.getDouble(null)));
            } else {
              SmartDashboard.putNumber(key, field.getDouble(null));
            }
          } else if (field.getType() == boolean.class) {
            if (readFromDashboard) {
              field.setBoolean(null, SmartDashboard.getBoolean(key, field.getBoolean(null)));
            } else {
              SmartDashboard.putBoolean(key, field.getBoolean(null));
            }
          }
        } catch (IllegalAccessException e) {
          // Ignore fields we can't access
        }
      }
    }
  }
}
