package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

/**
 * The main robot class, responsible for managing the robot lifecycle.
 * <p>
 * This class extends {@link TimedRobot} and delegates subsystem and command
 * initialization to {@link RobotContainer}. It handles periodic methods and
 * schedules commands for autonomous and teleoperated modes.
 */
public class Robot extends TimedRobot {
  private Command m_autonomousCommand;
  private RobotContainer m_robotContainer;

  @Override
  public void robotInit() {
    // Instantiate our RobotContainer. This will perform all our button bindings
    m_robotContainer = new RobotContainer();
  }

  @Override
  public void robotPeriodic() {
    // RUNS THE SCHEDULER. CRITICAL.
    CommandScheduler.getInstance().run();
  }

  @Override
  public void autonomousInit() {
    m_autonomousCommand = m_robotContainer.getAutonomousCommand();
    if (m_autonomousCommand != null) {
      m_autonomousCommand.schedule();
    }
  }

  @Override
  public void teleopInit() {
    if (m_autonomousCommand != null) {
      m_autonomousCommand.cancel();
    }
  }
}
