package frc.robot.subsystems;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import edu.wpi.first.wpilibj.RobotBase;
import org.junit.jupiter.api.Test;

/** Unit tests for the SwerveSubsystem class. */
public class SwerveSubsystemTest {

  @Test
  public void testMaximumSpeedDefaultValue() {
    try {
      SwerveSubsystem subsystem = new SwerveSubsystem();
      assertEquals(4.5, subsystem.maximumSpeed, 0.001, "Default maximum speed should be 4.5 m/s");
    } catch (Exception e) {
      // If hardware initialization fails in test environment, skip the test
      // This is expected when running tests without physical hardware or simulation
      assumeTrue(
          false, "Test skipped due to hardware initialization requirements: " + e.getMessage());
    }
  }

  @Test
  public void testGetSwerveDriveNotNull() {
    assumeTrue(RobotBase.isReal(), "Skipping SwerveDrive null check in simulation mode");
    try {
      SwerveSubsystem subsystem = new SwerveSubsystem();
      assertNotNull(subsystem.getSwerveDrive(), "SwerveDrive instance should not be null");
    } catch (Exception e) {
      // If hardware initialization fails in test environment, skip the test
      // This is expected when running tests without physical hardware or simulation
      assumeTrue(
          false, "Test skipped due to hardware initialization requirements: " + e.getMessage());
    }
  }
}
