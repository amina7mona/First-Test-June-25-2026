# FTC OpMode to SystemCore Structure Guide

## Goal

This guide is for FTC students reading the SystemCore / WPILib-style examples in this
project.

FTC students are usually used to writing one OpMode class that directly contains:

- hardware lookup from `hardwareMap`
- `init()` / `start()` / `loop()` or `runOpMode()`
- gamepad reads
- motor and servo commands
- `telemetry.addData(...)` and `telemetry.update()`

This project uses a more FRC-style structure. Hardware lives in a shared `Robot`
class, OpModes receive the robot object through their constructor, and telemetry can
be published through NetworkTables.

The purpose of this document is to show what each structure is, what the closest FTC
equivalent is, where FTC students usually saw it, and how it is used here.

## Files in This Project

| File | Purpose |
| --- | --- |
| `Robot.java` | Shared robot hardware container |
| `DefaultTeleMode.java` | Simple driver-control example |
| `DefaultAutoMode.java` | Simple timed autonomous example |
| `KiwiDriveExampleTeleMode.java` | Kiwi drive example without telemetry |
| `KiwiDriveExampleWithNetworkTableTelemetryTeleMode.java` | Kiwi drive plus NetworkTables telemetry |
| `NetworkTableTelemetryExampleTeleMode.java` | Telemetry-only example |

## Big Picture Comparison

| SystemCore / WPILib-style structure | FTC equivalent | Where FTC students usually saw it | How this project uses it |
| --- | --- | --- | --- |
| `Robot extends OpModeRobot` | The shared hardware setup that FTC teams often put inside each OpMode or a helper hardware class | `hardwareMap.get(...)` in `init()` or `runOpMode()` | Declares motors and servos once in `Robot.java` |
| `@UserControlsInstance(DefaultUserControls.class)` | Built-in `gamepad1` / `gamepad2` access | Fields available inside every FTC OpMode | Tells SystemCore which user-control provider to inject |
| `@Teleop` | `@TeleOp` | Above an FTC driver-control OpMode class | Adds the class to the TeleOp list |
| `@Autonomous` | `@Autonomous` | Above an FTC autonomous OpMode class | Adds the class to the Autonomous list |
| `PeriodicOpMode` | FTC iterative `OpMode` | `init()`, `start()`, `loop()` | Parent class for repeatedly running `periodic()` |
| constructor injection | Usually not used in FTC OpModes | FTC OpModes often directly access inherited fields | SystemCore passes `Robot` and `DefaultUserControls` into each OpMode constructor |
| `start()` | `start()` in iterative FTC OpMode, or the moment after `waitForStart()` in LinearOpMode | Setup done right when the match period begins | Starts timers, configures motors, or zeros outputs |
| `periodic()` | `loop()` in iterative FTC OpMode | Main repeated driver-control or autonomous loop | Reads inputs, calculates outputs, commands hardware, publishes telemetry |
| `Timer` | `ElapsedTime` | Timed autonomous steps | Chooses motor commands based on elapsed seconds |
| `NetworkTables` publishers | `telemetry.addData(...)` plus `telemetry.update()` | Driver Station telemetry | Publishes values to dashboard tools such as OutlineViewer or Elastic |

## `Robot.java`: Shared Hardware Container

In FTC, many students are used to this pattern inside each OpMode:

```java
DcMotor leftMotor = hardwareMap.get(DcMotor.class, "leftMotor");
Servo claw = hardwareMap.get(Servo.class, "claw");
```

In this SystemCore project, hardware is declared in `Robot.java`:

```java
public class Robot extends OpModeRobot {
  public final ExpansionHubMotor motor0 = new ExpansionHubMotor(0, 0);
  public final ExpansionHubMotor motor1 = new ExpansionHubMotor(0, 1);
  public final ExpansionHubMotor motor2 = new ExpansionHubMotor(0, 2);
  public final ExpansionHubMotor motor3 = new ExpansionHubMotor(0, 3);

  public final ExpansionHubServo servo0 = new ExpansionHubServo(0, 0);
  public final ExpansionHubServo servo1 = new ExpansionHubServo(0, 1);
}
```

| SystemCore term | FTC comparison | Meaning here |
| --- | --- | --- |
| `ExpansionHubMotor` | `DcMotor` | Motor connected to an Expansion Hub motor port |
| `ExpansionHubServo` | `Servo` | Servo connected to an Expansion Hub servo port |
| `new ExpansionHubMotor(0, 1)` | `hardwareMap.get(DcMotor.class, "...")` | Motor on hub `0`, port `1` |
| `new ExpansionHubServo(0, 0)` | `hardwareMap.get(Servo.class, "...")` | Servo on hub `0`, port `0` |
| `robot.motor1` | a motor field in an FTC OpMode | Access a motor from the shared robot object |

