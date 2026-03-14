package frc.robot.constants;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The SpeedConstants class provides configurable max speed (1-100) and sensitivity (1-100) for
 * every motor setup. Back motors are excluded as they are linked to the front motors.
 */
public final class SpeedConstants {
  // Drive Train (Back motors linked to front)
  public static double FRONT_RIGHT_MAX_SPEED = 85.0; // Reduced from 100 to 75
  public static double FRONT_RIGHT_SENSITIVITY = 100.0;

  public static double FRONT_LEFT_MAX_SPEED = 85.0; // Reduced from 100 to 75
  public static double FRONT_LEFT_SENSITIVITY = 100.0;

  // Intake Subsystem
  public static double INTAKE_MAIN_MAX_SPEED = 100.0;
  public static double INTAKE_MAIN_SENSITIVITY = 100.0;

  public static double INTAKE_SECONDARY_MAX_SPEED = 100.0;
  public static double INTAKE_SECONDARY_SENSITIVITY = 100.0;

  // Loader Subsystem
  public static double LOADER_1_MAX_SPEED = 100.0;
  public static double LOADER_1_SENSITIVITY = 100.0;

  public static double LOADER_2_MAX_SPEED = 100.0;
  public static double LOADER_2_SENSITIVITY = 100.0;

  public static double LOADER_3_MAX_SPEED = 100.0;
  public static double LOADER_3_SENSITIVITY = 100.0;

  // Turret Subsystem
  public static double TURRET_MAX_SPEED = 50.0;
  public static double TURRET_SENSITIVITY = 30.0;

  // Fire Subsystem
  public static double FIRE_MAX_SPEED = 100.0;
  public static double FIRE_SENSITIVITY = 100.0;

  public static boolean TUNING_MODE = false;
  private static boolean initialized = false;

