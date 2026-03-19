package frc.robot.utils;

import edu.wpi.first.math.interpolation.InterpolatingDoubleTreeMap;
import frc.robot.AutoAimConstants;

/**
 * Utility class to evaluate the auto aim regression model and calculate distance from AprilTags.
 */
public class AutoAimRegression {
  private static final InterpolatingDoubleTreeMap angleMap = new InterpolatingDoubleTreeMap();
  private static final InterpolatingDoubleTreeMap speedMap = new InterpolatingDoubleTreeMap();

  static {
    if (AutoAimConstants.RAW_DATA != null) {
      for (double[] point : AutoAimConstants.RAW_DATA) {
        if (point.length >= 3) {
          angleMap.put(point[0], point[1]);
          speedMap.put(point[0], point[2]);
        }
      }
    }
  }

  /**
   * Calculates the horizontal distance to an AprilTag target.
   *
   * @param targetHeight Height of the target off the ground
   * @param cameraHeight Height of the camera off the ground
   * @param cameraPitch Pitch angle of the camera
   * @param targetPitch Pitch angle from the camera to the target
   * @return Horizontal distance to the target
   */
  public static double calculateDistance(
      double targetHeight, double cameraHeight, double cameraPitch, double targetPitch) {
    return (targetHeight - cameraHeight) / Math.tan(cameraPitch + targetPitch);
  }

  /**
   * Predicts the required shooter angle based on distance. Evaluates the
   * InterpolatingDoubleTreeMap.
   *
   * @param distance Computed distance to the target
   * @return Recommended shooter angle (degrees/radians depending on input data)
   */
  public static double predictAngle(double distance) {
    if (AutoAimConstants.RAW_DATA == null || AutoAimConstants.RAW_DATA.length == 0) return 0.0;
    return angleMap.get(distance);
  }

  /**
   * Predicts the required shooter speed based on distance. Evaluates the
   * InterpolatingDoubleTreeMap.
   *
   * @param distance Computed distance to the target
   * @return Recommended shooter speed (RPM)
   */
  public static double predictSpeed(double distance) {
    if (AutoAimConstants.RAW_DATA == null || AutoAimConstants.RAW_DATA.length == 0) return 0.0;
    return speedMap.get(distance);
  }
}
