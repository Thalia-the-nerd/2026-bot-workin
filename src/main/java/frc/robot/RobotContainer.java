package frc.robot;

import com.pathplanner.lib.auto.NamedCommands;
import com.pathplanner.lib.commands.PathPlannerAuto;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import frc.robot.commands.AimCommand;
import frc.robot.commands.AutoAimCommand;
import frc.robot.commands.DefaultDrive;
import frc.robot.commands.FireCommand;
import frc.robot.commands.IntakeSliderCommand;
import frc.robot.commands.SetTurretPositionCommand;
import frc.robot.commands.UnjamIntakeCommand;
import frc.robot.subsystems.CameraSubsystem;
import frc.robot.subsystems.DriveSubsystem;
import frc.robot.subsystems.FireControlSubsystem;
import frc.robot.subsystems.IntakeSubsystem;
import frc.robot.subsystems.LoaderSubsystem;
import frc.robot.subsystems.TurretSubsystem;
import org.littletonrobotics.junction.networktables.LoggedDashboardChooser;

/**
 * The RobotContainer class is responsible for instantiating and configuring all robot subsystems,
 * setting up controller bindings, and managing the default and autonomous commands. This class
 * serves as the central hub for organizing the robot's command-based structure.
 */
public class RobotContainer {
  // The Controller (Port 0 is usually the first USB controller plugged in)
  private final CommandXboxController m_controller1 =
      new CommandXboxController(Constants.CONTROLLER_USB_INDEX);
  private final Joystick m_flightstick = new Joystick(Constants.FLIGHTSTICK_USB_INDEX);

  // Initialize subsystems
  // private final SwerveSubsystem swerveSubsystem = new SwerveSubsystem(new
  // File(Filesystem.getDeployDirectory(), "swerve"));
  private final DriveSubsystem m_driveSubsystem = new DriveSubsystem();
  private final CameraSubsystem m_cameraSubsystem = new CameraSubsystem(m_driveSubsystem);

  private final TurretSubsystem m_turretSubsystem = new TurretSubsystem();
  private final FireControlSubsystem m_fireSubsystem = new FireControlSubsystem();
  private final IntakeSubsystem m_intakeSubsystem = new IntakeSubsystem();
  private final LoaderSubsystem m_loaderSubsystem = new LoaderSubsystem();

  // Initialize Commands
  private final DefaultDrive m_defaultDrive =
      new DefaultDrive(
          m_driveSubsystem,
          this::getControllerLeftY,
          this::getControllerRightY,
          () -> m_controller1.getHID().getLeftBumper());
  private final AimCommand m_aimCommand = new AimCommand(m_driveSubsystem, m_cameraSubsystem);

  // Init controller buttons
  private Trigger m_toggleBrakeButton;
  private Trigger m_switchQueuedButton;
  private Trigger m_toggleIntakeButton;
  // Init joystick buttons
  private JoystickButton m_operatorDefaultButton;
  private JoystickButton m_operatorButton2;
  private JoystickButton m_operatorButton6;
  private JoystickButton m_operatorButton7;
  private JoystickButton m_operatorButton8;
  private JoystickButton m_operatorButton9;
  private JoystickButton m_operatorButton10;
  private JoystickButton m_operatorButton11;
  private JoystickButton m_operatorButton12;

  // Init For Autonomous
  private LoggedDashboardChooser<String> autoDashboardChooser =
      new LoggedDashboardChooser<String>("AutoMode");

  public final boolean enableAutoProfiling = false;

  /**
   * Constructs a new RobotContainer.
   *
   * <p>This constructor initializes the robot's subsystems and configures controller bindings by
   * calling {@link #configureBindings()}. This setup ensures that the drivebase subsystem and
   * controller commands are properly initialized before the robot starts operating.
   */
  public RobotContainer() {

    // Initialize the autonomous command
    initializeAutonomous();
    // Setup on the fly path planning
    configureTeleopPaths();
    // Configure the button bindings
    setupTriggers();
    if (enableAutoProfiling) {
      // bindDriveSysIDCommands();
      bindDriveSysIDCommands();
      // bindElevatorSysIDCommands();
    } else {
      bindCommands();
    }
  }

