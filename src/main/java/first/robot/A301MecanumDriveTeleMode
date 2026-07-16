// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot;

import com.revrobotics.spark.A301;
import com.revrobotics.util.Signal;
import org.wpilib.driverstation.DefaultUserControls;
import org.wpilib.networktables.BooleanPublisher;
import org.wpilib.networktables.DoublePublisher;
import org.wpilib.networktables.NetworkTable;
import org.wpilib.networktables.NetworkTableInstance;
import org.wpilib.networktables.StringPublisher;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Teleop;

@Teleop(name = "A301 Mecanum Drive")
public class A301MecanumDriveTeleMode extends PeriodicOpMode {
  private static final int FRONT_LEFT_BUS = 5; // Motioncore D0
  private static final int FRONT_RIGHT_BUS = 6; // Motioncore D1
  private static final int BACK_RIGHT_BUS = 7; // Motioncore D2
  private static final int BACK_LEFT_BUS = 8; // Motioncore D3
  private static final int A301_CAN_ID = 3;

  private static final double DEADBAND = 0.05;
  private static final double PRECISION_SCALE = 0.35;

  /*
   * The direct all-motor test already used -gamepad.getRightY(), so pushing the
   * stick forward produced a positive throttle. You saw the left side move
   * forward from that positive command, so the right side is inverted here.
   */
  private static final double FRONT_LEFT_SIGN = 1.0;
  private static final double FRONT_RIGHT_SIGN = -1.0;
  private static final double BACK_RIGHT_SIGN = -1.0;
  private static final double BACK_LEFT_SIGN = 1.0;

  private final DefaultUserControls userControls;

  private final NetworkTable table =
      NetworkTableInstance.getDefault().getTable("A301MecanumDrive");
  private final DoublePublisher loopCountPublisher =
      table.getDoubleTopic("loopCount").publish();
  private final DoublePublisher startCountPublisher =
      table.getDoubleTopic("startCount").publish();
  private final DoublePublisher endCountPublisher =
      table.getDoubleTopic("endCount").publish();
  private final DoublePublisher xInputPublisher =
      table.getDoubleTopic("xInput").publish();
  private final DoublePublisher yInputPublisher =
      table.getDoubleTopic("yInput").publish();
  private final DoublePublisher rotationInputPublisher =
      table.getDoubleTopic("rotationInput").publish();
  private final DoublePublisher speedScalePublisher =
      table.getDoubleTopic("speedScale").publish();
  private final DoublePublisher frontLeftCommandPublisher =
      table.getDoubleTopic("frontLeftCommand").publish();
  private final DoublePublisher frontRightCommandPublisher =
      table.getDoubleTopic("frontRightCommand").publish();
  private final DoublePublisher backRightCommandPublisher =
      table.getDoubleTopic("backRightCommand").publish();
  private final DoublePublisher backLeftCommandPublisher =
      table.getDoubleTopic("backLeftCommand").publish();
  private final BooleanPublisher precisionModePublisher =
      table.getBooleanTopic("precisionMode").publish();
  private final StringPublisher lifecyclePublisher =
      table.getStringTopic("lifecycle").publish();
  private final StringPublisher statusPublisher =
      table.getStringTopic("status").publish();

  private final MotorTelemetry frontLeftTelemetry = new MotorTelemetry("frontLeft");
  private final MotorTelemetry frontRightTelemetry = new MotorTelemetry("frontRight");
  private final MotorTelemetry backRightTelemetry = new MotorTelemetry("backRight");
  private final MotorTelemetry backLeftTelemetry = new MotorTelemetry("backLeft");

  private A301 frontLeft;
  private A301 frontRight;
  private A301 backRight;
  private A301 backLeft;

  private int loopCount;
  private int startCount;
  private int endCount;

  public A301MecanumDriveTeleMode(DefaultUserControls userControls) {
    System.out.println("A301 mecanum constructor called");
    this.userControls = userControls;
  }

