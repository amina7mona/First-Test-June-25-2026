package first.robot;

import com.revrobotics.spark.A301;
import org.wpilib.driverstation.DefaultUserControls;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Teleop;

@Teleop(name = "Mecanum Drive")
public class MecanumDriveTeleOp extends PeriodicOpMode {

    // MotionCore bus IDs
    private static final int D0 = 5;
    private static final int D1 = 6;
    private static final int D2 = 7;
    private static final int D3 = 8;

    private static final int CAN_ID = 3;

    private final DefaultUserControls userControls;

    private A301 frontLeft;
    private A301 backLeft;
    private A301 frontRight;
    private A301 backRight;

    public MecanumDriveTeleOp(DefaultUserControls userControls) {
        this.userControls = userControls;
    }

    @Override
    public void start() {
        frontLeft = new A301(D0, CAN_ID);
        backLeft = new A301(D1, CAN_ID);
        frontRight = new A301(D2, CAN_ID);
        backRight = new A301(D3, CAN_ID);
    }

    @Override
    public void periodic() {

        var gamepad = userControls.getGamepad(0);

        double forward = -gamepad.getLeftY();
        double strafe = gamepad.getLeftX();
        double rotate = gamepad.getRightX();

        // Deadband
        if (Math.abs(forward) < 0.05) forward = 0;
        if (Math.abs(strafe) < 0.05) strafe = 0;
        if (Math.abs(rotate) < 0.05) rotate = 0;

        double fl = forward + strafe + rotate;
        double bl = forward - strafe + rotate;
        double fr = forward - strafe - rotate;
        double br = forward + strafe - rotate;

        // Normalize
        double max = Math.max(
                1.0,
                Math.max(
                        Math.abs(fl),
                        Math.max(
                                Math.abs(bl),
                                Math.max(Math.abs(fr), Math.abs(br))
                        )
                )
        );

        fl /= max;
        bl /= max;
        fr /= max;
        br /= max;

        frontLeft.setThrottle(fl);
        backLeft.setThrottle(bl);

        // Reverse these if your right side spins backwards
        frontRight.setThrottle(-fr);
        backRight.setThrottle(-br);
    }

    @Override
    public void end() {
        frontLeft.setThrottle(0);
        backLeft.setThrottle(0);
        frontRight.setThrottle(0);
        backRight.setThrottle(0);
    }

    @Override
    public void close() {
        frontLeft.close();
        backLeft.close();
        frontRight.close();
        backRight.close();
    }
}
