// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.Threads;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.constants.Constants;
import org.littletonrobotics.junction.LogFileUtil;
import org.littletonrobotics.junction.LoggedRobot;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.NT4Publisher;
import org.littletonrobotics.junction.wpilog.WPILOGReader;
import org.littletonrobotics.junction.wpilog.WPILOGWriter;
import org.littletonrobotics.urcl.URCL;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends LoggedRobot {
  private Command m_autonomousCommand;

  private RobotContainer m_robotContainer;

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  public Robot() {
    // Record metadata
    Logger.recordMetadata("ProjectName", BuildConstants.MAVEN_NAME);
    Logger.recordMetadata("BuildDate", BuildConstants.BUILD_DATE);
    Logger.recordMetadata("GitSHA", BuildConstants.GIT_SHA);
    Logger.recordMetadata("GitDate", BuildConstants.GIT_DATE);
    Logger.recordMetadata("GitBranch", BuildConstants.GIT_BRANCH);
    switch (BuildConstants.DIRTY) {
      case 0:
        Logger.recordMetadata("GitDirty", "All changes committed");
        break;
      case 1:
        Logger.recordMetadata("GitDirty", "Uncomitted changes");
        break;
      default:
        Logger.recordMetadata("GitDirty", "Unknown");
        break;
    }

    // Set up data receivers & replay source
    switch (Constants.CURRENT_MODE) {
      case REAL:
        // Running on a real robot, log to a USB stick ("/U/logs")
        if (frc.robot.constants.TweakConstants.RECORD_TELEMETRY_TO_USB) {
          java.io.File usb1 = new java.io.File("/media/sda1");
          java.io.File usb2 = new java.io.File("/media/sda2");
          if (usb1.exists() && usb1.isDirectory()) {
            Logger.addDataReceiver(new WPILOGWriter("/media/sda1/logs"));
            System.out.println("USB Logging initialized on /media/sda1");
          } else if (usb2.exists() && usb2.isDirectory()) {
            Logger.addDataReceiver(new WPILOGWriter("/media/sda2/logs"));
            System.out.println("USB Logging initialized on /media/sda2");
          } else {
            System.out.println("WARNING: NO USB STICK FOUND. FALLING BACK TO ROBORIO FLASH!");
            frc.robot.RobotTelemetry.putString(
                "Alerts/USB Logging", "No USB Drive Found! Logging to RIO Flash.");
            Logger.addDataReceiver(new WPILOGWriter("/home/lvuser/logs"));
          }
        }
        Logger.addDataReceiver(new NT4Publisher());
        break;

      case SIM:
        // Running a physics simulator, log to NT
        Logger.addDataReceiver(new NT4Publisher());
        break;

      case REPLAY:
        // Replaying a log, set up replay source
        setUseTiming(false); // Run as fast as possible
        String logPath = LogFileUtil.findReplayLog();
        Logger.setReplaySource(new WPILOGReader(logPath));
        Logger.addDataReceiver(new WPILOGWriter(LogFileUtil.addPathSuffix(logPath, "_sim")));
        break;
    }

    // Initialize URCL
    Logger.registerURCL(URCL.startExternal());

    // Start logging! No more data receivers, replay sources, or metadata values may be added.
    Logger.start();

    if (frc.robot.constants.TweakConstants.DISABLE_BROWNOUT_PROTECTION) {
      edu.wpi.first.wpilibj.RobotController.setBrownoutVoltage(0.0);
    }

    if (frc.robot.constants.TweakConstants.FAST_BOOT_RIO_MODE) {
      System.out.println("FAST BOOT RIO MODE ENABLED! Skipping deep init checks.");
    }

    if (frc.robot.constants.TweakConstants.ENABLE_PIT_HEALTH_CHECK_ON_START) {
      // Pit health check requested
      System.out.println("Pit Health Check routine requested on startup.");
    }

    // Instantiate our RobotContainer.  This will perform all our button bindings, and put our
    // autonomous chooser on the dashboard.
    m_robotContainer = new RobotContainer();
  }

  /**
   * This function is called every robot packet, no matter the mode. Use this for items like
   * diagnostics that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
    // Switch thread to high priority to improve loop timing
    Threads.setCurrentThreadPriority(true, 99);

    // Runs the Scheduler. This is responsible for polling buttons, adding
    // newly-scheduled commands, running already-scheduled commands, removing
    // finished or interrupted commands, and running subsystem periodic() methods.
    // This must be called from the robot's periodic block in order for anything in
    // the Command-based framework to work.
    CommandScheduler.getInstance().run();

    // This is a custom periodic function that runs for inter subsystem state updating
    m_robotContainer.periodic();

    // Return to normal thread priority
    Threads.setCurrentThreadPriority(false, 10);
    if (m_robotContainer.enableAutoProfiling) {
      System.out.println("WARNING, AUTO PROFILE IS ENABLED!");
    }

    // Check for Battery Sagging
    if (frc.robot.constants.TweakConstants.BATTERY_SAGGING_ALERT
        && !frc.robot.constants.TweakConstants.OVERRIDE_BATTERY_SENSE) {
      if (edu.wpi.first.wpilibj.RobotController.getBatteryVoltage() < 11.0) {
        System.out.println("WARNING: BATTERY VOLTAGE SAG DETECTED (< 11.0V)!");
      }
    }

    frc.robot.utils.DebugDashboard.sync();
  }

  /** This function is called once each time the robot enters Disabled mode. */
  @Override
  public void disabledInit() {
    if (frc.robot.constants.TweakConstants.AUTO_HOME_TURRET_ON_DISABLE
        && m_robotContainer != null) {
      m_robotContainer.disabledInit();
    }
  }

  @Override
  public void disabledPeriodic() {}

  /** This autonomous runs the autonomous command selected by your {@link RobotContainer} class. */
  @Override
  public void autonomousInit() {
    m_autonomousCommand = m_robotContainer.getAutonomousCommand();

    // schedule the autonomous command (example)
    if (m_autonomousCommand != null) {
      CommandScheduler.getInstance().schedule(m_autonomousCommand);
    }
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {}

  @Override
  public void teleopInit() {
    // This makes sure that the autonomous stops running when
    // teleop starts running. If you want the autonomous to
    // continue until interrupted by another command, remove
    // this line or comment it out.
    if (m_autonomousCommand != null) {
      m_autonomousCommand.cancel();
    }

    if (frc.robot.constants.TweakConstants.ENABLE_PIT_HEALTH_CHECK_ON_START
        && m_robotContainer != null) {
      edu.wpi.first.wpilibj2.command.CommandScheduler.getInstance()
          .schedule(m_robotContainer.getPitHealthCheckCommand());
    }
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {}

  @Override
  public void testInit() {
    // Cancels all running commands at the start of test mode.
    CommandScheduler.getInstance().cancelAll();
  }

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {}

  /** This function is called once when the robot is first started up. */
  @Override
  public void simulationInit() {}

  /** This function is called periodically whilst in simulation. */
  @Override
  public void simulationPeriodic() {}
}
