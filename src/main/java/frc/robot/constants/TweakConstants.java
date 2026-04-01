package frc.robot.constants;

/**
 * TweakConstants holds toggleable features for the robot. These are modified by the C++ GUI prior
 * to deployment.
 */
public final class TweakConstants {
  // 1. LIMIT_DRIVE_SPEED_TO_75
  public static boolean LIMIT_DRIVE_SPEED_TO_75 = false;
  // 2. INVERT_DRIVE_CONTROLS
  public static boolean INVERT_DRIVE_CONTROLS = false;
  // 3. BOOST_MODE_OVERRIDE
  public static boolean BOOST_MODE_OVERRIDE = false;
  // 4. SLOW_MODE_MODIFIER_ACTIVE
  public static boolean SLOW_MODE_MODIFIER_ACTIVE = false;
  // 5. ENABLE_DYNAMIC_BRAKING
  public static boolean ENABLE_DYNAMIC_BRAKING = true;
  // 7. ENABLE_LED_DIAGNOSTICS
  public static boolean ENABLE_LED_DIAGNOSTICS = true;
  // 8. RECORD_TELEMETRY_TO_USB
  public static boolean RECORD_TELEMETRY_TO_USB = true;
  // 9. MUTE_DASHBOARD_ALERTS
  public static boolean MUTE_DASHBOARD_ALERTS = false;
  // 10. ENABLE_PIT_HEALTH_CHECK_ON_START
  public static boolean ENABLE_PIT_HEALTH_CHECK_ON_START = false;
  // 11. ENABLE_TURRET_UNWIND
  public static boolean ENABLE_TURRET_UNWIND = true;
  // 12. USE_RUST_LEAD_CALCULATOR
  public static boolean USE_RUST_LEAD_CALCULATOR = true;
  // 13. ALLOW_FIRE_WHILE_MOVING
  public static boolean ALLOW_FIRE_WHILE_MOVING = false;
  // 14. DISABLE_INTAKE_DURING_FIRE
  public static boolean DISABLE_INTAKE_DURING_FIRE = true;
  // 15. SMOOTH_LOADER_MOTORS
  public static boolean SMOOTH_LOADER_MOTORS = true;
  // 18. USE_PROFILED_PID_FOR_TURRET
  public static boolean USE_PROFILED_PID_FOR_TURRET = true;
  // 19. REVERSE_TURRET_DIRECTION
  public static boolean REVERSE_TURRET_DIRECTION = false;
  // 21. DISABLE_BROWNOUT_PROTECTION
  public static boolean DISABLE_BROWNOUT_PROTECTION = false;
  // 22. ENABLE_STALL_DETECTION
  public static boolean ENABLE_STALL_DETECTION = true;
  // 23. IGNORE_LIMIT_SWITCHES
  public static boolean IGNORE_LIMIT_SWITCHES = false;
  // 24. TESTING_MODE_BYPASS_SENSORS
  public static boolean TESTING_MODE_BYPASS_SENSORS = false;
  // 25. OVERRIDE_BATTERY_SENSE
  public static boolean OVERRIDE_BATTERY_SENSE = false;
  // 29. ENABLE_HAPTIC_FEEDBACK
  public static boolean ENABLE_HAPTIC_FEEDBACK = true;
  // 30. FORCE_RED_ALLIANCE_MODE
  public static boolean FORCE_RED_ALLIANCE_MODE = false;

  // --- New Advanced Tweaks ---
  // 44. BATTERY_SAGGING_ALERT
  public static boolean BATTERY_SAGGING_ALERT = true;
  // 46. IGNORE_SPINUP_TIME
  public static boolean IGNORE_SPINUP_TIME = false;
  // 48. AUTO_HOME_TURRET_ON_DISABLE
  public static boolean AUTO_HOME_TURRET_ON_DISABLE = false;
  // 52. DISPLAY_BIRDSEYE_MAP_DASHBOARD
  public static boolean DISPLAY_BIRDSEYE_MAP_DASHBOARD = true;
  // 59. FAST_BOOT_RIO_MODE
  public static boolean FAST_BOOT_RIO_MODE = false;
  // --- Future Prediction Options ---
  // ENABLE_AI_TARGET_PREDICTION
  public static boolean ENABLE_AI_TARGET_PREDICTION = false;
  // KINEMATIC_DRIVE_SMOOTHING
  public static boolean KINEMATIC_DRIVE_SMOOTHING = false;
}
