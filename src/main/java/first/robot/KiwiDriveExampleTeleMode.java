// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot;

import org.wpilib.driverstation.DefaultUserControls;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Teleop;

@Teleop(name = "Kiwi Drive Example")
public class KiwiDriveExampleTeleMode extends PeriodicOpMode {
  /*
   * Kiwi drive example for an FTC-style three-wheel Kiwi drivetrain on SystemCore.
   *
   * Robot layout used here:
   *
   *                  Front of robot
   *
   *             motor3                 motor1
   *        front-left wheel       front-right wheel
   *          Hub port 3             Hub port 1
   *
   *
   *                       motor2
   *                     back wheel
   *                    Hub port 2
   *
   *   motor0 / Hub port 0 is unused here.
   *
   * Motor assumption for this robot:
   *   The drivetrain uses goBILDA 5203-series motors.
   *   On this SystemCore alpha setup, positive throttle was observed to turn each motor
   *   counterclockwise when looking at the output shaft.
   *
   * FTC SDK comparison:
   *   In the normal FTC SDK, each configured motor has a MotorConfigurationType.
   *   MotorConfigurationType is filled from a @MotorType annotation, including an
   *   orientation() value. DcMotorImpl then uses that motor type orientation together
   *   with DcMotorSimple.Direction to flip power/encoder signs internally. This makes
   *   configured FTC motor types behave consistently from team code.
   *
   * SystemCore alpha note:
   *   ExpansionHubMotor.setThrottle() is lower-level here. It does not know that these
   *   are goBILDA 5203 motors from an FTC Robot Configuration screen, so this OpMode
   *   keeps optional per-wheel sign constants below.
   *
   * Direction setup instruction:
   *   This file currently matches the tested working KiwiDriveTeleMode behavior.
   *   Before full-speed driving on another robot, test each wheel at low power. If a
   *   wheel's physical direction is wrong, reverse only that wheel by changing its
   *   *_MOTOR_SIGN constant.
   */
  private static final double SQRT_3_OVER_2 = 0.8660254037844386;
  private static final double DEADBAND = 0.05;
  private static final double PRECISION_SCALE = 0.45;

  /*
   * These constants adapt mathematical wheel commands to the real motor/output-shaft
   * direction. They are 1.0 here so this example behaves the same as the tested
   * working KiwiDriveTeleMode. Change only the wheel that is physically reversed.
   */
  private static final double FRONT_LEFT_MOTOR_SIGN = 1.0;
  private static final double FRONT_RIGHT_MOTOR_SIGN = 1.0;
  private static final double BACK_MOTOR_SIGN = 1.0;

  private final Robot robot;
  private final DefaultUserControls userControls;

  public KiwiDriveExampleTeleMode(Robot robot, DefaultUserControls userControls) {
    this.robot = robot;
    this.userControls = userControls;
  }

  @Override
  public void start() {
    robot.motor0.setFloatOn0(false);
    robot.motor1.setFloatOn0(false);
    robot.motor2.setFloatOn0(false);
    robot.motor3.setFloatOn0(false);
  }

  @Override
  public void periodic() {
    var gamepad = userControls.getGamepad(0);

    double x = applyDeadband(gamepad.getLeftX());
    double y = applyDeadband(-gamepad.getLeftY());
    double rotation = applyDeadband(gamepad.getRightX());
    double speedScale = gamepad.getLeftBumperButton() ? PRECISION_SCALE : 1.0;

    /*
     * Kiwi inverse kinematics for this two-front, one-back wheel layout.
     *
     * x: sideways robot translation, using the raw left-stick X direction
     * y: forward robot translation
     * rotation: robot yaw
     *
     * The front wheels are angled 60 degrees from the robot's sideways axis,
     * so their sideways contribution is cos(60) = 0.5 and their forward
     * contribution is sin(60) = sqrt(3) / 2.
     *
     * The back wheel is sideways, so it responds to x and rotation but not y.
     * Keeping x as the raw joystick value makes the student-facing control
     * convention clear; the signs below describe this robot's wheel geometry.
     */
    double frontLeftPower = (0.5 * x) + (SQRT_3_OVER_2 * y) + rotation;
    double frontRightPower = (0.5 * x) - (SQRT_3_OVER_2 * y) + rotation;
    double backPower = -x + rotation;

    double maxMagnitude =
        Math.max(
            1.0,
            Math.max(
                Math.abs(frontLeftPower),
                Math.max(Math.abs(frontRightPower), Math.abs(backPower))));

    double frontLeftThrottle =
        FRONT_LEFT_MOTOR_SIGN * speedScale * frontLeftPower / maxMagnitude;
    double frontRightThrottle =
        FRONT_RIGHT_MOTOR_SIGN * speedScale * frontRightPower / maxMagnitude;
    double backThrottle = BACK_MOTOR_SIGN * speedScale * backPower / maxMagnitude;

    robot.motor0.setThrottle(0.0);
    robot.motor3.setThrottle(frontLeftThrottle);
    robot.motor1.setThrottle(frontRightThrottle);
    robot.motor2.setThrottle(backThrottle);
  }

  private static double applyDeadband(double value) {
    if (Math.abs(value) < DEADBAND) {
      return 0.0;
    }

    return value;
  }
}
