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
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.simulation.SingleJointedArmSim;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.RobotTelemetry;
import frc.robot.constants.Constants;
import frc.robot.constants.Constants.CANConstants;
import frc.robot.constants.SpeedConstants;

public class TurretSubsystem extends SubsystemBase {
  private final SparkMax m_turretMotor;
  private final SparkMaxConfig m_config;
  private final SparkClosedLoopController m_pidController;
  private final RelativeEncoder m_encoder;
  private final SlewRateLimiter m_speedLimiter;

  private boolean m_isUnwinding = false;

  // PID Constants (Need tuning)
  private final double kP = 0.1;
  private final double kI = 0.0;
  private final double kD = 0.0;

  // Sim Objects
  private final SparkMaxSim m_turretSim;
  private final SparkRelativeEncoderSim m_encoderSim;
  private final SingleJointedArmSim m_physicsSim;

  @SuppressWarnings("removal")
  public TurretSubsystem() {
    m_turretMotor = new SparkMax(CANConstants.MOTOR_TURRET_ID, MotorType.kBrushless);
    m_config = new SparkMaxConfig();

    // Electrical Safety Limit (Prevents the motor from pulling too many amps and burning out)
    m_config.smartCurrentLimit(40);

    // Hardware-level Torque Smoothing (time in seconds from 0 to full speed)
    m_config.openLoopRampRate(0.25);
    m_config.closedLoopRampRate(0.25);

    // Setup PID
    m_config.closedLoop.pid(kP, kI, kD);
    m_config.closedLoop.outputRange(-0.25, 0.25); // Limit output speed to prevent overshooting

    m_turretMotor.configure(
        m_config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

    // Software Slew Rate Limiter for manual inputs (acceleration cap: full speed in 0.5s)
    m_speedLimiter = new SlewRateLimiter(2.0);

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
    if (m_isUnwinding) return;
    // Add simple range just in case controller has drift
    if (Math.abs(speed) < 0.1) {
      speed = 0;
    }
    double adjustedSpeed =
        m_speedLimiter.calculate(
            SpeedConstants.adjustSpeed(
                speed, SpeedConstants.TURRET_MAX_SPEED, SpeedConstants.TURRET_SENSITIVITY));
    m_pidController.setReference(
        adjustedSpeed,
        SparkMax.ControlType.kDutyCycle,
        com.revrobotics.spark.ClosedLoopSlot.kSlot0);
  }

  /**
   * Directly sets the voltage of the turret motor, useful for ProfiledPID + Feedforward outputs.
   *
   * @param volts Output voltage.
   */
  public void setTurretVoltage(double volts) {
    if (m_isUnwinding) return;
    m_turretMotor.setVoltage(volts);
  }

  /** Gets the current robot-relative position of the turret in radians. */
  public double getTurretAngleRadians() {
    double currentRotations = m_encoder.getPosition();
    return (currentRotations / Constants.TURRET_GEAR_RATIO) * 2.0 * Math.PI;
  }

  /** Gets the current robot-relative position of the turret in degrees. */
  public double getTurretAngleDegrees() {
    double currentRotations = m_encoder.getPosition();
    return (currentRotations / Constants.TURRET_GEAR_RATIO) * 360.0;
  }

  /**
   * Sets the target angle of the turret using closed-loop control.
   *
   * @param targetAngleDegrees Target angle in degrees.
   */
  public void setTargetAngle(double targetAngleDegrees) {
    if (m_isUnwinding) return;
    double targetRotations = (targetAngleDegrees / 360.0) * Constants.TURRET_GEAR_RATIO;
    m_pidController.setReference(
        targetRotations,
        SparkMax.ControlType.kPosition,
        com.revrobotics.spark.ClosedLoopSlot.kSlot0);
  }

  /**
   * Checks if the turret is at the specified target angle.
   *
   * @param targetAngleDegrees Target angle in degrees.
   * @param toleranceDegrees Tolerance in degrees.
   * @return True if within tolerance, false otherwise.
   */
  public boolean isAtAngle(double targetAngleDegrees, double toleranceDegrees) {
    return Math.abs(getTurretAngleDegrees() - targetAngleDegrees) <= toleranceDegrees;
  }

  /** Stops the turret motor. */
  public void stop() {
    if (m_isUnwinding) return;
    m_turretMotor.set(0);
    m_speedLimiter.reset(0); // Reset limiter so next move doesn't jump
  }

  /** Returns whether the turret is currently auto-unwinding. */
  public boolean isUnwinding() {
    return m_isUnwinding;
  }

  @Override
  public void periodic() {
    double currentAngle = getTurretAngleDegrees();

    // Check if we exceeded bounds and enter unwinding state
    if (Math.abs(currentAngle) >= 360.0 && !m_isUnwinding) {
      m_isUnwinding = true;
    }

    // Handle unwinding logic
    if (m_isUnwinding) {
      // Force PID to target 0 rotations (0 degrees)
      m_pidController.setReference(
          0.0, SparkMax.ControlType.kPosition, com.revrobotics.spark.ClosedLoopSlot.kSlot0);

      // Check if we're back near 0 center
      // Stiction and SparkMax deadband with an undertuned PID (kP=0.1) can cause
      // the motor to stall ~18 degrees away from 0.0, so we use a wider 25.0 deg tolerance.
      if (Math.abs(currentAngle) <= 25.0) {
        m_isUnwinding = false;
        // Reset our rate limiter so the driver can cleanly regain control
        m_speedLimiter.reset(0);
      }
    }

    // Output current state of turret motor for debugging
    RobotTelemetry.putNumber("Turret Motor Speed Output", m_turretMotor.get());
    RobotTelemetry.putNumber("Turret Position", m_encoder.getPosition());
    RobotTelemetry.putBoolean("Turret Is Unwinding", m_isUnwinding);
  }

  @Override
  public void simulationPeriodic() {
    // Set simulator inputs
    m_physicsSim.setInput(m_turretSim.getAppliedOutput() * RobotController.getBatteryVoltage());
    m_physicsSim.update(0.02);

    // Update Spark Max simulated sensors
    m_encoderSim.setPosition(m_physicsSim.getAngleRads() / (2 * Math.PI) * 10.0); // Gear ratio 10

    // Broadcast for Python App
    RobotTelemetry.putNumber("Sim_TurretAngle", Math.toDegrees(m_physicsSim.getAngleRads()));
  }
}