The important structural difference is that FTC examples often put hardware lookup
inside the OpMode, while this project centralizes hardware in `Robot.java`.

## TeleOp Classes

FTC iterative OpModes usually look like:

```java
@TeleOp
public class MyTeleOp extends OpMode {
  public void loop() {
    leftMotor.setPower(gamepad1.left_stick_y);
  }
}
```

This project uses:

```java
@Teleop
public class DefaultTeleMode extends PeriodicOpMode {
  private final Robot robot;
  private final DefaultUserControls userControls;

  public DefaultTeleMode(Robot robot, DefaultUserControls userControls) {
    this.robot = robot;
    this.userControls = userControls;
  }

  @Override
  public void periodic() {
    robot.motor0.setThrottle(-userControls.getGamepad(0).getLeftY());
  }
}
```

| SystemCore structure | FTC equivalent | Meaning here |
| --- | --- | --- |
| `@Teleop` | `@TeleOp` | Register this as a driver-controlled mode |
| `extends PeriodicOpMode` | `extends OpMode` | This class has lifecycle methods that run repeatedly |
| constructor with `Robot robot` | inherited OpMode access to hardware or fields | Receives the shared hardware container |
| constructor with `DefaultUserControls userControls` | inherited `gamepad1` / `gamepad2` | Receives gamepad access |
| `periodic()` | `loop()` | Runs repeatedly while enabled |

## Autonomous Classes

FTC students often use `ElapsedTime` in autonomous:

```java
ElapsedTime timer = new ElapsedTime();

if (timer.seconds() < 2.0) {
  motor.setPower(0.5);
}
```

This project uses a WPILib-style `Timer`:

```java
private final Timer timer = new Timer();

@Override
public void start() {
  timer.reset();
  timer.start();
}

@Override
public void periodic() {
  if (timer.get() < 2.0) {
    robot.motor0.setThrottle(0.5);
  } else {
    robot.motor0.setThrottle(0.0);
  }
}
```

| SystemCore term | FTC equivalent | Meaning here |
| --- | --- | --- |
| `Timer` | `ElapsedTime` | Tracks elapsed seconds |
| `timer.reset()` | `timer.reset()` | Set time back to zero |
| `timer.start()` | timer begins after reset / start | Begin counting |
| `timer.get()` | `timer.seconds()` | Read elapsed time |
| `start()` | `start()` or after `waitForStart()` | Initialize autonomous timing |
| `periodic()` | `loop()` or repeated code in `while (opModeIsActive())` | Run timed autonomous logic |

## Gamepad Access

FTC gamepad code usually looks like:

```java
double leftY = gamepad1.left_stick_y;
boolean leftBumper = gamepad1.left_bumper;
```

This project uses `DefaultUserControls`:

```java
var gamepad = userControls.getGamepad(0);

double leftY = gamepad.getLeftY();
boolean leftBumper = gamepad.getLeftBumperButton();
```

| SystemCore call | FTC equivalent | Meaning |
| --- | --- | --- |
| `userControls.getGamepad(0)` | `gamepad1` | First driver controller |
| `userControls.getGamepad(1)` | `gamepad2` | Second driver controller |
| `getLeftX()` | `gamepad1.left_stick_x` | Left stick horizontal axis |
| `getLeftY()` | `gamepad1.left_stick_y` | Left stick vertical axis |
| `getRightX()` | `gamepad1.right_stick_x` | Right stick horizontal axis |
| `getRightY()` | `gamepad1.right_stick_y` | Right stick vertical axis |
| `getLeftTriggerAxis()` | `gamepad1.left_trigger` | Left trigger value |
| `getRightTriggerAxis()` | `gamepad1.right_trigger` | Right trigger value |
| `getLeftBumperButton()` | `gamepad1.left_bumper` | Left bumper button |
| `getRightBumperButton()` | `gamepad1.right_bumper` | Right bumper button |
| `getSouthFaceButton()` | `gamepad1.a` or PlayStation cross | Bottom face button |

One common FTC habit still appears here: joystick Y is often negated so pushing the
stick forward produces a positive forward value.

```java
double y = -gamepad.getLeftY();
```

## Motor and Servo Output

FTC output code usually looks like:

```java
motor.setPower(0.5);
servo.setPosition(1.0);
```

This project uses:

```java
robot.motor0.setThrottle(0.5);
robot.servo0.setPosition(1.0);
```

| SystemCore call | FTC equivalent | Meaning |
| --- | --- | --- |
| `setThrottle(0.5)` | `setPower(0.5)` | Run motor forward at partial power |
| `setThrottle(0.0)` | `setPower(0.0)` | Stop the motor |
| `setThrottle(-0.5)` | `setPower(-0.5)` | Run motor the opposite direction |
| `setPosition(0.0)` | `setPosition(0.0)` | Move servo to one end of travel |
| `setPosition(1.0)` | `setPosition(1.0)` | Move servo to the other end of travel |
| `setFloatOn0(false)` | zero-power behavior setup | Configure motor behavior when throttle is zero |

