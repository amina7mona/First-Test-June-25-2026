package first.robot;

import com.revrobotics.spark.A301;
import org.wpilib.driverstation.DefaultUserControls;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Teleop;

@Teleop(name = "A301 Single Motor Test")
public class A301SingleMotorTestTeleMode extends PeriodicOpMode {

  private static final int MOTIONCORE_CAN_D0_BUS_ID = 5;

  private static final int DEFAULT_A301_CAN_ID = 3;

  private final DefaultUserControls userControls;
  private A301 motor;

  public A301SingleMotorTestTeleMode(DefaultUserControls userControls) {
    System.out.println("Constructor called");
    this.userControls = userControls;
  }

  @Override
  public void start() {
    System.out.println("running");

    try {
      motor = new A301(MOTIONCORE_CAN_D0_BUS_ID, DEFAULT_A301_CAN_ID);

      System.out.println("Firmware: " + motor.getFirmwareString());

      motor.setThrottle(0.0);

    } catch (Throwable t) {
      System.out.println("failed");
      t.printStackTrace();
      motor = null;
    }
  }

  @Override
  public void periodic() {
    System.out.println("moving motor");

    if (motor != null) {
      motor.setThrottle(0.20);
    }
  }

  @Override
  public void end() {

    if (motor != null) {
      motor.setThrottle(0.0);
    }
  }

  @Override
  public void close() {

    if (motor != null) {
      motor.close();
    }
  }
}