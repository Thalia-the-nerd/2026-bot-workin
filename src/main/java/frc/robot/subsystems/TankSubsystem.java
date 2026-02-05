package frc.robot.subsystems;
//https://www.youtube.com/watch?v=RLLJRB7Kglo BE ALL THAT YOU CAN BE

import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkBase.PersistMode;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class TankSubsystem extends SubsystemBase {

  private final Field2d m_field = new Field2d();

  private final SparkMax leftFront = new SparkMax(1, MotorType.kBrushless);
  private final SparkMax leftRear = new SparkMax(2, MotorType.kBrushless);
  private final SparkMax rightFront = new SparkMax(3, MotorType.kBrushless);
  private final SparkMax rightRear = new SparkMax(4, MotorType.kBrushless);

  private final DifferentialDrive drive;

  public TankSubsystem() {
    SparkMaxConfig followerConfig = new SparkMaxConfig();
    
    // Configure followers
    followerConfig.follow(leftFront);
    leftRear.configure(followerConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

    followerConfig.follow(rightFront);
    rightRear.configure(followerConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

    //inversion
    SparkMaxConfig rightLeaderConfig = new SparkMaxConfig();
    rightLeaderConfig.inverted(true);
    rightFront.configure(rightLeaderConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

    drive = new DifferentialDrive(leftFront, rightFront);
    SmartDashboard.putData("Field", m_field);
  }

  public void drive(double speed, double rotation) {
    drive.arcadeDrive(speed, rotation);
  }

  // Simulation stuff
  private final edu.wpi.first.wpilibj.simulation.DifferentialDrivetrainSim m_driveSim = new edu.wpi.first.wpilibj.simulation.DifferentialDrivetrainSim(
      edu.wpi.first.math.system.plant.DCMotor.getNEO(2),       // 2 NEOs per side
      7.29,                                                    // 7.29:1 gearing
      7.5,                                                     // 7.5 kg mass related (J)
      60.0,                                                    // Mass
      edu.wpi.first.math.util.Units.inchesToMeters(2),         // 2" radius wheels
      edu.wpi.first.math.util.Units.inchesToMeters(20),        // track width
      null                                                     // Measurement noise
  );

  @Override
  public void periodic() {
  }
//I think i like index too much the videos are just so cool and the fighter jets are so epic
  @Override
  public void simulationPeriodic() {
    // physics moment
    m_driveSim.setInputs(leftFront.get() * 12, rightFront.get() * 12);
    m_driveSim.update(0.02);

    m_field.setRobotPose(m_driveSim.getPose());
  }
}
