# Claude/Fable Brief: Ultimate FTC Student Guide to SystemCore

## Purpose

Use `FTC_TO_SYSTEMCORE_STRUCTURE_README.md` as the starting point and expand it into
an ultimate transition guide for FTC students learning SystemCore.

The final guide should help an FTC student who already understands FTC OpModes,
`hardwareMap`, `gamepad1`, motors, servos, telemetry, and basic autonomous timing
understand how those same ideas appear in the SystemCore / WPILib-style project
structure used here.

The guide should not just define terms. It should repeatedly show:

1. What the student probably did in the FTC SDK.
2. Where that concept usually lived in FTC code.
3. What the equivalent structure is in this SystemCore project.
4. Why the SystemCore version is organized that way.
5. A small side-by-side code comparison.

## Source Material

Use these files as source material:

```text
README.md
FTC_TO_SYSTEMCORE_STRUCTURE_README.md
OUTLINEVIEWER_AND_ELASTIC_NETWORKTABLES_README.md
src/main/java/first/robot/Robot.java
src/main/java/first/robot/DefaultTeleMode.java
src/main/java/first/robot/DefaultAutoMode.java
src/main/java/first/robot/KiwiDriveExampleTeleMode.java
src/main/java/first/robot/KiwiDriveExampleWithNetworkTableTelemetryTeleMode.java
src/main/java/first/robot/NetworkTableTelemetryExampleTeleMode.java
```

Do not invent APIs that are not present in the samples unless clearly labeled as
conceptual background. Prefer the exact names used by this project.

## Intended Reader

The reader is an FTC student or mentor who may be comfortable with code like:

```java
@TeleOp
public class MyTeleOp extends OpMode {
  private DcMotor leftMotor;

  @Override
  public void init() {
    leftMotor = hardwareMap.get(DcMotor.class, "leftMotor");
  }

  @Override
  public void loop() {
    leftMotor.setPower(-gamepad1.left_stick_y);
    telemetry.addData("left power", -gamepad1.left_stick_y);
    telemetry.update();
  }
}
```

But the reader may be new to:

- `Robot extends OpModeRobot`
- constructor injection
- `PeriodicOpMode`
- `DefaultUserControls`
- `ExpansionHubMotor`
- `setThrottle(...)`
- NetworkTables publishers
- WPILib-style dashboard tools
- separating robot hardware structure from individual OpModes

## Tone and Teaching Style

Write for a smart student who knows FTC but is seeing this structure for the first
time.

Use plain language. Avoid sounding like an API reference. The guide should feel like
a mentor walking through the project and saying, "You know this FTC thing? Here is
where it went in SystemCore."

Good style:

- "In FTC, you probably saw this inside `init()`."
- "In this project, that responsibility moved into `Robot.java`."
- "The idea is the same, but the object that owns the hardware is different."
- "This is closest to FTC iterative `loop()`, not `LinearOpMode` by itself."

Avoid:

- assuming the student already knows FRC command-based structure
- overexplaining Java basics unless directly relevant
- turning the guide into a generic WPILib manual
- hiding the differences behind vague phrases like "similar architecture"

## Recommended Final File

Create or draft the expanded guide as:

```text
FTC_STUDENT_ULTIMATE_SYSTEMCORE_GUIDE.md
```

## Suggested Structure

### 1. Title

Suggested title:

```markdown
# FTC Student Ultimate Guide to SystemCore Structure
```

Subtitle idea:

```markdown
How familiar FTC OpMode ideas map to this FRC-style SystemCore project.
```

### 2. Quick Orientation

Explain that FTC code often puts hardware setup, driver input, robot behavior, and
telemetry inside one OpMode class, while this project separates those responsibilities.

Include a compact mental model:

