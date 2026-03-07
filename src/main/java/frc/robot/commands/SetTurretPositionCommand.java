package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.TurretSubsystem;

public class SetTurretPositionCommand extends Command {
  private final TurretSubsystem m_turret;
  private final double m_targetAngleDegrees;
  private final double m_toleranceDegrees = 2.0;

  public SetTurretPositionCommand(TurretSubsystem turret, double targetAngleDegrees) {
    m_turret = turret;
    m_targetAngleDegrees = targetAngleDegrees;
    // Use addRequirements() here to declare subsystem dependencies.
    addRequirements(turret);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    m_turret.setTargetAngle(m_targetAngleDegrees);
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    // The SparkMax built in PID will handle the movement automatically.
    // We optionally can continuously set the reference here just in case.
    m_turret.setTargetAngle(m_targetAngleDegrees);
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return m_turret.isAtAngle(m_targetAngleDegrees, m_toleranceDegrees);
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    // We intentionally don't call stop() here so the PID can hold the position
  }
}
