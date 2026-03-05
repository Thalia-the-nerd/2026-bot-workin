package frc.robot;

/**
 * The SpeedConstants class provides configurable max speed (1-100) and sensitivity (1-100) for
 * every motor setup. Back motors are excluded as they are linked to the front motors.
 */
public final class SpeedConstants {
  // Drive Train (Back motors linked to front)
  public static final double FRONT_RIGHT_MAX_SPEED = 100.0;
  public static final double FRONT_RIGHT_SENSITIVITY = 100.0;

  public static final double FRONT_LEFT_MAX_SPEED = 100.0;
  public static final double FRONT_LEFT_SENSITIVITY = 100.0;

  // Intake Subsystem
  public static final double INTAKE_MAIN_MAX_SPEED = 100.0;
  public static final double INTAKE_MAIN_SENSITIVITY = 100.0;

  public static final double INTAKE_SECONDARY_MAX_SPEED = 100.0;
  public static final double INTAKE_SECONDARY_SENSITIVITY = 100.0;

  // Loader Subsystem
  public static final double LOADER_1_MAX_SPEED = 100.0;
  public static final double LOADER_1_SENSITIVITY = 100.0;

  public static final double LOADER_2_MAX_SPEED = 100.0;
  public static final double LOADER_2_SENSITIVITY = 100.0;

  public static final double LOADER_3_MAX_SPEED = 100.0;
  public static final double LOADER_3_SENSITIVITY = 100.0;

  // Turret Subsystem
  public static final double TURRET_MAX_SPEED = 100.0;
  public static final double TURRET_SENSITIVITY = 100.0;

  // Fire Subsystem
  public static final double FIRE_MAX_SPEED = 100.0;
  public static final double FIRE_SENSITIVITY = 100.0;

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