| FTC SDK Habit | SystemCore Project Habit |
| --- | --- |
| OpMode owns hardware fields directly | `Robot.java` owns hardware fields |
| `hardwareMap.get(...)` in `init()` | `new ExpansionHubMotor(hub, port)` in `Robot.java` |
| `gamepad1` inherited by OpMode | `userControls.getGamepad(0)` |
| `loop()` | `periodic()` |
| `motor.setPower(...)` | `robot.motorX.setThrottle(...)` |
| `telemetry.addData(...)` | NetworkTables publisher `.set(...)` |

### 3. Project Map

Show the project files and what role each file plays.

Example:

| File | FTC mental equivalent | Role in this project |
| --- | --- | --- |
| `Robot.java` | Hardware class or repeated `hardwareMap` setup | Defines motors and servos |
| `DefaultTeleMode.java` | Simple `@TeleOp` | Direct gamepad-to-hardware test |
| `DefaultAutoMode.java` | Simple `@Autonomous` | Timed autonomous |
| `KiwiDriveExampleTeleMode.java` | Drivetrain TeleOp | Kiwi drive math without telemetry |
| `NetworkTableTelemetryExampleTeleMode.java` | Telemetry test OpMode | Publishes sample values |
| `KiwiDriveExampleWithNetworkTableTelemetryTeleMode.java` | Drive OpMode plus telemetry | Combines drivetrain and dashboard data |

### 4. Hardware Setup: `hardwareMap` vs `Robot.java`

Explain FTC hardware setup first.

FTC example:

```java
private DcMotor leftMotor;
private Servo claw;

@Override
public void init() {
  leftMotor = hardwareMap.get(DcMotor.class, "leftMotor");
  claw = hardwareMap.get(Servo.class, "claw");
}
```

SystemCore example:

```java
public class Robot extends OpModeRobot {
  public final ExpansionHubMotor motor1 = new ExpansionHubMotor(0, 1);
  public final ExpansionHubServo servo0 = new ExpansionHubServo(0, 0);
}
```

Comparison points:

- FTC usually maps by configured device name.
- This project creates hardware objects by hub number and port number.
- FTC hardware fields are often private fields inside each OpMode.
- SystemCore hardware fields live in one shared `Robot` object.
- OpModes access hardware through `robot.motor1`, `robot.servo0`, etc.

Include a table:

| FTC | SystemCore here | Student translation |
| --- | --- | --- |
| `DcMotor` | `ExpansionHubMotor` | Motor object |
| `Servo` | `ExpansionHubServo` | Servo object |
| `hardwareMap.get(...)` | `new ExpansionHubMotor(0, port)` | Connect code to hub hardware |
| configured device name | hub and port number | How the hardware is identified |

### 5. OpMode Registration: `@TeleOp` / `@Autonomous`

Compare annotations:

FTC:

```java
@TeleOp(name = "My TeleOp")
public class MyTeleOp extends OpMode {
}
```

SystemCore:

```java
@Teleop(name = "Kiwi Drive Example")
public class KiwiDriveExampleTeleMode extends PeriodicOpMode {
}
```

Discuss:

- FTC uses `@TeleOp`; this project uses `@Teleop`.
- FTC and SystemCore both use annotations to put modes in the driver station list.
- `@Autonomous` serves the same broad purpose in both.

### 6. Lifecycle: `init/start/loop` and `runOpMode` vs `start/periodic`

This should be one of the most important sections.

Include a side-by-side table:

| FTC Iterative OpMode | FTC LinearOpMode | SystemCore here |
| --- | --- | --- |
| `init()` | code before `waitForStart()` | constructor plus `Robot.java` setup |
| `start()` | code immediately after `waitForStart()` | `start()` |
| `loop()` | `while (opModeIsActive())` body | `periodic()` |
| `stop()` | code after active loop | not shown in these samples |

Explain:

- `periodic()` is the main repeated loop.
- `start()` is for setup that should happen when the OpMode begins.
- Constructors receive shared objects instead of doing FTC-style inherited field access.

### 7. Constructor Injection: Why OpModes Receive `Robot`

