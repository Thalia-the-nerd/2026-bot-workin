package frc.robot.subsystems;

import com.revrobotics.RelativeEncoder;
import com.revrobotics.sim.SparkMaxSim;
import com.revrobotics.sim.SparkRelativeEncoderSim;
import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.simulation.SingleJointedArmSim;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.CANConstants;
import frc.robot.SpeedConstants;

public class TurretSubsystem extends SubsystemBase {
  private final SparkMax m_turretMotor;
  private final SparkMaxConfig m_config;
  private final SparkClosedLoopController m_pidController;
  private final RelativeEncoder m_encoder;

  // PID Constants (Need tuning)
  private final double kP = 0.1;
  private final double kI = 0.0;
  private final double kD = 0.0;

  // Sim Objects
  private final SparkMaxSim m_turretSim;
  private final SparkRelativeEncoderSim m_encoderSim;
  private final SingleJointedArmSim m_physicsSim;

  public TurretSubsystem() {
    m_turretMotor = new SparkMax(CANConstants.MOTOR_TURRET_ID, MotorType.kBrushless);
    m_config = new SparkMaxConfig();

    // Electrical Safety Limit (Prevents the motor from pulling too many amps and burning out)
    m_config.smartCurrentLimit(40);
    // Setup PID
    m_config.closedLoop.pid(kP, kI, kD);
    m_config.closedLoop.outputRange(-0.5, 0.5); // Limit output speed for safety during testing

    m_turretMotor.configure(
        m_config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

    m_pidController = m_turretMotor.getClosedLoopController();
    m_encoder = m_turretMotor.getEncoder();

    // Configure Simulation
    m_turretSim = new SparkMaxSim(m_turretMotor, DCMotor.getNEO(1));
    m_encoderSim = new SparkRelativeEncoderSim(m_turretMotor);
    // Physics sim: gravity disabled for horizontal turret. Range: -360 to 360 degrees
    m_physicsSim =
        new SingleJointedArmSim(
            DCMotor.getNEO(1),
            10.0, // Gear Ratio
            0.5, // Moment of Inertia
            1.0, // Mass
            -Math.PI * 2,
            Math.PI * 2,
            false,
            0.0);
  }

  /**
   * Sets the speed of the turret motor.
   *
   * @param speed The target speed (-1 to 1) (bool).
   */
  public void setTurretSpeed(double speed) {
    // Add simple range just in case controller has drift
    if (Math.abs(speed) < 0.1) {
      speed = 0;
    }
    double adjustedSpeed =
        SpeedConstants.adjustSpeed(
            speed, SpeedConstants.TURRET_MAX_SPEED, SpeedConstants.TURRET_SENSITIVITY);
    m_pidController.setReference(adjustedSpeed, SparkMax.ControlType.kDutyCycle);
  }

  /**
   * Sets the target position of the turret motor using closed-loop control.
   *
   * @param targetRotations Target position in motor rotations.
   */
  public void setTargetPosition(double targetRotations) {
    m_pidController.setReference(targetRotations, SparkMax.ControlType.kPosition);
  }

  /**
   * Checks if the turret is at the specified target position.
   *
   * @param targetRotations Target position in motor rotations.
   * @param tolerance Tolerance in motor rotations.
   * @return True if within tolerance, false otherwise.
   */
  public boolean isAtPosition(double targetRotations, double tolerance) {
    double currentPos = m_encoder.getPosition();
    return Math.abs(currentPos - targetRotations) <= tolerance;
  }

  /** Stops the turret motor. */
  public void stop() {
    m_turretMotor.set(0);
  }

  @Override
  public void periodic() {
    // Output current state of turret motor for debugging
    SmartDashboard.putNumber("Turret Motor Speed Output", m_turretMotor.get());
    SmartDashboard.putNumber("Turret Position", m_encoder.getPosition());
  }

  @Override
  public void simulationPeriodic() {
    // Set simulator inputs
    m_physicsSim.setInput(m_turretSim.getAppliedOutput() * RobotController.getBatteryVoltage());
    m_physicsSim.update(0.02);

    // Update Spark Max simulated sensors
    m_encoderSim.setPosition(m_physicsSim.getAngleRads() / (2 * Math.PI) * 10.0); // Gear ratio 10

    // Broadcast for Python App
    SmartDashboard.putNumber("Sim_TurretAngle", Math.toDegrees(m_physicsSim.getAngleRads()));
  }
}
