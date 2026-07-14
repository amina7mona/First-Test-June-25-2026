package first.robot;

import com.revrobotics.spark.A301;
import org.wpilib.driverstation.DefaultUserControls;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Teleop;

@Teleop(name = "A301 Single Motor Test")
public class A301SingleMotorTestTeleMode extends PeriodicOpMode {

  private final DefaultUserControls userControls;
  private A301 motor;

  public A301SingleMotorTestTeleMode(DefaultUserControls userControls) {
    System.out.println("Constructor called");
    this.userControls = userControls;
  }

  @Override
public void start() {
    System.out.println("START");

    try {
        motor = new A301(5, 3);
        System.out.println("CREATED");
        System.out.println("FW: " + motor.getFirmwareString());
        motor.setThrottle(0.0);
    } catch (Throwable t) {
        System.out.println("FAILED");
        t.printStackTrace();
    }
}

// @Override
// public void periodic() {
//     var gamepad = userControls.getGamepad(0);

//     if (motor == null) {
//         return;
//     }

//     // Triangle = Forward
//     if (gamepad.getNorthFaceButton()) {
//         motor.setThrottle(0.20);
//     }
//     // Cross = Reverse
//     else if (gamepad.getSouthFaceButton()) {
//         motor.setThrottle(-0.20);
//     }
//     // No button = Stop
//     else {
//         motor.setThrottle(0.0);
//     }
// }

@Override
public void periodic() {
    var gamepad = userControls.getGamepad(0);

    if (motor == null) {
        return;
    }

    // Button overrides
    if (gamepad.getNorthFaceButton()) {          // Triangle
        motor.setThrottle(0.5);
    } else if (gamepad.getSouthFaceButton()) {   // Cross
        motor.setThrottle(-0.5);
    } else {
        // Right joystick Y-axis
        double throttle = -gamepad.getRightY();

        // Small deadband so tiny joystick movements don't move the motor
        if (Math.abs(throttle) < 0.05) {
            throttle = 0.0;
        }

        // Joystick controls motor proportionally
        motor.setThrottle(throttle);
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