FTC students may not expect constructors in OpModes.

FTC style:

```java
public class MyTeleOp extends OpMode {
  private DcMotor motor;
}
```

SystemCore style:

```java
private final Robot robot;
private final DefaultUserControls userControls;

public DefaultTeleMode(Robot robot, DefaultUserControls userControls) {
  this.robot = robot;
  this.userControls = userControls;
}
```

Explain:

- `Robot` is the shared hardware object.
- `DefaultUserControls` is the shared input object.
- `final` means this OpMode keeps the same references for its lifetime.
- This makes dependencies explicit: the class shows what it needs to run.

### 8. Gamepad Input: `gamepad1` vs `DefaultUserControls`

FTC:

```java
double x = gamepad1.left_stick_x;
double y = -gamepad1.left_stick_y;
boolean slowMode = gamepad1.left_bumper;
```

SystemCore:

```java
var gamepad = userControls.getGamepad(0);

double x = gamepad.getLeftX();
double y = -gamepad.getLeftY();
boolean slowMode = gamepad.getLeftBumperButton();
```

Include mapping table:

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
| `a` / Cross | `getSouthFaceButton()` |

### 9. Motor and Servo Output: `setPower` vs `setThrottle`

FTC:

```java
motor.setPower(0.5);
servo.setPosition(1.0);
```

SystemCore:

```java
robot.motor0.setThrottle(0.5);
robot.servo0.setPosition(1.0);
```

Explain:

- `setThrottle(...)` fills the same basic role as FTC `setPower(...)`.
- `setPosition(...)` is familiar for servos.
- `robot.motor0` means the motor came from `Robot.java`.

Include note about sign:

- In FTC, students often reverse motors with `motor.setDirection(...)`.
- In these Kiwi examples, wheel direction is handled with sign constants.

### 10. Autonomous Timing: `ElapsedTime` vs `Timer`

FTC:

```java
ElapsedTime timer = new ElapsedTime();

if (timer.seconds() < 2.0) {
  motor.setPower(0.5);
}
```

SystemCore:

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

Explain that the timed-state idea is the same even though the class name and method
names differ.

### 11. Telemetry: `telemetry.addData` vs NetworkTables

FTC:

```java
telemetry.addData("leftX", leftX);
telemetry.addData("button", pressed);
telemetry.update();
```

SystemCore:

```java
private final NetworkTable telemetryExampleTable =
    NetworkTableInstance.getDefault().getTable("TelemetryExample");

private final DoublePublisher leftXPublisher =
    telemetryExampleTable.getDoubleTopic("leftX").publish();

leftXPublisher.set(leftX);
```

Explain:

- FTC telemetry is often shown as lines on the Driver Station.
- NetworkTables publishes named values to a shared data tree.
- OutlineViewer shows raw tables and values.
- Elastic can show those values in a dashboard.
- Publishers are created once as fields.
- `.set(...)` is called repeatedly from `periodic()`.

Include mapping:

| FTC telemetry | NetworkTables here |
| --- | --- |
| `telemetry.addData("leftX", leftX)` | `leftXPublisher.set(leftX)` |
| `telemetry.update()` | values update when publishers call `.set(...)` |
| telemetry caption | topic name |
| telemetry screen | OutlineViewer or Elastic |

### 12. Kiwi Drive Case Study

Use `KiwiDriveExampleTeleMode.java` as a case study for how a real FTC drivetrain
example translates.

Structure:

1. Read joystick values.
2. Apply deadband.
3. Calculate wheel powers.
4. Normalize wheel powers.
5. Apply motor signs.
6. Send throttle values to hardware.

Show the important SystemCore code:

```java
double x = applyDeadband(gamepad.getLeftX());
double y = applyDeadband(-gamepad.getLeftY());
double rotation = applyDeadband(gamepad.getRightX());
double speedScale = gamepad.getLeftBumperButton() ? PRECISION_SCALE : 1.0;
```

