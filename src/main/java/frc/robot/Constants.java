// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.RobotBase;

/**
 * The Constants class provides a convenient place for teams to hold robot-wide numerical or boolean
 * constants. This class should not be used for any other purpose. All constants should be declared
 * globally (i.e. public static). Do not put anything functional in this class.
 *
 * <p>It is advised to statically import this class (or one of its inner classes) wherever the
 * constants are needed, to reduce verbosity.
 */
public final class Constants {
  public static final class CANConstants {
    // CAN Bus Devices
    /// Drive Train Motors
    public static final int MOTOR_FRONT_RIGHT_ID = -1;
    public static final int MOTOR_BACK_RIGHT_ID = -1;
    public static final int MOTOR_FRONT_LEFT_ID = -1;
    public static final int MOTOR_BACK_LEFT_ID = -1;

    /// Elevator Motors
    public static final int MOTOR_ELEVATOR_LEFT_ID = -1;
    public static final int MOTOR_ELEVATOR_RIGHT_ID = -1;

    /// Arm Motors
    public static final int MOTOR_ARM_MAIN_ID = -1;

    /// Shooter Motors
    public static final int MOTOR_SHOOTER_LEFT_ID = -1;
    public static final int MOTOR_SHOOTER_RIGHT_ID = -1;

    /// Lifter Motors
    public static final int MOTOR_LIFTER_LEFT_ID = -1;
    public static final int MOTOR_LIFTER_RIGHT_ID = -1;
  }

  // Is simulation
  public static final Mode SIM_MODE = Mode.SIM;
  public static final Mode CURRENT_MODE = RobotBase.isReal() ? Mode.REAL : SIM_MODE;

  public static enum Mode {
    /** Running on a real robot. */
    REAL,

    /** Running a physics simulator. */
    SIM,

    /** Replaying from a log file. */
    REPLAY
  }

  // Max speeds
  public static final double MAX_SPEED = 0.75;

  // USB Devices
  public static final int CONTROLLER_USB_INDEX = 0;
  public static final int FLIGHTSTICK_USB_INDEX = 1;
  // On-Controller joystick deadzone
  public static final double CONTROLLER_DEAD_ZONE = 0.1;

  // Joystick buttons
  public static final int JOYSTICK_DEFAULT_BUTTON = 1;
  public static final int JOYSTICK_TRIGGER = 1;
}