  @Override
  public void start() {
    System.out.println("A301 MECANUM START");
    loopCount = 0;
    startCount++;
    startCountPublisher.set(startCount);
    lifecyclePublisher.set("start() called; creating motor handles");

    closeAllMotors();
    createAllMotors();
    stopAllMotors();
    publishAllMotorTelemetry();
  }

  @Override
  public void periodic() {
    loopCount++;
    loopCountPublisher.set(loopCount);

    var gamepad = userControls.getGamepad(0);

    double x = applyDeadband(gamepad.getLeftX());
    double y = applyDeadband(-gamepad.getLeftY());
    double rotation = applyDeadband(gamepad.getRightX());
    double speedScale = gamepad.getLeftBumperButton() ? PRECISION_SCALE : 1.0;

    double frontLeftPower = y + x + rotation;
    double frontRightPower = y - x - rotation;
    double backRightPower = y + x - rotation;
    double backLeftPower = y - x + rotation;

    double maxMagnitude =
        Math.max(
            1.0,
            Math.max(
                Math.abs(frontLeftPower),
                Math.max(
                    Math.abs(frontRightPower),
                    Math.max(Math.abs(backRightPower), Math.abs(backLeftPower)))));

    double frontLeftCommand =
        FRONT_LEFT_SIGN * speedScale * frontLeftPower / maxMagnitude;
    double frontRightCommand =
        FRONT_RIGHT_SIGN * speedScale * frontRightPower / maxMagnitude;
    double backRightCommand =
        BACK_RIGHT_SIGN * speedScale * backRightPower / maxMagnitude;
    double backLeftCommand =
        BACK_LEFT_SIGN * speedScale * backLeftPower / maxMagnitude;

    if (hasAnyNonzeroCommand(
        frontLeftCommand, frontRightCommand, backRightCommand, backLeftCommand)) {
      if (anyMotorMissing()) {
        lifecyclePublisher.set("command requested after handles were closed; recreating motors");
        createAllMotors();
      }

      setMotor("frontLeft", frontLeft, frontLeftTelemetry, frontLeftCommand);
      setMotor("frontRight", frontRight, frontRightTelemetry, frontRightCommand);
      setMotor("backRight", backRight, backRightTelemetry, backRightCommand);
      setMotor("backLeft", backLeft, backLeftTelemetry, backLeftCommand);
    } else if (!anyMotorMissing()) {
      stopAllMotors();
    }

    publishDriveTelemetry(
        x,
        y,
        rotation,
        speedScale,
        gamepad.getLeftBumperButton(),
        frontLeftCommand,
        frontRightCommand,
        backRightCommand,
        backLeftCommand);
    publishAllMotorTelemetry();
    publishSummaryStatus();
  }

  @Override
  public void end() {
    endCount++;
    endCountPublisher.set(endCount);
    lifecyclePublisher.set("end() called; stopping and closing motor handles");
    stopAllMotors();
    closeAllMotors();
    zeroCommandTelemetry();
  }

  @Override
  public void close() {
    lifecyclePublisher.set("close() called; stopping and closing motor handles");
    stopAllMotors();
    closeAllMotors();
    zeroCommandTelemetry();
  }

  private void createAllMotors() {
    frontLeft = createMotor("frontLeft", "D0", FRONT_LEFT_BUS, frontLeftTelemetry);
    frontRight = createMotor("frontRight", "D1", FRONT_RIGHT_BUS, frontRightTelemetry);
    backRight = createMotor("backRight", "D2", BACK_RIGHT_BUS, backRightTelemetry);
    backLeft = createMotor("backLeft", "D3", BACK_LEFT_BUS, backLeftTelemetry);
  }