## Kiwi Drive Example

`KiwiDriveExampleTeleMode.java` shows a three-wheel Kiwi drivetrain.

The FTC-style concept is the same as any drivetrain math example:

1. Read joystick inputs.
2. Apply a deadband.
3. Convert driver commands into wheel powers.
4. Normalize the wheel powers so none exceed the safe range.
5. Send the final values to the motors.

Important helper structures:

| Structure | FTC equivalent | Meaning here |
| --- | --- | --- |
| `applyDeadband(value)` | joystick dead zone helper | Turns tiny stick values into `0.0` |
| `DEADBAND` | dead zone constant | How much stick movement to ignore |
| `PRECISION_SCALE` | slow mode multiplier | Makes the robot drive slower when left bumper is held |
| `FRONT_LEFT_MOTOR_SIGN` | `DcMotorSimple.Direction.REVERSE` or sign flip | Used to reverse one physical wheel if needed |
| `maxMagnitude` | drivetrain normalization | Scales powers so the largest magnitude is at most `1.0` |

FTC students may expect motor direction to be handled with:

```java
motor.setDirection(DcMotorSimple.Direction.REVERSE);
```

In these samples, the Kiwi drive code uses sign constants instead:

```java
private static final double FRONT_LEFT_MOTOR_SIGN = 1.0;
private static final double FRONT_RIGHT_MOTOR_SIGN = 1.0;
private static final double BACK_MOTOR_SIGN = 1.0;
```

If a wheel spins the wrong way on the real robot, change only that wheel's sign.

## Telemetry and NetworkTables

FTC telemetry usually looks like:

```java
telemetry.addData("left x", leftX);
telemetry.addData("button pressed", isPressed);
telemetry.update();
```

The NetworkTables examples use publishers:

```java
private final NetworkTable telemetryExampleTable =
    NetworkTableInstance.getDefault().getTable("TelemetryExample");

private final DoublePublisher leftXPublisher =
    telemetryExampleTable.getDoubleTopic("leftX").publish();

leftXPublisher.set(leftX);
```

| NetworkTables structure | FTC equivalent | Meaning here |
| --- | --- | --- |
| `NetworkTableInstance.getDefault()` | the telemetry system being available to the OpMode | Access the shared NetworkTables instance |
| `getTable("TelemetryExample")` | telemetry caption grouping by name | Create or access a table of values |
| `getDoubleTopic("leftX").publish()` | `telemetry.addData("leftX", number)` setup | Create a numeric value publisher |
| `getBooleanTopic(...).publish()` | `telemetry.addData("button", boolean)` setup | Create a true/false value publisher |
| `getStringTopic(...).publish()` | `telemetry.addData("status", text)` setup | Create a text value publisher |
| `publisher.set(value)` | `telemetry.addData(...)` with the latest value | Publish the newest value |
| `publishTelemetry(...)` | the telemetry block before `telemetry.update()` | Helper method that sends all telemetry values |

The main difference is that FTC telemetry often feels like writing lines to the
Driver Station screen. NetworkTables is more like publishing named values to a shared
data tree that dashboard tools can display.

For dashboard viewing steps, see:

```text
OUTLINEVIEWER_AND_ELASTIC_NETWORKTABLES_README.md
```

## Lifecycle Cheat Sheet

| FTC iterative OpMode | FTC LinearOpMode | SystemCore sample structure |
| --- | --- | --- |
| hardware fields | hardware fields or helper class | `Robot.java` fields |
| `init()` | code before `waitForStart()` | constructor plus `Robot.java` setup |
| `start()` | immediately after `waitForStart()` | `start()` |
| `loop()` | `while (opModeIsActive())` body | `periodic()` |
| `stop()` | code after loop exits | not shown in these samples |
| `gamepad1` | `gamepad1` | `userControls.getGamepad(0)` |
| `motor.setPower(...)` | `motor.setPower(...)` | `robot.motorX.setThrottle(...)` |
| `telemetry.addData(...)` | `telemetry.addData(...)` | NetworkTables publisher `.set(...)` |

## Student Reading Order

For FTC students new to this structure, read the samples in this order:

1. `Robot.java` to see where hardware is declared.
2. `DefaultTeleMode.java` to see the simplest gamepad-to-hardware loop.
3. `DefaultAutoMode.java` to see timed autonomous structure.
4. `KiwiDriveExampleTeleMode.java` to see drivetrain math.
5. `NetworkTableTelemetryExampleTeleMode.java` to see telemetry by itself.
6. `KiwiDriveExampleWithNetworkTableTelemetryTeleMode.java` to see drive code and telemetry combined.
