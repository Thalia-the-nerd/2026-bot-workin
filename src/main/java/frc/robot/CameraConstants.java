package frc.robot;

import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.util.Units;

public class CameraConstants {
  public static class CameraState {
    public final String NAME;
    public final Transform3d LOCATION;

    /**
     * @param Name Name of the camera
     * @param X_Location X Location in meters
     * @param Y_Location Y Location in meters
     * @param Z_Location Z Location in meters
     * @param Roll Roll in radians
     * @param Pitch Pitch in radians
     * @param Yaw Yaw in radians
     */
    public CameraState(
        String name,
        double X_Location,
        double Y_Location,
        double Z_Location,
        double Roll,
        double Pitch,
        double Yaw) {
      NAME = name;
      LOCATION =
          new Transform3d(
              new Translation3d(X_Location, Y_Location, Z_Location),
              new Rotation3d(Roll, Pitch, Yaw));
    }
  }

  public static final CameraState POSE_CAMERA1 =
      new CameraState(
          "Pose1",
          Units.inchesToMeters(-1), // X location: in front of center
          Units.inchesToMeters(-1), // Y location: left of center
          Units.inchesToMeters(-1), // Z: Location up from center
          Units.degreesToRadians(0.0), // Roll
          Units.degreesToRadians(0.0), // Pitch
          Units.degreesToRadians(0.0)); // Yaw

  public static final CameraState POSE_CAMERA2 =
      new CameraState(
          "Pose2",
          Units.inchesToMeters(-1), // X Location; front/back
          Units.inchesToMeters(-1), // Y Location; left/right
          Units.inchesToMeters(-1), // Z Location; up/down
          Units.degreesToRadians(0), // Roll
          Units.degreesToRadians(0.0), // Pitch
          Units.degreesToRadians(0.0)); // Yaw

  public static final CameraState TARGETING_CAMERA1 =
      new CameraState(
          "Targeting1",
          Units.inchesToMeters(-1), // X Locatio: front/back
          Units.inchesToMeters(-1), // Y Location: left/right
          Units.inchesToMeters(12.5), // Z Location: up/down
          Units.degreesToRadians(0),
          Units.degreesToRadians(0.0),
          Units.degreesToRadians(0));
}