  private A301 createMotor(
      String name, String portName, int busId, MotorTelemetry telemetry) {
    System.out.println("Creating " + name + " on Motioncore " + portName + " bus " + busId);
    telemetry.portPublisher.set(portName);
    telemetry.busPublisher.set(busId);
    telemetry.connectedPublisher.set(false);
    telemetry.commandAcceptedPublisher.set(false);
    telemetry.respondingPublisher.set(false);
    telemetry.lastErrorPublisher.set("not created yet");

    try {
      A301 motor = new A301(busId, A301_CAN_ID);
      System.out.println(name + " CREATED");

      String firmware = motor.getFirmwareString();
      System.out.println(name + " FW: " + firmware);

      motor.setThrottle(0.0);
      telemetry.connectedPublisher.set(true);
      telemetry.commandAcceptedPublisher.set(true);
      telemetry.firmwarePublisher.set(firmware);
      telemetry.lastErrorPublisher.set("");
      return motor;
    } catch (Throwable t) {
      System.out.println(name + " FAILED");
      t.printStackTrace();

      telemetry.connectedPublisher.set(false);
      telemetry.commandAcceptedPublisher.set(false);
      telemetry.respondingPublisher.set(false);
      telemetry.firmwarePublisher.set("");
      telemetry.lastErrorPublisher.set(getThrowableText(t));
      return null;
    }
  }

  private boolean anyMotorMissing() {
    return frontLeft == null || frontRight == null || backRight == null || backLeft == null;
  }

  private void stopAllMotors() {
    setMotor("frontLeft", frontLeft, frontLeftTelemetry, 0.0);
    setMotor("frontRight", frontRight, frontRightTelemetry, 0.0);
    setMotor("backRight", backRight, backRightTelemetry, 0.0);
    setMotor("backLeft", backLeft, backLeftTelemetry, 0.0);
  }

  private void setMotor(String name, A301 motor, MotorTelemetry telemetry, double throttle) {
    telemetry.commandPublisher.set(throttle);

    if (motor == null) {
      telemetry.connectedPublisher.set(false);
      telemetry.commandAcceptedPublisher.set(false);
      telemetry.respondingPublisher.set(false);
      telemetry.lastErrorPublisher.set("motor object is null");
      return;
    }

    try {
      motor.setThrottle(throttle);
      telemetry.connectedPublisher.set(true);
      telemetry.commandAcceptedPublisher.set(true);
      telemetry.lastErrorPublisher.set("");
    } catch (Throwable t) {
      System.out.println(name + " setThrottle FAILED");
      t.printStackTrace();

      telemetry.commandAcceptedPublisher.set(false);
      telemetry.respondingPublisher.set(false);
      telemetry.lastErrorPublisher.set("setThrottle failed: " + getThrowableText(t));
    }
  }

  private void publishDriveTelemetry(
      double x,
      double y,
      double rotation,
      double speedScale,
      boolean precisionMode,
      double frontLeftCommand,
      double frontRightCommand,
      double backRightCommand,
      double backLeftCommand) {
    xInputPublisher.set(x);
    yInputPublisher.set(y);
    rotationInputPublisher.set(rotation);
    speedScalePublisher.set(speedScale);
    precisionModePublisher.set(precisionMode);
    frontLeftCommandPublisher.set(frontLeftCommand);
    frontRightCommandPublisher.set(frontRightCommand);
    backRightCommandPublisher.set(backRightCommand);
    backLeftCommandPublisher.set(backLeftCommand);
  }

  private void publishAllMotorTelemetry() {
    publishMotorTelemetry("frontLeft", frontLeft, frontLeftTelemetry);
    publishMotorTelemetry("frontRight", frontRight, frontRightTelemetry);
    publishMotorTelemetry("backRight", backRight, backRightTelemetry);
    publishMotorTelemetry("backLeft", backLeft, backLeftTelemetry);
  }