  private void setupTriggers() {
    // Controller Buttons
    m_toggleBrakeButton = m_controller1.rightBumper();
    m_switchQueuedButton = m_controller1.y();
    m_toggleIntakeButton = m_controller1.a();

    // Joystick Buttons
    m_operatorDefaultButton =
        new JoystickButton(m_flightstick, Constants.JOYSTICK_DEFAULT_BUTTON); //
    m_operatorButton2 = new JoystickButton(m_flightstick, 2);
    m_operatorButton6 = new JoystickButton(m_flightstick, 6);
    m_operatorButton7 = new JoystickButton(m_flightstick, 7);
    m_operatorButton8 = new JoystickButton(m_flightstick, 8);
    m_operatorButton9 = new JoystickButton(m_flightstick, 9);
    m_operatorButton10 = new JoystickButton(m_flightstick, 10);
    m_operatorButton11 = new JoystickButton(m_flightstick, 11);
    m_operatorButton12 = new JoystickButton(m_flightstick, 12);
  }

  private void bindCommands() {
    // Controller Bindings
    m_toggleBrakeButton.onTrue(new InstantCommand(() -> m_driveSubsystem.SwitchBrakemode()));

    // Intake
    m_toggleIntakeButton.toggleOnTrue(
        new RunCommand(() -> m_intakeSubsystem.setIntakeSpeed(1.0), m_intakeSubsystem));
    m_controller1
        .leftTrigger()
        .whileTrue(new RunCommand(() -> m_intakeSubsystem.setIntakeSpeed(-1.0), m_intakeSubsystem));

    // Fire Override
    m_controller1
        .rightTrigger()
        .whileTrue(new RunCommand(() -> m_fireSubsystem.fire(1.0), m_fireSubsystem));

    // Default Drive
    m_driveSubsystem.setDefaultCommand(m_defaultDrive);
    // Joystick Bindings
    // (Removed queued shooter mode override)

    // Turret Default Command (Bind to X-axis of flight stick)
    m_turretSubsystem.setDefaultCommand(
        new RunCommand(
            () -> m_turretSubsystem.setTurretSpeed(m_flightstick.getX()), m_turretSubsystem));

    // Loader Default Command (Bind to Y-axis of flight stick)
    m_loaderSubsystem.setDefaultCommand(
        new RunCommand(
            () -> m_loaderSubsystem.setLoaderSpeed(m_flightstick.getY()), m_loaderSubsystem));

    // Fire Control Command (Bind to Trigger / Button 1 of flight stick)
    // Run at full speed (1.0) while trigger is held, rather than mapped to Y axis.
    m_operatorDefaultButton.whileTrue(
        new FireCommand(m_fireSubsystem, () -> 1.0, m_operatorDefaultButton));

    // Auto Aim Command (Bind to Button 2 of flight stick to toggle)
    m_operatorButton2.toggleOnTrue(new AutoAimCommand(m_turretSubsystem, m_cameraSubsystem));

    // Turret Preset Orientations (Buttons 6 - 11)
    // Values are placeholders for raw motor rotations until gear ratio is determined.
    m_operatorButton6.onTrue(new SetTurretPositionCommand(m_turretSubsystem, -0.5));
    m_operatorButton7.onTrue(new SetTurretPositionCommand(m_turretSubsystem, -0.25));
    m_operatorButton8.onTrue(new SetTurretPositionCommand(m_turretSubsystem, 0.0));
    m_operatorButton9.onTrue(new SetTurretPositionCommand(m_turretSubsystem, 0.25));
    m_operatorButton10.onTrue(new SetTurretPositionCommand(m_turretSubsystem, 0.5));
    m_operatorButton11.onTrue(new SetTurretPositionCommand(m_turretSubsystem, 0.75));

    // Intake System
    // Bind fuzzy slider (Flightstick Throttle axis) to automatically control the Intake.
    m_intakeSubsystem.setDefaultCommand(
        new IntakeSliderCommand(m_intakeSubsystem, () -> m_flightstick.getThrottle()));

    // Emergency Unjam (Button 12)
    m_operatorButton12.onTrue(new UnjamIntakeCommand(m_intakeSubsystem));

    // TODO: Make Swerve code follow proper command-based structure
    /*
    drivebase.setDefaultCommand(
        // We create a "RunCommand" (runs repeatedly)
        Commands.run(
            () -> {
                // 1. Get Joystick Inputs (Inverted because Y is up-negative in computer graphics)
                // MathUtil.applyDeadband ignores tiny drift when the stick is centered
                double yVelocity = -MathUtil.applyDeadband(driverXbox.getLeftY(), 0.1);
                double xVelocity = -MathUtil.applyDeadband(driverXbox.getLeftX(), 0.1);
                double rotation  = -MathUtil.applyDeadband(driverXbox.getRightX(), 0.1);

                // 2. Drive
                drivebase.drive(
                    new Translation2d(yVelocity * drivebase.maximumSpeed, xVelocity * drivebase.maximumSpeed),
                    rotation * Math.PI,
                    true // Field Relative (True = Standard, False = Robot Oriented)
                );
            },
            drivebase // REQUIRE the subsystem so no other command can interrupt this one
        )
    );

    // Map "Back" button to zero the gyro (reset field orientation)
    if (drivebase.getSwerveDrive() != null) {
      driverXbox.back().onTrue(Commands.runOnce(drivebase.getSwerveDrive()::zeroGyro, drivebase));
    }
      */
  }

