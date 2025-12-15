package frc.robot.subsystems;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.config.PIDConstants;
import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.controllers.PPHolonomicDriveController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.io.File;
import java.io.IOException;

import swervelib.SwerveDrive;
import swervelib.parser.SwerveParser;
import swervelib.telemetry.SwerveDriveTelemetry;
import swervelib.telemetry.SwerveDriveTelemetry.TelemetryVerbosity;

public class SwerveSubsystem extends SubsystemBase {

  // The heavy lifting object from YAGSL
  private final SwerveDrive swerveDrive;

  // Maximum speed in Meters/Second.
  public double maximumSpeed = 4.5;

  public SwerveSubsystem(File directory) {
    try {
      // Configure the Telemetry
      SwerveDriveTelemetry.verbosity = TelemetryVerbosity.HIGH;
      
      // Load the JSON configuration
      this.swerveDrive = new SwerveParser(directory).createSwerveDrive(maximumSpeed);

    } catch (Exception e) {
      throw new RuntimeException("CRITICAL: YAGSL Failed to load. Check JSON paths. \n" + e.getMessage());
    }
    
    // Initialize PathPlanner
    setupPathPlanner();
  }

  public SwerveSubsystem() {
    this(new File(Filesystem.getDeployDirectory(), "swerve"));
  }

  /**
   * Setup AutoBuilder for PathPlanner.
   */
  public void setupPathPlanner() {
    try {
      // Load the RobotConfig from the GUI settings (deploy/pathplanner/settings.json)
      RobotConfig config = RobotConfig.fromGUISettings();

      // Configure AutoBuilder
      AutoBuilder.configure(
          this::getPose,           // Robot pose supplier
          this::resetOdometry,     // Method to reset odometry
          this::getRobotVelocity,  // ChassisSpeeds supplier. MUST BE ROBOT RELATIVE.
          (speeds, feedforwards) -> swerveDrive.drive(speeds), // Method that will drive the robot
          new PPHolonomicDriveController( // Holonomic controller
              new PIDConstants(5.0, 0.0, 0.0), // Translation PID constants (Tune these!)
              new PIDConstants(5.0, 0.0, 0.0)  // Rotation PID constants (Tune these!)
          ),
          config, // The robot configuration
          () -> {
              // Boolean supplier that controls when the path will be mirrored for the red alliance
              // This will flip the path being followed to the red side of the field.
              // THE ORIGIN WILL REMAIN ON THE BLUE SIDE
              var alliance = DriverStation.getAlliance();
              return alliance.isPresent() && alliance.get() == DriverStation.Alliance.Red;
          },
          this // Reference to this subsystem to set requirements
      );

    } catch (Exception e) {
        // Handle the error if the config fails to load
        // This usually happens if you haven't deployed code via the GUI yet
        DriverStation.reportError("Failed to load PathPlanner config and configure AutoBuilder", e.getStackTrace());
    }
  }

  /**
   * Primary drive method for Teleop
   */
  public void drive(Translation2d translation, double rotation, boolean fieldRelative) {
    swerveDrive.drive(translation, rotation, fieldRelative, false);
  }

  // --- Helper Methods Required by AutoBuilder ---

  public Pose2d getPose() {
    return swerveDrive.getPose();
  }

  public void resetOdometry(Pose2d initialHolonomicPose) {
    swerveDrive.resetOdometry(initialHolonomicPose);
  }

  public ChassisSpeeds getRobotVelocity() {
    return swerveDrive.getRobotVelocity();
  }
  
  public SwerveDrive getSwerveDrive() {
    return swerveDrive;
  }
  
  @Override
  public void periodic() {
      swerveDrive.updateOdometry();
  }
}
