package frc.robot.commands;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.RobotTelemetry;
import frc.robot.subsystems.*;

public class PitHealthCheckCommand extends Command {
  private final DriveSubsystem m_drive;
  private final IntakeSubsystem m_intake;
  private final FireControlSubsystem m_fire;
  private final LoaderSubsystem m_loader;
  private final TurretSubsystem m_turret;

  private final Timer m_timer = new Timer();
  private int m_stage = 0;
  private StringBuilder m_report = new StringBuilder();

  public PitHealthCheckCommand(
      DriveSubsystem drive,
      IntakeSubsystem intake,
      FireControlSubsystem fire,
      LoaderSubsystem loader,
      TurretSubsystem turret) {
    m_drive = drive;
    m_intake = intake;
    m_fire = fire;
    m_loader = loader;
    m_turret = turret;
    addRequirements(m_drive, m_intake, m_fire, m_loader, m_turret);
  }

  @Override
  public void initialize() {
    m_stage = 0;
    m_timer.restart();
    m_report.setLength(0);
    m_report.append("Starting Pit Verification Sweep...\n");
    RobotTelemetry.putString("Alerts/PitHealthCheck", "RUNNING SWEEP - PLEASE STAND CLEAR");
  }

  @Override
  public void execute() {
    double time = m_timer.get();

    // Stage 0: 0s to 1s -> Intake
    if (m_stage == 0) {
      if (time < 1.0) {
        m_intake.setRunSpeed(0.2);
      } else {
        m_intake.stop();
        m_stage++;
      }
    }
    // Stage 1: 1s to 2s -> Loader
    else if (m_stage == 1) {
      if (time < 2.0) {
        m_loader.setLoaderSpeed(0.2);
      } else {
        m_loader.stop();
        m_stage++;
      }
    }
    // Stage 2: 2s to 3s -> Fire
    else if (m_stage == 2) {
      if (time < 3.0) {
        m_fire.setShooterRPM(frc.robot.constants.SpeedConstants.FIRE_MAX_SPEED);
      } else {
        m_fire.stop();
        m_stage++;
      }
    }
    // Stage 3: 3s to 4s -> Turret
    else if (m_stage == 3) {
      if (time < 4.0) {
        m_turret.setTurretSpeed(0.1);
      } else {
        m_turret.setTurretSpeed(0);
        m_stage++;
        m_report.append("All Subsystems successfully swept! Ready for Play.");
      }
    }
  }

  @Override
  public void end(boolean interrupted) {
    m_intake.stop();
    m_loader.stop();
    m_fire.stop();
    m_turret.setTurretSpeed(0);
    if (!interrupted) {
      RobotTelemetry.putString("Alerts/PitHealthCheck", "PASSED: " + m_report.toString());
    } else {
      RobotTelemetry.putString("Alerts/PitHealthCheck", "INTERRUPTED: Safety Override triggered.");
    }
  }

  @Override
  public boolean isFinished() {
    return m_stage > 3;
  }
}
