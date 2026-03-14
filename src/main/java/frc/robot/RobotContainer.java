package frc.robot;

import com.pathplanner.lib.auto.NamedCommands;
import com.pathplanner.lib.commands.PathPlannerAuto;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.button.CommandJoystick;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import frc.robot.commands.AimCommand;
import frc.robot.commands.AutoAimCommand;
import frc.robot.commands.DefaultDrive;
import frc.robot.commands.FireCommand;
import frc.robot.commands.IntakeSliderCommand;
import frc.robot.commands.SetTurretPositionCommand;
import frc.robot.commands.UnjamIntakeCommand;
import frc.robot.constants.Constants;
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
  private final CommandJoystick m_flightstick =
      new CommandJoystick(Constants.FLIGHTSTICK_USB_INDEX);

  // Initialize subsystems
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

    if (enableAutoProfiling) {
      // bindDriveSysIDCommands();
      bindDriveSysIDCommands();
      // bindElevatorSysIDCommands();
    } else {
      bindCommands();
    }
  }

  private void bindCommands() {
    // Controller Bindings
    m_controller1
        .rightBumper()
        .onTrue(new InstantCommand(() -> m_driveSubsystem.SwitchBrakemode()));

    // Intake
    m_controller1
        .a()
        .toggleOnTrue(
            new RunCommand(() -> m_intakeSubsystem.setIntakeSpeed(1.0), m_intakeSubsystem));
    m_controller1
        .leftTrigger()
        .whileTrue(new RunCommand(() -> m_intakeSubsystem.setIntakeSpeed(-1.0), m_intakeSubsystem));

    // Fire Override
    m_controller1
        .rightTrigger()
        .and(() -> !m_turretSubsystem.isUnwinding())
        .whileTrue(
            new edu.wpi.first.wpilibj2.command.StartEndCommand(
                () -> m_fireSubsystem.setShooterRPM(5000.0),
                () -> m_fireSubsystem.stop(),
                m_fireSubsystem));

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
    m_flightstick
        .button(Constants.JOYSTICK_DEFAULT_BUTTON)
        .and(() -> !m_turretSubsystem.isUnwinding())
        .whileTrue(
            new FireCommand(
                m_fireSubsystem,
                m_loaderSubsystem,
                () -> 1.0,
                m_flightstick.button(Constants.JOYSTICK_DEFAULT_BUTTON)));

    // Auto Aim Command (Bind to Button 2 of flight stick to toggle)
    m_flightstick
        .button(2)
        .toggleOnTrue(new AutoAimCommand(m_turretSubsystem, m_cameraSubsystem, m_driveSubsystem));

    // Turret Preset Orientations (Buttons 6 - 11)
    // Values are placeholders for raw motor rotations until gear ratio is determined.
    m_flightstick.button(6).onTrue(new SetTurretPositionCommand(m_turretSubsystem, -90.0));
    m_flightstick.button(7).onTrue(new SetTurretPositionCommand(m_turretSubsystem, -45.0));
    m_flightstick.button(8).onTrue(new SetTurretPositionCommand(m_turretSubsystem, 0.0));
    m_flightstick.button(9).onTrue(new SetTurretPositionCommand(m_turretSubsystem, 45.0));
    m_flightstick.button(10).onTrue(new SetTurretPositionCommand(m_turretSubsystem, 90.0));
    m_flightstick.button(11).onTrue(new SetTurretPositionCommand(m_turretSubsystem, 180.0));

    // Intake System
    // Bind fuzzy slider (Flightstick Throttle axis) to automatically control the Intake.
    m_intakeSubsystem.setDefaultCommand(
        new IntakeSliderCommand(m_intakeSubsystem, () -> m_flightstick.getThrottle()));

    // Emergency Unjam (Button 12)
    m_flightstick.button(12).onTrue(new UnjamIntakeCommand(m_intakeSubsystem));
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
  public CommandJoystick getFlightStick() {
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
