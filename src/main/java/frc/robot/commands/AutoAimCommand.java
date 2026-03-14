package frc.robot.commands;

import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.CameraSubsystem;
import frc.robot.subsystems.DriveSubsystem;
import frc.robot.subsystems.TurretSubsystem;
import frc.robot.utils.AutoAimCalculations;

public class AutoAimCommand extends Command {
  private final TurretSubsystem m_turret;
  private final CameraSubsystem m_camera;
  private final DriveSubsystem m_drive;

  // Profiled PID for incredibly smooth tracking
  private final ProfiledPIDController m_yawController =
      new ProfiledPIDController(
          4.5, 0.0, 0.1, new TrapezoidProfile.Constraints(6.0, 8.0)); // Rad/s and Rad/s/s

  // Feedforward for smooth following
  private final SimpleMotorFeedforward m_feedforward = new SimpleMotorFeedforward(0.1, 0.5);

  // Hardcoded target pose (From CameraSubsystem.java where TargetModel is defined)
  private final Pose3d m_targetPose = new Pose3d(16, 4, 2, new Rotation3d(0, 0, Math.PI));
  private final double m_turretHeight = 0.5; // Roughly match Rust Sim

  public AutoAimCommand(TurretSubsystem turret, CameraSubsystem camera, DriveSubsystem drive) {
    m_turret = turret;
    m_camera = camera;
    m_drive = drive;

    // Turret wraps around so we make it continuous between -Pi and Pi
    m_yawController.enableContinuousInput(-Math.PI, Math.PI);
    m_yawController.setTolerance(Math.toRadians(1.0));

    // Use addRequirements() here to declare subsystem dependencies.
    addRequirements(turret);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    System.out.println("AutoAimCommand Scheduled - Handing over to AutoAim");
    m_yawController.reset(m_turret.getTurretAngleRadians());
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    // 1) Get current state from odometry
    Pose2d robotPose = m_drive.getPose();

    // 2) Get optimal lead (yaw + RPM) from Rust algorithm port
    AutoAimCalculations.AimResult result =
        AutoAimCalculations.calculateLead(
            m_drive.getSpeeds(), robotPose, m_targetPose, m_turretHeight);

    // 3) Calculate tracking using profiled PID and feedforward
    double currentAngle = m_turret.getTurretAngleRadians();

    // Field-relative desired yaw to robot-relative: subtract the robot's heading
    double robotRelativeDesiredYaw = result.desiredYaw - robotPose.getRotation().getRadians();

    double pidOut = m_yawController.calculate(currentAngle, robotRelativeDesiredYaw);
    double ffOut = m_feedforward.calculate(m_yawController.getSetpoint().velocity);

    // 4) Apply voltage
    m_turret.setTurretVoltage(pidOut + ffOut);
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false; // Run until interrupted/toggled off
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    m_turret.stop();
    System.out.println("AutoAimCommand Ended - Returning to Manual Control");
  }
}
