// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.DeferredCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.RobotTelemetry;
import frc.robot.constants.CameraConstants;
import frc.robot.subsystems.CameraSubsystem;
import frc.robot.subsystems.DriveSubsystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.photonvision.targeting.PhotonPipelineResult;
import org.photonvision.targeting.PhotonTrackedTarget;

/** The Aim command that uses the camera to generate a path to the target. */
public class AimCommand extends SequentialCommandGroup {

  // Generated using Vernier Graphical Analysis
  private static final double distancePowerA = 9.847;
  private static final double distancePowerB = -0.6214;

  /**
   * Creates a new AimCommand.
   *
   * @param d_subsystem The drive subsystem to use.
   * @param c_subsystem The camera subsystem to use.
   */
  public AimCommand(DriveSubsystem d_subsystem, CameraSubsystem c_subsystem) {
    // Add requirements for the entire sequence (it will occupy the drivetrain)
    addRequirements(d_subsystem, c_subsystem);

    Transform3d camOffset = CameraConstants.TARGETING_CAMERA1.LOCATION;
    // this offset takes the center of robot and tells it to move back so that we dont just run over
    // the target
    Transform3d targetingOffset = camOffset.plus(new Transform3d());

    addCommands(
        // Step 1: Wait until we see a target
        Commands.waitUntil(
            () -> {
              Optional<PhotonPipelineResult> result = c_subsystem.targetingCamera1Result;
              if (result.isPresent() && result.get().hasTargets()) {
                return true;
              }
              SmartDashboard.putBoolean("CameraTargetDetected", false);
              SmartDashboard.putNumber("CameraTargetPitch", 0.0);
              return false;
            }),
        // Step 2: Once the target is seen, defer the PathPlanner command generation until this step
        // runs
        new DeferredCommand(
            () -> {
              Optional<PhotonPipelineResult> resultOpt = c_subsystem.targetingCamera1Result;
              if (resultOpt.isEmpty() || !resultOpt.get().hasTargets()) {
                return Commands.none();
              }

              PhotonPipelineResult result = resultOpt.get();
              SmartDashboard.putBoolean("CameraTargetDetected", true);
              PhotonTrackedTarget target = result.getBestTarget();

              double detectedArea = target.area;
              double distance = distancePowerA * Math.pow(detectedArea, distancePowerB);
              RobotTelemetry.recordOutput("BallDistance", distance);

              double yaw = Units.degreesToRadians(target.getYaw());
              double distance_x = distance * Math.cos(yaw);
              double distance_y = distance * Math.sin(yaw);
              Transform3d cameraToTarget =
                  new Transform3d(
                      new Translation3d(distance_x, distance_y, 0), new Rotation3d(0, 0, yaw));

              RobotTelemetry.recordOutput("AimCamToTargetTransform", cameraToTarget);

              Transform3d targetOffset = cameraToTarget.plus(targetingOffset);

              RobotTelemetry.recordOutput("AimTargetRelRobotPose", targetOffset);

              Pose3d robotPose = new Pose3d(d_subsystem.getPose());
              Pose3d robotToTarget = robotPose.plus(targetOffset);
              RobotTelemetry.recordOutput("AimNavRelPose", robotToTarget);

              Pose2d newTargetPose = robotToTarget.toPose2d();
              RobotTelemetry.recordOutput("AimNav2dPose", newTargetPose);

              Rotation2d newRotation = new Rotation2d(newTargetPose.getRotation().getDegrees());

              List<Pose2d> targetPoses = new ArrayList<>();
              targetPoses.add(
                  new Pose2d(
                      robotPose.getTranslation().getX(),
                      robotPose.getTranslation().getY(),
                      newRotation));
              targetPoses.add(
                  new Pose2d(
                      newTargetPose.getTranslation().getX(),
                      newTargetPose.getTranslation().getY(),
                      newRotation));

              d_subsystem.setReducedSpeed(false);
              return d_subsystem.GenerateOnTheFlyCommand(targetPoses);
            },
            Set.of(d_subsystem)));
  }
}
