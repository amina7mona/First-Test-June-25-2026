# FTC Student Ultimate Guide to SystemCore Structure

How familiar FTC OpMode ideas map to this FRC-style SystemCore project.

> [!NOTE]
> This guide expands on the shorter
> [FTC_TO_SYSTEMCORE_STRUCTURE_README.md](FTC_TO_SYSTEMCORE_STRUCTURE_README.md).
> Everything here is based on the actual sample code in this project, so the names
> match what you will see when you open the files.

## Contents

1. [Quick Orientation](#1-quick-orientation)
2. [Project Map](#2-project-map)
3. [Hardware Setup: `hardwareMap` vs `Robot.java`](#3-hardware-setup-hardwaremap-vs-robotjava)
4. [OpMode Registration: `@TeleOp` vs `@Teleop`](#4-opmode-registration-teleop-vs-teleop)
5. [Lifecycle: `init/start/loop` vs `start/periodic`](#5-lifecycle-initstartloop-vs-startperiodic)
6. [Constructor Injection: Why OpModes Receive `Robot`](#6-constructor-injection-why-opmodes-receive-robot)
7. [Gamepad Input: `gamepad1` vs `DefaultUserControls`](#7-gamepad-input-gamepad1-vs-defaultusercontrols)
8. [Motor and Servo Output: `setPower` vs `setThrottle`](#8-motor-and-servo-output-setpower-vs-setthrottle)
9. [Autonomous Timing: `ElapsedTime` vs `Timer`](#9-autonomous-timing-elapsedtime-vs-timer)
10. [Telemetry: `telemetry.addData` vs NetworkTables](#10-telemetry-telemetryadddata-vs-networktables)
11. [Kiwi Drive Case Study](#11-kiwi-drive-case-study)
12. [Side-by-Side Mini Examples](#12-side-by-side-mini-examples)
13. [Common FTC Assumptions That Change](#13-common-ftc-assumptions-that-change)
14. [Student Reading Order](#14-student-reading-order)
15. [Glossary](#15-glossary)

---

## 1. Quick Orientation

In FTC, one OpMode class usually does everything. It looks up hardware in `init()`,
reads `gamepad1` in `loop()`, commands motors, and calls `telemetry.update()` — all
in the same file. If you wrote three OpModes, you probably copied the same
`hardwareMap.get(...)` lines into all three.

This SystemCore project splits those jobs apart:

- **Hardware** lives in one shared class, `Robot.java`.
- **OpModes** (`DefaultTeleMode`, `DefaultAutoMode`, the Kiwi examples) contain only
  behavior. They receive the shared `Robot` object through their constructor.
- **Gamepad access** comes from a `DefaultUserControls` object instead of an
  inherited `gamepad1` field.
- **Telemetry** goes through NetworkTables publishers instead of
  `telemetry.addData(...)`.

None of the ideas are new to you. What moves is *where each idea lives*.

| FTC SDK Habit | SystemCore Project Habit |
| --- | --- |
| OpMode owns hardware fields directly | `Robot.java` owns hardware fields |
| `hardwareMap.get(...)` in `init()` | `new ExpansionHubMotor(hub, port)` in `Robot.java` |
| `gamepad1` inherited by OpMode | `userControls.getGamepad(0)` |
| `loop()` | `periodic()` |
| `motor.setPower(...)` | `robot.motorX.setThrottle(...)` |
| `telemetry.addData(...)` | NetworkTables publisher `.set(...)` |

Keep this table in mind. The rest of the guide walks through each row with real code
from this project.

## 2. Project Map

All the robot code lives in `src/main/java/first/robot/`.

| File | FTC mental equivalent | Role in this project |
| --- | --- | --- |
| `Robot.java` | Hardware class, or the `hardwareMap` setup you copied into every OpMode | Declares the four motors and two servos once |
| `DefaultTeleMode.java` | The simplest possible `@TeleOp` | Direct gamepad-to-hardware test: sticks drive motors, triggers move servos |
| `DefaultAutoMode.java` | A simple timed `@Autonomous` | Runs two motors on a timed schedule, then stops |
| `KiwiDriveExampleTeleMode.java` | A drivetrain TeleOp | Full Kiwi drive math: deadband, kinematics, normalization, motor signs |
| `NetworkTableTelemetryExampleTeleMode.java` | An OpMode you write just to test telemetry | Publishes gamepad numbers, booleans, and status strings — does not drive |
| `KiwiDriveExampleWithNetworkTableTelemetryTeleMode.java` | Drive OpMode plus telemetry lines | The Kiwi drive code plus NetworkTables publishing of inputs and outputs |

There is one more difference from FTC worth noticing right away: there is exactly
**one** `Robot.java`, and **every** OpMode shares it. In FTC, each OpMode typically
had its own private copy of the hardware fields.

## 3. Hardware Setup: `hardwareMap` vs `Robot.java`

### What you did in FTC

Hardware lookup lived inside each OpMode, usually in `init()`:

```java
private DcMotor leftMotor;
private Servo claw;

@Override
public void init() {
  leftMotor = hardwareMap.get(DcMotor.class, "leftMotor");
  claw = hardwareMap.get(Servo.class, "claw");
}
```

The string `"leftMotor"` had to match a device name in the Robot Configuration on
the Driver Station. If the names didn't match, you got a crash at init.

### Where it went in SystemCore

In this project, all of that moved into [Robot.java](src/main/java/first/robot/Robot.java):

```java
@UserControlsInstance(DefaultUserControls.class)
public class Robot extends OpModeRobot {
  public final ExpansionHubMotor motor0 = new ExpansionHubMotor(0, 0);
  public final ExpansionHubMotor motor1 = new ExpansionHubMotor(0, 1);
  public final ExpansionHubMotor motor2 = new ExpansionHubMotor(0, 2);
  public final ExpansionHubMotor motor3 = new ExpansionHubMotor(0, 3);

  public final ExpansionHubServo servo0 = new ExpansionHubServo(0, 0);
  public final ExpansionHubServo servo1 = new ExpansionHubServo(0, 1);
}
```

Notice what changed:

- **No device names.** `new ExpansionHubMotor(0, 1)` means "the motor on hub `0`,
  port `1`." The hardware is identified by hub number and port number in code, not
  by a configured name on the Driver Station.
- **No `init()` lookup step.** The fields are created when `Robot` is created.
- **The fields are `public final`.** OpModes reach them as `robot.motor1`,
  `robot.servo0`, and so on. `final` means the reference never changes after
  construction.
- **One place to change wiring.** If you move a motor to a different port, you edit
  one line in `Robot.java` and every OpMode picks up the change.

The `@UserControlsInstance(DefaultUserControls.class)` annotation on `Robot` tells
SystemCore which gamepad-access class to provide to OpModes. More on that in
[section 7](#7-gamepad-input-gamepad1-vs-defaultusercontrols).

### Translation table

| FTC | SystemCore here | Student translation |
| --- | --- | --- |
| `DcMotor` | `ExpansionHubMotor` | Motor object |
| `Servo` | `ExpansionHubServo` | Servo object |
| `hardwareMap.get(...)` | `new ExpansionHubMotor(0, port)` | Connect code to hub hardware |
| configured device name | hub and port number | How the hardware is identified |
| private field in each OpMode | `public final` field in `Robot.java` | Where the hardware object lives |

> [!TIP]
> If you ever built a shared `RobotHardware` helper class in FTC so you wouldn't
> repeat `hardwareMap` calls, `Robot.java` is that same idea — except here it is the
> standard structure, not a team trick.

## 4. OpMode Registration: `@TeleOp` vs `@Teleop`

Both systems use an annotation above the class to put an OpMode into the driver
station's mode list.

FTC:

```java
@TeleOp(name = "My TeleOp")
public class MyTeleOp extends OpMode {
}
```

This project:

```java
@Teleop(name = "Kiwi Drive Example")
public class KiwiDriveExampleTeleMode extends PeriodicOpMode {
}
```

Three things to notice:

1. **The spelling is different.** FTC uses `@TeleOp` (capital O). This project uses
   `@Teleop` (lowercase o). If you type the FTC spelling here, it will not compile.
2. **`name` is optional in both.** `DefaultTeleMode` uses plain `@Teleop` with no
   name; the Kiwi examples pass names like `"Kiwi Drive Example"` and
   `"Kiwi Drive + NT Telemetry"` that show up in the mode list.
3. **`@Autonomous` exists in both** and serves the same broad purpose.
   `DefaultAutoMode` is annotated `@Autonomous`.

## 5. Lifecycle: `init/start/loop` vs `start/periodic`

This is the most important section of the guide. Everything else is naming; this is
about *when your code runs*.

`PeriodicOpMode` in this project is closest to FTC's **iterative `OpMode`** — the
one with `init()`, `start()`, and `loop()`. If you mostly wrote `LinearOpMode`, the
comparison still works, but map your `while (opModeIsActive())` body to `periodic()`.

| FTC Iterative OpMode | FTC LinearOpMode | SystemCore here |
| --- | --- | --- |
| `init()` | code before `waitForStart()` | constructor plus `Robot.java` setup |
| `start()` | code immediately after `waitForStart()` | `start()` |
| `loop()` | `while (opModeIsActive())` body | `periodic()` |
| `stop()` | code after the active loop | not shown in these samples |

Here is the full lifecycle in one real file,
[DefaultAutoMode.java](src/main/java/first/robot/DefaultAutoMode.java):

```java
@Autonomous
public class DefaultAutoMode extends PeriodicOpMode {
  private final Robot robot;
  private final Timer timer = new Timer();

  public DefaultAutoMode(Robot robot) {   // "init": receive shared objects, create fields
    this.robot = robot;
  }

  @Override
  public void start() {                   // "start": runs once when the mode begins
    timer.reset();
    timer.start();
  }

  @Override
  public void periodic() {                // "loop": runs repeatedly while enabled
    if (timer.get() < 2.0) {
      robot.motor0.setThrottle(0.5);
      robot.motor1.setThrottle(0.5);
    } else if (timer.get() < 4.0) {
      robot.motor0.setThrottle(0.9);
      robot.motor1.setThrottle(0.9);
    } else {
      robot.motor0.setThrottle(0.0);
      robot.motor1.setThrottle(0.0);
    }
  }
}
```

How to think about each piece:

- **The constructor plays the role of `init()`.** In FTC, `init()` was where you
  looked up hardware. Here, hardware already exists inside the `Robot` object, so
  the constructor just stores the references it is handed. Fields with initializers
  (like `new Timer()` or the NetworkTables publishers you'll see later) are also
  created at this stage.
- **`start()` is your `start()`.** Use it for things that must happen at the moment
  the mode begins: resetting timers, zeroing outputs, configuring motor behavior.
  The Kiwi examples use `start()` to call `setFloatOn0(false)` on each drive motor.
- **`periodic()` is your `loop()`.** Read inputs, compute, command hardware,
  publish telemetry. Just like `loop()`, it must finish quickly every time — no
  `while` loops that wait around, no `sleep(...)`.

> [!IMPORTANT]
> If you came from `LinearOpMode`, the biggest habit change is that there is no
> "write the whole match as one long script" style here. `periodic()` is called for
> you over and over. Anything that used to be "wait 2 seconds, then..." becomes a
> timer check inside `periodic()`, exactly like the `DefaultAutoMode` code above.

## 6. Constructor Injection: Why OpModes Receive `Robot`

FTC OpModes never needed constructors. You extended `OpMode` and the SDK filled in
inherited fields like `hardwareMap`, `gamepad1`, and `telemetry` behind the scenes:

```java
public class MyTeleOp extends OpMode {
  private DcMotor motor;   // filled in later, inside init()
}
```

In this project, an OpMode declares what it needs as constructor parameters, and
SystemCore passes those objects in when it creates the OpMode. This pattern is
called **constructor injection**. From
[DefaultTeleMode.java](src/main/java/first/robot/DefaultTeleMode.java):

```java
private final Robot robot;
private final DefaultUserControls userControls;

public DefaultTeleMode(Robot robot, DefaultUserControls userControls) {
  this.robot = robot;
  this.userControls = userControls;
}
```

What each piece means:

- **`Robot robot`** — the shared hardware container from `Robot.java`. Every OpMode
  that touches motors or servos asks for it.
- **`DefaultUserControls userControls`** — the shared gamepad-access object. This is
  the class named in `Robot.java`'s `@UserControlsInstance(DefaultUserControls.class)`
  annotation.
- **`final`** — this OpMode keeps the same references for its whole lifetime.
- **You don't call the constructor yourself.** SystemCore constructs the OpMode and
  supplies the arguments, the same way the FTC SDK used to silently fill
  `hardwareMap` for you.

An OpMode only asks for what it uses. `DefaultAutoMode` takes just `Robot` because
autonomous doesn't read gamepads. The TeleOp modes take both.

Why is this better than inherited fields? The class signature now tells you exactly
what the OpMode depends on. When you open `KiwiDriveExampleTeleMode.java` and see
`(Robot robot, DefaultUserControls userControls)`, you know everything it can touch
without reading the whole file.

## 7. Gamepad Input: `gamepad1` vs `DefaultUserControls`

### What you did in FTC

`gamepad1` and `gamepad2` were inherited fields with public members:

```java
double x = gamepad1.left_stick_x;
double y = -gamepad1.left_stick_y;
boolean slowMode = gamepad1.left_bumper;
```

### What this project does

You ask `userControls` for a gamepad by index, then call getter methods:

```java
var gamepad = userControls.getGamepad(0);

double x = gamepad.getLeftX();
double y = -gamepad.getLeftY();
boolean slowMode = gamepad.getLeftBumperButton();
```

`getGamepad(0)` is your `gamepad1`; `getGamepad(1)` is your `gamepad2`. The fields
became methods, and the names shifted slightly.

| FTC | SystemCore here |
| --- | --- |
| `gamepad1` | `userControls.getGamepad(0)` |
| `gamepad2` | `userControls.getGamepad(1)` |
| `left_stick_x` | `getLeftX()` |
| `left_stick_y` | `getLeftY()` |
| `right_stick_x` | `getRightX()` |
| `right_stick_y` | `getRightY()` |
| `left_trigger` | `getLeftTriggerAxis()` |
| `right_trigger` | `getRightTriggerAxis()` |
| `left_bumper` | `getLeftBumperButton()` |
| `right_bumper` | `getRightBumperButton()` |
| `a` / PlayStation Cross | `getSouthFaceButton()` |

Two familiar FTC habits carry over unchanged:

- **Negating stick Y.** Pushing a stick forward still reads negative, so the
  samples still write `-gamepad.getLeftY()` to make forward positive. Same trick,
  same reason.
- **Reading inputs at the top of the loop.** The Kiwi examples grab
  `var gamepad = userControls.getGamepad(0);` as the first line of `periodic()`,
  just like you read `gamepad1` at the top of `loop()`.

The button naming is worth a second look: `getSouthFaceButton()` describes the
button's *position* (bottom of the face-button diamond) instead of its label. That
way the same code works whether the controller labels it `A` or Cross.

## 8. Motor and Servo Output: `setPower` vs `setThrottle`

FTC:

```java
motor.setPower(0.5);
servo.setPosition(1.0);
```

This project:

```java
robot.motor0.setThrottle(0.5);
robot.servo0.setPosition(1.0);
```

What maps to what:

- **`setThrottle(...)` fills the same basic role as FTC `setPower(...)`.** Positive
  runs the motor one way, negative the other, `0.0` stops it, and the useful range
  is `-1.0` to `1.0`. (They are conceptually similar, but don't assume they are
  identical internally — see the motor-sign note below.)
- **`setPosition(...)` is the same idea you know from FTC servos.** `0.0` to `1.0`
  across the travel range.
- **The `robot.` prefix tells you where the hardware lives.** Writing
  `robot.motor0` instead of `motor0` is a constant reminder that the motor belongs
  to the shared `Robot` object, not to this OpMode.

There is one extra call you'll see in the Kiwi examples' `start()`:

```java
robot.motor0.setFloatOn0(false);
```

This configures what the motor does when throttle is `0.0`. It plays the same role
as FTC's zero-power behavior setup (`setZeroPowerBehavior(...)`): `false` here means
the motor resists motion at zero throttle instead of coasting freely.

### A note about motor direction

In FTC you probably reversed motors with:

```java
motor.setDirection(DcMotorSimple.Direction.REVERSE);
```

The FTC SDK could do this partly because the Robot Configuration told it what kind
of motor was attached. `ExpansionHubMotor.setThrottle()` in these samples is
lower-level — it doesn't know the motor type — so the Kiwi examples handle direction
with **sign constants** instead:

```java
private static final double FRONT_LEFT_MOTOR_SIGN = 1.0;
```

If a wheel spins the wrong way on a real robot, you change that one wheel's sign
constant to `-1.0`. Same goal as `Direction.REVERSE`, different mechanism. More in
the [Kiwi drive case study](#11-kiwi-drive-case-study).

## 9. Autonomous Timing: `ElapsedTime` vs `Timer`

The FTC pattern you know:

```java
ElapsedTime timer = new ElapsedTime();

if (timer.seconds() < 2.0) {
  motor.setPower(0.5);
}
```

The pattern in [DefaultAutoMode.java](src/main/java/first/robot/DefaultAutoMode.java):

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
  }
}
```

The idea — *pick behavior based on how many seconds have elapsed* — is identical.
Only the names changed:

| FTC `ElapsedTime` | SystemCore `Timer` | Meaning |
| --- | --- | --- |
| `new ElapsedTime()` | `new Timer()` | Create the timer |
| `timer.reset()` | `timer.reset()` | Set elapsed time back to zero |
| (starts automatically) | `timer.start()` | Begin counting — you must call this |
| `timer.seconds()` | `timer.get()` | Read elapsed seconds |

One real difference: this `Timer` needs an explicit `timer.start()` after
`timer.reset()`. That's why `DefaultAutoMode.start()` calls both. Resetting and
starting the timer in `start()` (not the constructor) matters, because the
constructor may run well before the match period actually begins — `start()` is the
moment that corresponds to "the mode just started running."

`DefaultAutoMode` chains its time checks into stages, which is exactly the timed
state machine many FTC teams built: drive at `0.5` until 2.0 seconds, `0.9` until
4.0 seconds, then stop.

## 10. Telemetry: `telemetry.addData` vs NetworkTables

### What you did in FTC

```java
telemetry.addData("leftX", leftX);
telemetry.addData("button", pressed);
telemetry.update();
```

Those lines appeared on the Driver Station screen as text.

### What this project does

Telemetry goes through **NetworkTables**: a shared tree of named values that the
robot publishes and dashboard tools display. From
[NetworkTableTelemetryExampleTeleMode.java](src/main/java/first/robot/NetworkTableTelemetryExampleTeleMode.java):

```java
// Created once, as fields:
private final NetworkTable telemetryExampleTable =
    NetworkTableInstance.getDefault().getTable("TelemetryExample");

private final DoublePublisher leftXPublisher =
    telemetryExampleTable.getDoubleTopic("leftX").publish();

// Called every loop, inside periodic():
leftXPublisher.set(leftX);
```

The two-step structure is the key difference from FTC:

1. **Publishers are created once, as fields.** Getting the table and topic
   (`getTable(...)`, `getDoubleTopic(...).publish()`) happens when the OpMode
   object is constructed. This is like deciding your telemetry captions up front.
2. **`.set(value)` is called every loop from `periodic()`.** This is the part that
   corresponds to `telemetry.addData(...)` with a fresh value. There is no
   `telemetry.update()` equivalent — each `.set(...)` updates that value's current
   state in the tree.

Values are typed. The telemetry example uses three publisher types:

| Publisher type | For | Example topic in this project |
| --- | --- | --- |
| `DoublePublisher` | numbers | `leftX`, `leftStickMagnitude` |
| `BooleanPublisher` | true/false | `leftBumperButton`, `leftStickActive` |
| `StringPublisher` | text | `driveDirectionStatus`, `buttonStatus` |

The published values form paths in a tree. The telemetry example creates paths like:

```text
/TelemetryExample/leftX
/TelemetryExample/leftBumperButton
/TelemetryExample/driveDirectionStatus
```

And the mapping back to your FTC mental model:

| FTC telemetry | NetworkTables here |
| --- | --- |
| `telemetry.addData("leftX", leftX)` | `leftXPublisher.set(leftX)` |
| `telemetry.update()` | values update when publishers call `.set(...)` |
| telemetry caption | topic name |
| telemetry screen | OutlineViewer or Elastic |

### Where you see the values

FTC telemetry could only appear on the Driver Station. NetworkTables values can be
viewed by any connected dashboard tool:

- **OutlineViewer** shows the raw tree of tables and values — the quickest way to
  confirm your data is publishing.
- **Elastic** lets you build a dashboard by dragging those values onto a grid.

Setup steps for both are in
[OUTLINEVIEWER_AND_ELASTIC_NETWORKTABLES_README.md](OUTLINEVIEWER_AND_ELASTIC_NETWORKTABLES_README.md).

Both telemetry samples in this project collect their `.set(...)` calls into one
helper method named `publishTelemetry(...)`, called at the end of `periodic()`. That
call is deliberately placed where you would have written `telemetry.update()` — after
the loop's inputs are read and outputs are commanded.

## 11. Kiwi Drive Case Study

[KiwiDriveExampleTeleMode.java](src/main/java/first/robot/KiwiDriveExampleTeleMode.java)
is where all the pieces come together in one realistic drivetrain OpMode. It drives
a three-wheel Kiwi (omni) drivetrain: two angled front wheels and one sideways back
wheel.

The wheel-to-motor layout, from the file's own comments:

```text
                 Front of robot

            motor3                 motor1
       front-left wheel       front-right wheel
         Hub port 3             Hub port 1

                      motor2
                    back wheel
                   Hub port 2

  motor0 / Hub port 0 is unused here.
```

`periodic()` follows six steps every loop. Each one is something you've seen in FTC
drivetrain code:

### Step 1 — Read joystick values

```java
var gamepad = userControls.getGamepad(0);

double x = applyDeadband(gamepad.getLeftX());
double y = applyDeadband(-gamepad.getLeftY());
double rotation = applyDeadband(gamepad.getRightX());
double speedScale = gamepad.getLeftBumperButton() ? PRECISION_SCALE : 1.0;
```

In FTC terms:

- `x` — strafe / sideways command (left stick X)
- `y` — forward/back command (left stick Y, negated so forward is positive)
- `rotation` — turn command (right stick X)
- `speedScale` — slow mode: holding the left bumper scales everything by
  `PRECISION_SCALE` (`0.45`), just like a slow-mode button in FTC

### Step 2 — Apply deadband

```java
private static double applyDeadband(double value) {
  if (Math.abs(value) < DEADBAND) {
    return 0.0;
  }
  return value;
}
```

Any stick reading smaller than `DEADBAND` (`0.05`) becomes `0.0`, so a slightly
off-center stick doesn't creep the robot. Identical to the dead-zone helpers FTC
teams write.

### Step 3 — Calculate wheel powers

```java
double frontLeftPower = (0.5 * x) + (SQRT_3_OVER_2 * y) + rotation;
double frontRightPower = (0.5 * x) - (SQRT_3_OVER_2 * y) + rotation;
double backPower = -x + rotation;
```

This is the Kiwi equivalent of your mecanum `y + x + turn` math. The front wheels
sit at 60 degrees, so their contributions use `cos(60°) = 0.5` and
`sin(60°) = √3/2` (the `SQRT_3_OVER_2` constant). The back wheel is mounted
sideways, so it responds to strafe and rotation but not forward motion.

### Step 4 — Normalize wheel powers

```java
double maxMagnitude =
    Math.max(1.0,
        Math.max(Math.abs(frontLeftPower),
            Math.max(Math.abs(frontRightPower), Math.abs(backPower))));
```

If the math produced any value beyond `±1.0`, every wheel gets divided by the
largest magnitude so the *ratios* stay correct. FTC mecanum code does exactly this
before calling `setPower(...)`.

### Step 5 — Apply motor signs and scaling

```java
double frontLeftThrottle =
    FRONT_LEFT_MOTOR_SIGN * speedScale * frontLeftPower / maxMagnitude;
```

The `*_MOTOR_SIGN` constants are this sample's stand-in for FTC's
`motor.setDirection(DcMotorSimple.Direction.REVERSE)`. All three are `1.0` in the
sample because that matched the tested robot. If a wheel on your robot spins the
wrong way, flip **only that wheel's** sign to `-1.0` — and test at low power first,
as the file's comments insist.

### Step 6 — Send throttle values to hardware

```java
robot.motor0.setThrottle(0.0);            // unused port, explicitly stopped
robot.motor3.setThrottle(frontLeftThrottle);
robot.motor1.setThrottle(frontRightThrottle);
robot.motor2.setThrottle(backThrottle);
```

Note that the motor numbers follow the *wiring* (hub ports), not the math names —
`motor3` is the front-left wheel. This is the kind of mapping FTC students used to
carry in configuration names like `"frontLeft"`; here it's carried by the layout
comment at the top of the file.

### The telemetry variant

[KiwiDriveExampleWithNetworkTableTelemetryTeleMode.java](src/main/java/first/robot/KiwiDriveExampleWithNetworkTableTelemetryTeleMode.java)
is the same drive code plus one addition: after commanding the motors, it publishes
the loop's inputs and outputs to NetworkTables under `/KiwiDriveExample`:

```java
publishTelemetry(x, y, rotation, frontLeftThrottle, frontRightThrottle, backThrottle);
```

Compare the two files side by side and you'll see the telemetry is purely additive —
six publisher fields and one method call at the end of `periodic()`. That's a good
template for adding dashboard data to your own OpModes.

## 12. Side-by-Side Mini Examples

Quick-reference translations of the four things every FTC student does first. The
SystemCore lines are real patterns from this project's samples.

### Read the left stick and drive one motor

FTC:

```java
motor.setPower(-gamepad1.left_stick_y);
```

SystemCore:

```java
robot.motor0.setThrottle(-userControls.getGamepad(0).getLeftY());
```

### Move a servo with a trigger

FTC:

```java
claw.setPosition(gamepad1.right_trigger);
```

SystemCore:

```java
robot.servo1.setPosition(userControls.getGamepad(0).getRightTriggerAxis());
```

### Timed autonomous

FTC:

```java
if (timer.seconds() < 2.0) {
  motor.setPower(0.5);
} else {
  motor.setPower(0.0);
}
```

SystemCore (inside `periodic()`, with the timer reset and started in `start()`):

```java
if (timer.get() < 2.0) {
  robot.motor0.setThrottle(0.5);
} else {
  robot.motor0.setThrottle(0.0);
}
```

### Publish telemetry

FTC:

```java
telemetry.addData("leftX", leftX);
telemetry.update();
```

SystemCore (publisher created once as a field, then every loop):

```java
leftXPublisher.set(leftX);
```

## 13. Common FTC Assumptions That Change

These are the habits most likely to trip you up. Skim this table when something
"should work" but doesn't.

| FTC assumption | What changes here |
| --- | --- |
| Every OpMode does its own `hardwareMap` setup | Hardware is centralized in `Robot.java`; OpModes receive the `Robot` object |
| `gamepad1` is an inherited field | Gamepad access comes from `DefaultUserControls` via `getGamepad(0)` |
| `loop()` is the main repeated method | `periodic()` is the repeated method |
| `setPower(...)` commands motors | `setThrottle(...)` commands motors |
| `telemetry.update()` refreshes the Driver Station display | NetworkTables publishers update dashboard values with `.set(...)` |
| Motor names come from the FTC Robot Configuration | Motors are created by hub and port number in code |
| `@TeleOp` (capital O) | `@Teleop` (lowercase o) |
| `ElapsedTime` starts counting on its own | `Timer` needs `reset()` **and** `start()` |
| `motor.setDirection(REVERSE)` flips a motor | These samples flip a per-wheel sign constant instead |
| OpModes never have constructors | OpModes declare what they need as constructor parameters |

## 14. Student Reading Order

Read the samples in this order — each file adds one idea to the previous one:

1. [Robot.java](src/main/java/first/robot/Robot.java) — where hardware is declared.
2. [DefaultTeleMode.java](src/main/java/first/robot/DefaultTeleMode.java) — the
   simplest gamepad-to-hardware loop.
3. [DefaultAutoMode.java](src/main/java/first/robot/DefaultAutoMode.java) — timed
   autonomous with `start()` and `Timer`.
4. [KiwiDriveExampleTeleMode.java](src/main/java/first/robot/KiwiDriveExampleTeleMode.java)
   — real drivetrain math.
5. [NetworkTableTelemetryExampleTeleMode.java](src/main/java/first/robot/NetworkTableTelemetryExampleTeleMode.java)
   — telemetry by itself, no driving.
6. [KiwiDriveExampleWithNetworkTableTelemetryTeleMode.java](src/main/java/first/robot/KiwiDriveExampleWithNetworkTableTelemetryTeleMode.java)
   — drive code and telemetry combined.

## 15. Glossary

Skimmable definitions of every project-specific name used in this guide.

| Term | Quick definition |
| --- | --- |
| `OpModeRobot` | Base class that `Robot` extends; makes `Robot` the shared robot container SystemCore constructs and hands to OpModes |
| `Robot` | This project's hardware container: four `ExpansionHubMotor`s and two `ExpansionHubServo`s as `public final` fields |
| `PeriodicOpMode` | Base class for OpModes here; closest to FTC iterative `OpMode`, with `start()` and a repeatedly-called `periodic()` |
| `@Teleop` | Registers a class as a driver-controlled mode (FTC: `@TeleOp` — note the spelling difference) |
| `@Autonomous` | Registers a class as an autonomous mode (same name as FTC) |
| `@UserControlsInstance` | Annotation on `Robot` naming which user-controls class SystemCore should provide to OpModes |
| `DefaultUserControls` | The gamepad-access object; `getGamepad(0)` is your `gamepad1`, `getGamepad(1)` is your `gamepad2` |
| `ExpansionHubMotor` | A motor on an Expansion Hub port, e.g. `new ExpansionHubMotor(0, 1)` = hub 0, port 1 (FTC: `DcMotor`) |
| `ExpansionHubServo` | A servo on an Expansion Hub servo port (FTC: `Servo`) |
| `setThrottle` | Commands motor output from `-1.0` to `1.0` (FTC: `setPower`) |
| `setFloatOn0` | Configures motor behavior at zero throttle; `false` = resist motion (FTC: zero-power behavior) |
| `setPosition` | Moves a servo across `0.0`–`1.0`, same as FTC |
| `Timer` | Elapsed-seconds timer (FTC: `ElapsedTime`); needs `reset()` and `start()`, read with `get()` |
| `NetworkTable` | A named group of values in the NetworkTables tree, e.g. `getTable("TelemetryExample")` |
| `DoublePublisher` | Publishes a numeric value to a NetworkTables topic |
| `BooleanPublisher` | Publishes a true/false value to a NetworkTables topic |
| `StringPublisher` | Publishes a text value to a NetworkTables topic |
| `publishTelemetry` | Helper method in these samples that calls `.set(...)` on every publisher; sits where `telemetry.update()` would in FTC |
| `periodic` | The repeated OpMode method (FTC: `loop()`); reads inputs, commands hardware, publishes telemetry |
| `start` | Runs once when the mode begins (FTC: `start()`, or the moment after `waitForStart()`) |
| deadband | Treating tiny stick values as zero so a resting stick doesn't move the robot (`DEADBAND = 0.05` here) |
| normalization | Dividing all wheel powers by the largest magnitude so none exceed `1.0` while keeping their ratios |
| motor sign | A `1.0` / `-1.0` constant that flips one wheel's direction (this project's stand-in for FTC's `Direction.REVERSE`) |
| OutlineViewer | WPILib tool that shows the raw NetworkTables tree — quickest check that values are publishing |
| Elastic | Dashboard tool for arranging NetworkTables values as widgets |