  private void publishMotorTelemetry(String name, A301 motor, MotorTelemetry telemetry) {
    if (motor == null) {
      telemetry.connectedPublisher.set(false);
      telemetry.commandAcceptedPublisher.set(false);
      telemetry.respondingPublisher.set(false);
      return;
    }

    telemetry.connectedPublisher.set(true);
    telemetry.respondingPublisher.set(false);
    readDoubleSignal(
        name,
        "busVoltage",
        () -> motor.getBusVoltage(),
        telemetry.busVoltagePublisher,
        telemetry.busVoltageValidPublisher,
        telemetry.busVoltageErrorPublisher,
        telemetry);
    readDoubleSignal(
        name,
        "appliedOutput",
        () -> motor.getAppliedOutput(),
        telemetry.appliedOutputPublisher,
        telemetry.appliedOutputValidPublisher,
        telemetry.appliedOutputErrorPublisher,
        telemetry);
    readDoubleSignal(
        name,
        "motorCurrent",
        () -> motor.getMotorCurrent(),
        telemetry.motorCurrentPublisher,
        telemetry.motorCurrentValidPublisher,
        telemetry.motorCurrentErrorPublisher,
        telemetry);
    readDoubleSignal(
        name,
        "encoderVelocity",
        () -> motor.getEncoderVelocity(),
        telemetry.encoderVelocityPublisher,
        telemetry.encoderVelocityValidPublisher,
        telemetry.encoderVelocityErrorPublisher,
        telemetry);
  }

  private void readDoubleSignal(
      String name,
      String signalName,
      DoubleSignalSupplier signalSupplier,
      DoublePublisher valuePublisher,
      BooleanPublisher validPublisher,
      StringPublisher errorPublisher,
      MotorTelemetry telemetry) {
    try {
      Signal<Double> signal = signalSupplier.get();
      double value = signal.get(0.0);
      boolean valid = signal.isValid();
      String error = String.valueOf(signal.getError());

      valuePublisher.set(value);
      validPublisher.set(valid);
      errorPublisher.set(error);

      if (valid) {
        telemetry.respondingPublisher.set(true);
      }
    } catch (Throwable t) {
      System.out.println(name + " " + signalName + " read FAILED");
      t.printStackTrace();

      validPublisher.set(false);
      errorPublisher.set(getThrowableText(t));
      telemetry.respondingPublisher.set(false);
      telemetry.lastErrorPublisher.set(signalName + " read failed: " + getThrowableText(t));
    }
  }

  private void publishSummaryStatus() {
    statusPublisher.set(
        "loops="
            + loopCount
            + " FL="
            + statusText(frontLeft)
            + " FR="
            + statusText(frontRight)
            + " BR="
            + statusText(backRight)
            + " BL="
            + statusText(backLeft));
  }

  private String statusText(A301 motor) {
    if (motor == null) {
      return "null";
    }

    return "created";
  }

  private void zeroCommandTelemetry() {
    frontLeftCommandPublisher.set(0.0);
    frontRightCommandPublisher.set(0.0);
    backRightCommandPublisher.set(0.0);
    backLeftCommandPublisher.set(0.0);
  }

  private void closeMotor(String name, A301 motor, MotorTelemetry telemetry) {
    if (motor == null) {
      telemetry.connectedPublisher.set(false);
      telemetry.commandAcceptedPublisher.set(false);
      telemetry.respondingPublisher.set(false);
      return;
    }

    try {
      motor.close();
      telemetry.connectedPublisher.set(false);
      telemetry.commandAcceptedPublisher.set(false);
      telemetry.respondingPublisher.set(false);
    } catch (Throwable t) {
      System.out.println(name + " close FAILED");
      t.printStackTrace();

      telemetry.lastErrorPublisher.set("close failed: " + getThrowableText(t));
    }
  }

  private void closeAllMotors() {
    closeMotor("frontLeft", frontLeft, frontLeftTelemetry);
    closeMotor("frontRight", frontRight, frontRightTelemetry);
    closeMotor("backRight", backRight, backRightTelemetry);
    closeMotor("backLeft", backLeft, backLeftTelemetry);

    frontLeft = null;
    frontRight = null;
    backRight = null;
    backLeft = null;
  }

  private static double applyDeadband(double value) {
    if (Math.abs(value) < DEADBAND) {
      return 0.0;
    }

    return value;
  }

