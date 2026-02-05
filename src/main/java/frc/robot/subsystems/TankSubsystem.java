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

    // FUCK inversion
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
  private final edu.wpi.first.math.numbers.N2 m_chars = edu.wpi.first.math.VecBuilder.fill(1.5, 3); // linear, angular
  private final edu.wpi.first.wpilibj.simulation.DifferentialDrivetrainSim m_driveSim = new edu.wpi.first.wpilibj.simulation.DifferentialDrivetrainSim(
      edu.wpi.first.math.system.plant.DCMotor.getNEO(2),       // 2 NEOs per side
      7.29,                                                    // 7.29:1 gearing
      7.5,                                                     // 7.5 kg mass (approx)
      edu.wpi.first.math.util.Units.inchesToMeters(2),         // 2" radius wheels (approx)
      edu.wpi.first.math.util.Units.inchesToMeters(20),        // track width
      m_chars
  );

  @Override
  public void periodic() {
  }

  @Override
  public void simulationPeriodic() {
    // FUCK physics
    m_driveSim.setInputs(leftFront.getAppliedOutput() * 12, rightFront.getAppliedOutput() * 12);
    m_driveSim.update(0.02);

    m_field.setRobotPose(m_driveSim.getPose());
  }
}
