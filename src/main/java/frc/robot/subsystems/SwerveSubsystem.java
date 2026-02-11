package frc.robot.subsystems;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.io.File;
import swervelib.SwerveDrive;
import swervelib.parser.SwerveParser;

public class SwerveSubsystem extends SubsystemBase {

  // The heavy lifting object from YAGSL
  // Note: This is null during simulation/testing when RobotBase.isReal() returns false
  private SwerveDrive swerveDrive;

  // Field2d object for visualization in simulation and SmartDashboard
  private final Field2d m_field = new Field2d();

  // Maximum speed in Meters/Second. Adjust this to your specific robot gearing/safety needs.
  // 4.5 m/s is a standard fast speed for L2 gearing.
  public double maximumSpeed = 4.5;

  public SwerveSubsystem(File directory) {
    if (RobotBase.isReal()) {
      try {
        // Configure the Telemetry to be less spammy (optional)
        // SwerveTelemetry.verbosity = SwerveTelemetry.TelemetryVerbosity.LOW;

        // Load the JSON configuration
        this.swerveDrive = new SwerveParser(directory).createSwerveDrive(maximumSpeed);

      } catch (Exception e) {
        throw new RuntimeException(
            "CRITICAL: YAGSL Failed to load. Check JSON paths. \n" + e.getMessage());
      }
    }
    SmartDashboard.putData("Field", m_field);
  }

  // Overloaded constructor for default path
  public SwerveSubsystem() {
    this(new File(Filesystem.getDeployDirectory(), "swerve"));
  }

  /**
   * The primary drive method.
   *
   * @param translation The X/Y translation vector (forward/strafe)
   * @param rotation The Z rotation value
   * @param fieldRelative True for field-oriented control (standard), False for robot-oriented
   */
  public void drive(Translation2d translation, double rotation, boolean fieldRelative) {
    if (swerveDrive != null) {
      swerveDrive.drive(translation, rotation, fieldRelative, false);
    }
  }

  @Override
  public void periodic() {
    // This updates the odometry (robot position on field).
    // ABSOLUTELY REQUIRED for the robot to know where it is.
    if (swerveDrive != null) {
      swerveDrive.updateOdometry();
      // Update the Field2d visualization with current robot pose
      m_field.setRobotPose(swerveDrive.getPose());
    }
  }

  @Override
  public void simulationPeriodic() {
    // Update the Field2d object with the current robot pose during simulation
    if (swerveDrive != null) {
      m_field.setRobotPose(swerveDrive.getPose());
    } else {
      // In simulation without hardware, use a default pose
      m_field.setRobotPose(new Pose2d());
    }
  }

  // Helper to get the drive object if needed for advanced features (PathPlanner)
  public SwerveDrive getSwerveDrive() {
    return swerveDrive;
  }
}
