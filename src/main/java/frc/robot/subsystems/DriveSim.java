package frc.robot.subsystems;

import com.revrobotics.sim.SparkMaxSim;
import com.revrobotics.sim.SparkRelativeEncoderSim;
import com.revrobotics.spark.SparkMax;
import edu.wpi.first.hal.SimDouble;
import edu.wpi.first.hal.simulation.SimDeviceDataJNI;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.simulation.BatterySim;
import edu.wpi.first.wpilibj.simulation.DifferentialDrivetrainSim;
import edu.wpi.first.wpilibj.simulation.RoboRioSim;
import frc.robot.DriveConstants;

public class DriveSim {
  private final SimDouble SimGyroAngleHandler;
  private final DCMotor m_leftGearbox;
  private final DCMotor m_rightGearbox;
  private final SparkMaxSim m_leftSim;
  private final SparkMaxSim m_rightSim;
  private final DifferentialDrivetrainSim m_driveTrainSim;
  private final SparkRelativeEncoderSim m_leftEncoderSim;
  private final SparkRelativeEncoderSim m_rightEncoderSim;

  public DriveSim(SparkMax leftMotor, SparkMax rightMotor) {
    m_leftGearbox = DCMotor.getNEO(2);
    m_rightGearbox = DCMotor.getNEO(2);
    m_leftSim = new SparkMaxSim(leftMotor, m_leftGearbox);
    m_rightSim = new SparkMaxSim(rightMotor, m_rightGearbox);

    int gyroID = SimDeviceDataJNI.getSimDeviceHandle("navX-Sensor[4]");
    SimGyroAngleHandler = new SimDouble(SimDeviceDataJNI.getSimValueHandle(gyroID, "Yaw"));

    m_driveTrainSim =
        new DifferentialDrivetrainSim(
            LinearSystemId.identifyDrivetrainSystem(
                DriveConstants.kvDriveVoltSecondsPerMeter,
                DriveConstants.kaDriveVoltSecondsSquaredPerMeter,
                DriveConstants.kvDriveVoltSecondsPerMeterAngular,
                DriveConstants.kaDriveVoltSecondsSquaredPerMeterAngular),
            DCMotor.getNEO(2),
            DriveConstants.GEAR_RATIO,
            DriveConstants.kTrackwidthMeters,
            DriveConstants.WHEEL_RADIUS,
            VecBuilder.fill(0.001, 0.001, 0.001, 0.1, 0.1, 0.005, 0.005));

    m_leftEncoderSim = new SparkRelativeEncoderSim(leftMotor);
    m_rightEncoderSim = new SparkRelativeEncoderSim(rightMotor);
  }

  public void update() {
    m_driveTrainSim.setInputs(
        m_leftSim.getAppliedOutput() * RobotController.getInputVoltage(),
        -m_rightSim.getAppliedOutput() * RobotController.getInputVoltage());
    m_driveTrainSim.update(0.02);

    m_leftSim.iterate(
        m_driveTrainSim.getLeftVelocityMetersPerSecond(), RoboRioSim.getVInVoltage(), 0.02);
    m_rightSim.iterate(
        m_driveTrainSim.getRightVelocityMetersPerSecond(), RoboRioSim.getVInVoltage(), 0.02);

    RoboRioSim.setVInVoltage(
        BatterySim.calculateDefaultBatteryLoadedVoltage(m_driveTrainSim.getCurrentDrawAmps()));

    // In Simulation, navX gets updated negatively
    SimGyroAngleHandler.set(-m_driveTrainSim.getHeading().getDegrees());

    m_leftEncoderSim.setPosition(m_driveTrainSim.getLeftPositionMeters());
    m_leftEncoderSim.setVelocity(m_driveTrainSim.getLeftVelocityMetersPerSecond());
    m_rightEncoderSim.setPosition(m_driveTrainSim.getRightPositionMeters());
    m_rightEncoderSim.setVelocity(m_driveTrainSim.getRightVelocityMetersPerSecond());
  }
}
