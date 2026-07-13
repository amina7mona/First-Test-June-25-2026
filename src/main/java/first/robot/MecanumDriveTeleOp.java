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
// package first.robot;

// import com.revrobotics.spark.A301;
// import org.wpilib.driverstation.DefaultUserControls;
// import org.wpilib.opmode.PeriodicOpMode;
// import org.wpilib.opmode.Teleop;

// @Teleop(name = "Mecanum Drive Test")
// public class MecanumDriveTeleOp extends PeriodicOpMode {

//     private static final double DEADBAND = 0.05;
//     private static final double MAX_OUTPUT = 0.35;

//     // MotionCore bus IDs
//     private static final int FRONT_LEFT_BUS = 5;
//     // private static final int BACK_LEFT_BUS = 6;
//     // private static final int FRONT_RIGHT_BUS = 7;
//     // private static final int BACK_RIGHT_BUS = 8;

//     private static final int DEVICE_ID = 3;

//     private final DefaultUserControls userControls;

//     private A301 frontLeft;
//     // private A301 backLeft;
//     // private A301 frontRight;
//     // private A301 backRight;

//     private boolean driveReady = false;

//     public MecanumDriveTeleOp(DefaultUserControls userControls) {
//         this.userControls = userControls;
//     }

//     @Override
//     public void start() {
//         try {
//             frontLeft = new A301(FRONT_LEFT_BUS, DEVICE_ID);
//             // backLeft = new A301(BACK_LEFT_BUS, DEVICE_ID);
//             // frontRight = new A301(FRONT_RIGHT_BUS, DEVICE_ID);
//             // backRight = new A301(BACK_RIGHT_BUS, DEVICE_ID);

//             frontLeft.setThrottle(0.0);
//             // backLeft.setThrottle(0.0);
//             // frontRight.setThrottle(0.0);
//             // backRight.setThrottle(0.0);

//             driveReady = true;

//             System.out.println("Mecanum initialized");
//         } catch (Throwable t) {
//             System.out.println("Failed to initialize mecanum drive");
//             t.printStackTrace();
//             driveReady = false;
//         }
//     }

//     @Override
//     public void periodic() {

//         if (!driveReady) {
//             return;
//         }

//         var gamepad = userControls.getGamepad(0);

//         double forward = -gamepad.getLeftY();
//         double strafe = gamepad.getLeftX();
//         double rotate = gamepad.getRightX();

//         if (Math.abs(forward) < DEADBAND) forward = 0.0;
//         if (Math.abs(strafe) < DEADBAND) strafe = 0.0;
//         if (Math.abs(rotate) < DEADBAND) rotate = 0.0;

//         double fl = forward + strafe + rotate;
//         double bl = forward - strafe + rotate;
//         double fr = forward - strafe - rotate;
//         double br = forward + strafe - rotate;

//         double max = Math.max(
//                 1.0,
//                 Math.max(
//                         Math.abs(fl),
//                         Math.max(
//                                 Math.abs(bl),
//                                 Math.max(Math.abs(fr), Math.abs(br))
//                         )
//                 )
//         );

//         fl = fl / max * MAX_OUTPUT;
//         bl = bl / max * MAX_OUTPUT;
//         fr = fr / max * MAX_OUTPUT;
//         br = br / max * MAX_OUTPUT;

//         frontLeft.setThrottle(fl);
//         // backLeft.setThrottle(bl);

//         // // Flip these if the right side runs backward
//         // frontRight.setThrottle(-fr);
//         // backRight.setThrottle(-br);
//     }

//     @Override
//     public void end() {
//         stopDrive();
//     }

//     @Override
//     public void close() {

//         stopDrive();

//         if (frontLeft != null) frontLeft.close();
//         // if (backLeft != null) backLeft.close();
//         // if (frontRight != null) frontRight.close();
//         // if (backRight != null) backRight.close();
//     }

//     private void stopDrive() {
//         if (frontLeft != null) frontLeft.setThrottle(0.0);
//         // if (backLeft != null) backLeft.setThrottle(0.0);
//         // if (frontRight != null) frontRight.setThrottle(0.0);
//         // if (backRight != null) backRight.setThrottle(0.0);
//     }
// }
// package first.robot;

// import com.revrobotics.spark.A301;
// import org.wpilib.driverstation.DefaultUserControls;
// import org.wpilib.opmode.PeriodicOpMode;
// import org.wpilib.opmode.Teleop;

// @Teleop(name = "Mecanum Drive")
// public class MecanumDriveTeleOp extends PeriodicOpMode {

//     // MotionCore bus IDs
//     private static final int D0 = 5;
//     private static final int D1 = 6;
//     private static final int D2 = 7;
//     private static final int D3 = 8;

//     private static final int CAN_ID = 3;

//     private final DefaultUserControls userControls;

//     private A301 frontLeft;
//     private A301 backLeft;
//     private A301 frontRight;
//     private A301 backRight;

//     public MecanumDriveTeleOp(DefaultUserControls userControls) {
//         this.userControls = userControls;
//     }

//     @Override
//     public void start() {
//         frontLeft = new A301(D0, CAN_ID);
//         backLeft = new A301(D1, CAN_ID);
//         frontRight = new A301(D2, CAN_ID);
//         backRight = new A301(D3, CAN_ID);
//     }

//     @Override
//     public void periodic() {

//         var gamepad = userControls.getGamepad(0);

//         double forward = -gamepad.getLeftY();
//         double strafe = gamepad.getLeftX();
//         double rotate = gamepad.getRightX();

//         // Deadband
//         if (Math.abs(forward) < 0.05) forward = 0;
//         if (Math.abs(strafe) < 0.05) strafe = 0;
//         if (Math.abs(rotate) < 0.05) rotate = 0;

//         double fl = forward + strafe + rotate;
//         double bl = forward - strafe + rotate;
//         double fr = forward - strafe - rotate;
//         double br = forward + strafe - rotate;

//         // Normalize
//         double max = Math.max(
//                 1.0,
//                 Math.max(
//                         Math.abs(fl),
//                         Math.max(
//                                 Math.abs(bl),
//                                 Math.max(Math.abs(fr), Math.abs(br))
//                         )
//                 )
//         );

//         fl /= max;
//         bl /= max;
//         fr /= max;
//         br /= max;

//         frontLeft.setThrottle(fl);
//         backLeft.setThrottle(bl);

//         // Reverse these if your right side spins backwards
//         frontRight.setThrottle(-fr);
//         backRight.setThrottle(-br);
//     }

//     @Override
//     public void end() {
//         frontLeft.setThrottle(0);
//         backLeft.setThrottle(0);
//         frontRight.setThrottle(0);
//         backRight.setThrottle(0);
//     }

//     @Override
//     public void close() {
//         frontLeft.close();
//         backLeft.close();
//         frontRight.close();
//         backRight.close();
//     }
// }