  private static boolean hasAnyNonzeroCommand(
      double frontLeftCommand,
      double frontRightCommand,
      double backRightCommand,
      double backLeftCommand) {
    return Math.abs(frontLeftCommand) >= DEADBAND
        || Math.abs(frontRightCommand) >= DEADBAND
        || Math.abs(backRightCommand) >= DEADBAND
        || Math.abs(backLeftCommand) >= DEADBAND;
  }

  private String getThrowableText(Throwable t) {
    String message = t.getMessage();
    if (message == null || message.isBlank()) {
      return t.getClass().getName();
    }

    return t.getClass().getName() + ": " + message;
  }

  @FunctionalInterface
  private interface DoubleSignalSupplier {
    Signal<Double> get();
  }

  private final class MotorTelemetry {
    private final StringPublisher portPublisher;
    private final DoublePublisher busPublisher;
    private final DoublePublisher commandPublisher;
    private final BooleanPublisher connectedPublisher;
    private final BooleanPublisher commandAcceptedPublisher;
    private final BooleanPublisher respondingPublisher;
    private final StringPublisher firmwarePublisher;
    private final StringPublisher lastErrorPublisher;
    private final DoublePublisher busVoltagePublisher;
    private final BooleanPublisher busVoltageValidPublisher;
    private final StringPublisher busVoltageErrorPublisher;
    private final DoublePublisher appliedOutputPublisher;
    private final BooleanPublisher appliedOutputValidPublisher;
    private final StringPublisher appliedOutputErrorPublisher;
    private final DoublePublisher motorCurrentPublisher;
    private final BooleanPublisher motorCurrentValidPublisher;
    private final StringPublisher motorCurrentErrorPublisher;
    private final DoublePublisher encoderVelocityPublisher;
    private final BooleanPublisher encoderVelocityValidPublisher;
    private final StringPublisher encoderVelocityErrorPublisher;

    private MotorTelemetry(String prefix) {
      portPublisher = table.getStringTopic(prefix + "/port").publish();
      busPublisher = table.getDoubleTopic(prefix + "/bus").publish();
      commandPublisher = table.getDoubleTopic(prefix + "/command").publish();
      connectedPublisher = table.getBooleanTopic(prefix + "/connected").publish();
      commandAcceptedPublisher = table.getBooleanTopic(prefix + "/commandAccepted").publish();
      respondingPublisher = table.getBooleanTopic(prefix + "/responding").publish();
      firmwarePublisher = table.getStringTopic(prefix + "/firmware").publish();
      lastErrorPublisher = table.getStringTopic(prefix + "/lastError").publish();
      busVoltagePublisher = table.getDoubleTopic(prefix + "/busVoltage").publish();
      busVoltageValidPublisher = table.getBooleanTopic(prefix + "/busVoltageValid").publish();
      busVoltageErrorPublisher = table.getStringTopic(prefix + "/busVoltageError").publish();
      appliedOutputPublisher = table.getDoubleTopic(prefix + "/appliedOutput").publish();
      appliedOutputValidPublisher =
          table.getBooleanTopic(prefix + "/appliedOutputValid").publish();
      appliedOutputErrorPublisher =
          table.getStringTopic(prefix + "/appliedOutputError").publish();
      motorCurrentPublisher = table.getDoubleTopic(prefix + "/motorCurrent").publish();
      motorCurrentValidPublisher = table.getBooleanTopic(prefix + "/motorCurrentValid").publish();
      motorCurrentErrorPublisher = table.getStringTopic(prefix + "/motorCurrentError").publish();
      encoderVelocityPublisher = table.getDoubleTopic(prefix + "/encoderVelocity").publish();
      encoderVelocityValidPublisher =
          table.getBooleanTopic(prefix + "/encoderVelocityValid").publish();
      encoderVelocityErrorPublisher =
          table.getStringTopic(prefix + "/encoderVelocityError").publish();
    }
  }
}
