package first.robot;

import com.revrobotics.spark.A301;
import org.wpilib.driverstation.DefaultUserControls;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Teleop;

@Teleop(name = "MecanumDriveTeleOp")
public class MecanumDriveTeleOp extends PeriodicOpMode {

    private final DefaultUserControls userControls;

    private A301 motor;
    private A301 motor0;
    private A301 motor1;
    private A301 motor2;

    public MecanumDriveTeleOp(DefaultUserControls userControls) {
        System.out.println("Constructor called");
        this.userControls = userControls;
    }

    @Override
    public void start() {
        System.out.println("START");

        try {
            motor  = new A301(5, 3);
            motor0 = new A301(6, 3);
            motor1 = new A301(7, 3);
            motor2 = new A301(8, 3);

            System.out.println("CREATED");
            System.out.println("FW: " + motor.getFirmwareString());

            motor.setThrottle(0.0);
            motor0.setThrottle(0.0);
            motor1.setThrottle(0.0);
            motor2.setThrottle(0.0);

        } catch (Throwable t) {
            System.out.println("FAILED");
            t.printStackTrace();
        }
    }

    @Override
    public void periodic() {
        var gamepad = userControls.getGamepad(0);

        if (motor == null) {
            return;
        }

        // Button overrides
        if (gamepad.getNorthFaceButton()) {

            motor.setThrottle(0.5);
            motor0.setThrottle(0.5);
            motor1.setThrottle(0.5);
            motor2.setThrottle(0.5);

        } else if (gamepad.getSouthFaceButton()) {

            motor.setThrottle(-0.5);
            motor0.setThrottle(-0.5);
            motor1.setThrottle(-0.5);
            motor2.setThrottle(-0.5);

        } else {

            double forward = -gamepad.getLeftY();
            double strafe  =  gamepad.getLeftX();
            double rotate  =  gamepad.getRightX();

            if (Math.abs(forward) < 0.05) forward = 0.0;
            if (Math.abs(strafe)  < 0.05) strafe  = 0.0;
            if (Math.abs(rotate)  < 0.05) rotate  = 0.0;

            // Standard mecanum mix
            double m0 = forward + strafe + rotate;
            double m1 = forward - strafe - rotate;
            double m2 = forward - strafe + rotate;
            double m3 = forward + strafe - rotate;

            // Normalize so nothing exceeds ±1.0
            double max = Math.max(
                    1.0,
                    Math.max(
                            Math.abs(m0),
                            Math.max(
                                    Math.abs(m1),
                                    Math.max(Math.abs(m2), Math.abs(m3))
                            )
                    )
            );

            m0 /= max;
            m1 /= max;
            m2 /= max;
            m3 /= max;

            motor.setThrottle(m0);
            motor0.setThrottle(m1);
            motor1.setThrottle(m2);
            motor2.setThrottle(m3);
        }
    }

    @Override
    public void end() {

        if (motor != null) {
            motor.setThrottle(0.0);
            motor0.setThrottle(0.0);
            motor1.setThrottle(0.0);
            motor2.setThrottle(0.0);
        }
    }

    @Override
    public void close() {

        if (motor != null) motor.close();
        if (motor0 != null) motor0.close();
        if (motor1 != null) motor1.close();
        if (motor2 != null) motor2.close();
    }
}