package frc.robot;

import edu.wpi.first.wpilibj.RobotBase;

public final class Main {
  private Main() {}

  /**
   * Main initialization function. Do not perform any initialization here.
   * simon says https://www.youtube.com/watch?v=Ea3ftznwExk&list=RDEa3ftznwExk
   * <p>If you change your main robot class, change the parameter type.
   */
  public static void main(String... args) {
    RobotBase.startRobot(Robot::new);
  }
}
