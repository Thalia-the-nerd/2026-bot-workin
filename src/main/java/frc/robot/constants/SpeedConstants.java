package frc.robot.constants;

/**
 * The SpeedConstants class provides configurable max speed (1-100) and sensitivity (1-100) for
 * every motor setup. Back motors are excluded as they are linked to the front motors.
 */
public final class SpeedConstants {
  // Drive Train (Back motors linked to front)
  public static double FRONT_RIGHT_MAX_SPEED = 90.0; // Reduced from 100 to 75
  public static double FRONT_RIGHT_SENSITIVITY = 100.0;

  public static double FRONT_LEFT_MAX_SPEED = 90.0; // Reduced from 100 to 75
  public static double FRONT_LEFT_SENSITIVITY = 100.0;

  // Intake Subsystem
  public static double INTAKE_RUN_MAX_SPEED = 100.0;
  public static double INTAKE_RUN_SENSITIVITY = 100.0;

  public static double INTAKE_PIVOT_MAX_SPEED = 20.0;
  public static double INTAKE_PIVOT_SENSITIVITY = 100.0;

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
  public static double FIRE_MAX_SPEED = 5000.0;
  public static double FIRE_SENSITIVITY = 100.0;

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