  public static void syncNetworkTables() {
    if (!initialized) {
      SmartDashboard.putNumber("Speed/FRONT_RIGHT_MAX_SPEED", FRONT_RIGHT_MAX_SPEED);
      SmartDashboard.putNumber("Speed/FRONT_RIGHT_SENSITIVITY", FRONT_RIGHT_SENSITIVITY);
      SmartDashboard.putNumber("Speed/FRONT_LEFT_MAX_SPEED", FRONT_LEFT_MAX_SPEED);
      SmartDashboard.putNumber("Speed/FRONT_LEFT_SENSITIVITY", FRONT_LEFT_SENSITIVITY);
      SmartDashboard.putNumber("Speed/INTAKE_MAIN_MAX_SPEED", INTAKE_MAIN_MAX_SPEED);
      SmartDashboard.putNumber("Speed/INTAKE_MAIN_SENSITIVITY", INTAKE_MAIN_SENSITIVITY);
      SmartDashboard.putNumber("Speed/INTAKE_SECONDARY_MAX_SPEED", INTAKE_SECONDARY_MAX_SPEED);
      SmartDashboard.putNumber("Speed/INTAKE_SECONDARY_SENSITIVITY", INTAKE_SECONDARY_SENSITIVITY);
      SmartDashboard.putNumber("Speed/LOADER_1_MAX_SPEED", LOADER_1_MAX_SPEED);
      SmartDashboard.putNumber("Speed/LOADER_1_SENSITIVITY", LOADER_1_SENSITIVITY);
      SmartDashboard.putNumber("Speed/LOADER_2_MAX_SPEED", LOADER_2_MAX_SPEED);
      SmartDashboard.putNumber("Speed/LOADER_2_SENSITIVITY", LOADER_2_SENSITIVITY);
      SmartDashboard.putNumber("Speed/LOADER_3_MAX_SPEED", LOADER_3_MAX_SPEED);
      SmartDashboard.putNumber("Speed/LOADER_3_SENSITIVITY", LOADER_3_SENSITIVITY);
      SmartDashboard.putNumber("Speed/TURRET_MAX_SPEED", TURRET_MAX_SPEED);
      SmartDashboard.putNumber("Speed/TURRET_SENSITIVITY", TURRET_SENSITIVITY);
      SmartDashboard.putNumber("Speed/FIRE_MAX_SPEED", FIRE_MAX_SPEED);
      SmartDashboard.putNumber("Speed/FIRE_SENSITIVITY", FIRE_SENSITIVITY);
      initialized = true;
    } else if (TUNING_MODE) {
      FRONT_RIGHT_MAX_SPEED =
          SmartDashboard.getNumber("Speed/FRONT_RIGHT_MAX_SPEED", FRONT_RIGHT_MAX_SPEED);
      FRONT_RIGHT_SENSITIVITY =
          SmartDashboard.getNumber("Speed/FRONT_RIGHT_SENSITIVITY", FRONT_RIGHT_SENSITIVITY);
      FRONT_LEFT_MAX_SPEED =
          SmartDashboard.getNumber("Speed/FRONT_LEFT_MAX_SPEED", FRONT_LEFT_MAX_SPEED);
      FRONT_LEFT_SENSITIVITY =
          SmartDashboard.getNumber("Speed/FRONT_LEFT_SENSITIVITY", FRONT_LEFT_SENSITIVITY);
      INTAKE_MAIN_MAX_SPEED =
          SmartDashboard.getNumber("Speed/INTAKE_MAIN_MAX_SPEED", INTAKE_MAIN_MAX_SPEED);
      INTAKE_MAIN_SENSITIVITY =
          SmartDashboard.getNumber("Speed/INTAKE_MAIN_SENSITIVITY", INTAKE_MAIN_SENSITIVITY);
      INTAKE_SECONDARY_MAX_SPEED =
          SmartDashboard.getNumber("Speed/INTAKE_SECONDARY_MAX_SPEED", INTAKE_SECONDARY_MAX_SPEED);
      INTAKE_SECONDARY_SENSITIVITY =
          SmartDashboard.getNumber(
              "Speed/INTAKE_SECONDARY_SENSITIVITY", INTAKE_SECONDARY_SENSITIVITY);
      LOADER_1_MAX_SPEED = SmartDashboard.getNumber("Speed/LOADER_1_MAX_SPEED", LOADER_1_MAX_SPEED);
      LOADER_1_SENSITIVITY =
          SmartDashboard.getNumber("Speed/LOADER_1_SENSITIVITY", LOADER_1_SENSITIVITY);
      LOADER_2_MAX_SPEED = SmartDashboard.getNumber("Speed/LOADER_2_MAX_SPEED", LOADER_2_MAX_SPEED);
      LOADER_2_SENSITIVITY =
          SmartDashboard.getNumber("Speed/LOADER_2_SENSITIVITY", LOADER_2_SENSITIVITY);
      LOADER_3_MAX_SPEED = SmartDashboard.getNumber("Speed/LOADER_3_MAX_SPEED", LOADER_3_MAX_SPEED);
      LOADER_3_SENSITIVITY =
          SmartDashboard.getNumber("Speed/LOADER_3_SENSITIVITY", LOADER_3_SENSITIVITY);
      TURRET_MAX_SPEED = SmartDashboard.getNumber("Speed/TURRET_MAX_SPEED", TURRET_MAX_SPEED);
      TURRET_SENSITIVITY = SmartDashboard.getNumber("Speed/TURRET_SENSITIVITY", TURRET_SENSITIVITY);
      FIRE_MAX_SPEED = SmartDashboard.getNumber("Speed/FIRE_MAX_SPEED", FIRE_MAX_SPEED);
      FIRE_SENSITIVITY = SmartDashboard.getNumber("Speed/FIRE_SENSITIVITY", FIRE_SENSITIVITY);
    }
  }

  /**
   * Applies sensitivity and max speed limits to a motor speed input.
   *
   * @param input The original speed input (-1.0 to 1.0)
   * @param maxSpeed Max speed constant (1.0 to 100.0)
   * @param sensitivity Sensitivity constant (1.0 to 100.0)
   * @return The adjusted speed
   */
  public static double adjustSpeed(double input, double maxSpeed, double sensitivity) {
    double max = maxSpeed / 100.0;
    double sens = sensitivity / 100.0;

    // Apply sensitivity curve: output = input * sens + input^3 * (1 - sens)
    double curvedInput = (input * sens) + (Math.pow(input, 3) * (1.0 - sens));

    // Apply max speed limit
    return curvedInput * max;
  }
}
