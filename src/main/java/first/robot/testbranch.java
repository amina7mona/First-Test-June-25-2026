package first.robot;

import com.revrobotics.spark.A301;
import org.wpilib.driverstation.DefaultUserControls;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Teleop;

@Teleop(name = "testbranch")
public class testbranch extends PeriodicOpMode {

    private final DefaultUserControls userControls;
    private A301 motor;

    public testbranch(DefaultUserControls userControls) {
        System.out.println("Constructor called");
        this.userControls = userControls;
    }

    @Override
    public void start() {
        System.out.println("START");

        try {
            // MotionCore D0, CAN ID 3
            motor = new A301(5, 3);

            System.out.println("CREATED");
            System.out.println("FW: " + motor.getFirmwareString());

            motor.setThrottle(0.0);

        } catch (Throwable t) {
            System.out.println("FAILED");
            t.printStackTrace();
        }
    }

    @Override
    public void periodic() {

        if (motor == null) {
            return;
        }

        var gamepad = userControls.getGamepad(0);

        // Right stick Y
        double throttle = -gamepad.getRightY();

        // Deadband
        if (Math.abs(throttle) < 0.05) {
            throttle = 0.0;
        }

        // Limit to 50% power
        throttle *= 0.5;

        motor.setThrottle(throttle);

        // Debug
        System.out.println("Throttle = " + throttle);
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