Then explain each value in FTC terms:

- `x`: strafe or sideways command
- `y`: forward/back command
- `rotation`: turn command
- `speedScale`: slow mode

Explain motor signs:

```java
private static final double FRONT_LEFT_MOTOR_SIGN = 1.0;
```

This is the sample's current stand-in for FTC-style motor direction reversal.

### 13. Side-by-Side Mini Examples

Include short examples that compare the same task in FTC and SystemCore.

Suggested examples:

#### Read Left Stick and Drive One Motor

FTC:

```java
motor.setPower(-gamepad1.left_stick_y);
```

SystemCore:

```java
robot.motor0.setThrottle(-userControls.getGamepad(0).getLeftY());
```

#### Move a Servo with a Trigger

FTC:

```java
claw.setPosition(gamepad1.right_trigger);
```

SystemCore:

```java
robot.servo1.setPosition(userControls.getGamepad(0).getRightTriggerAxis());
```

#### Timed Autonomous

FTC:

```java
if (timer.seconds() < 2.0) {
  motor.setPower(0.5);
} else {
  motor.setPower(0.0);
}
```

SystemCore:

```java
if (timer.get() < 2.0) {
  robot.motor0.setThrottle(0.5);
} else {
  robot.motor0.setThrottle(0.0);
}
```

#### Publish Telemetry

FTC:

```java
telemetry.addData("leftX", leftX);
telemetry.update();
```

SystemCore:

```java
leftXPublisher.set(leftX);
```

### 14. Common FTC Assumptions That Change

Make a section that explicitly names habits students may bring from FTC.

| FTC assumption | What changes here |
| --- | --- |
| Every OpMode does its own `hardwareMap` setup | Hardware is centralized in `Robot.java` |
| `gamepad1` is an inherited field | Gamepad access comes from `DefaultUserControls` |
| `loop()` is the main repeated method | `periodic()` is the repeated method |
| `setPower(...)` commands motors | `setThrottle(...)` commands motors |
| `telemetry.update()` refreshes the Driver Station display | NetworkTables publishers update dashboard values |
| Motor names come from the FTC Robot Configuration | Motors are created by hub and port in code |

### 15. Glossary

End with a glossary students can skim.

Include at least:

- `OpModeRobot`
- `Robot`
- `PeriodicOpMode`
- `@Teleop`
- `@Autonomous`
- `DefaultUserControls`
- `ExpansionHubMotor`
- `ExpansionHubServo`
- `setThrottle`
- `setPosition`
- `Timer`
- `NetworkTable`
- `DoublePublisher`
- `BooleanPublisher`
- `StringPublisher`
- `publishTelemetry`
- `periodic`
- `start`
- `deadband`
- `normalization`
- `motor sign`

## Important Accuracy Notes

Keep these distinctions clear:

- FTC `@TeleOp` and this project's `@Teleop` are spelled differently.
- FTC `DcMotor.setPower(...)` and SystemCore `ExpansionHubMotor.setThrottle(...)`
  are conceptually similar, but do not imply they are identical internally.
- FTC `hardwareMap` uses configured device names; this project creates Expansion Hub
  objects with hub and port numbers.
- FTC telemetry and NetworkTables solve a similar student need, but NetworkTables is
  a shared table/topic system, not just a Driver Station text log.
- This project uses `PeriodicOpMode`, so compare it first to FTC iterative OpMode.
  Mention LinearOpMode only as a familiar alternative.

## Output Expectations

The finished guide should be:

- written as Markdown
- structured with clear headings
- table-heavy where side-by-side comparison helps
- code-heavy enough to be concrete, but not a wall of code
- focused on the samples in this repo
- approachable for FTC students
- useful as a pull-request documentation artifact

Prefer short code snippets over full file listings.

Do not remove the smaller `FTC_TO_SYSTEMCORE_STRUCTURE_README.md`; the new guide can
expand beyond it.