  private void initializeAutonomous() {
    // Network Table Routine Options
    autoDashboardChooser.addOption("DriveStraight", "DriveStraight");
    autoDashboardChooser.addOption("Do Nothing", "DoNothing");
    SmartDashboard.putData(autoDashboardChooser.getSendableChooser());

    // Named Commands
    // ex:
    // NamedCommands.registerCommand("A", new PathFollowingCommand(m_driveSubsystem,
    // pathGroup.get(0)));
    NamedCommands.registerCommand(
        "BrakeCommand", new InstantCommand(() -> m_driveSubsystem.SetBrakemode()));
    NamedCommands.registerCommand("AimCommand", m_aimCommand);
  }

  private void bindDriveSysIDCommands() {
    m_controller1.a().whileTrue(m_driveSubsystem.sysIdQuasistatic(SysIdRoutine.Direction.kForward));
    m_controller1.b().whileTrue(m_driveSubsystem.sysIdQuasistatic(SysIdRoutine.Direction.kReverse));
    m_controller1.x().whileTrue(m_driveSubsystem.sysIdDynamic(SysIdRoutine.Direction.kForward));
    m_controller1.y().whileTrue(m_driveSubsystem.sysIdDynamic(SysIdRoutine.Direction.kReverse));
    m_controller1.leftTrigger().whileTrue(new InstantCommand(() -> DataLogManager.stop()));
  }

  private void configureTeleopPaths() {
    // TODO: Write new paths
    // EX
    // PathPlannerPath ampPath = PathPlannerPath.fromPathFile("TeleopAmpPath");

    // m_driveToAmp = AutoBuilder.pathfindThenFollowPath(ampPath, constraints);
  }

  public double getControllerRightY() {
    return -m_controller1.getRightY();
  }

  public double getControllerLeftY() {
    return -m_controller1.getLeftY();
  }

  public double GetFlightStickY() {
    return m_flightstick.getY();
  }

  // for autonomous
  public DefaultDrive getM_defaultDrive() {
    return m_defaultDrive;
  }

  // for future SmartDashboard uses.
  public CommandXboxController getM_controller1() {
    return this.m_controller1;
  }

  // for smart dashboard.
  public Joystick getFlightStick() {
    return this.m_flightstick;
  }

  /**
   * Returns the command to run during the autonomous period.
   *
   * @return the autonomous command to execute
   */
  public Command getAutonomousCommand() {
    String autoName = autoDashboardChooser.get();
    return new PathPlannerAuto(autoName);
  }

  public void periodic() {
    // This method will be called once per scheduler run (Only for inter subsystem state updating)
  }
}
