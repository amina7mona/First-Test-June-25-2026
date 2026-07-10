// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot;

import org.wpilib.driverstation.DefaultUserControls;
import org.wpilib.networktables.BooleanPublisher;
import org.wpilib.networktables.DoublePublisher;
import org.wpilib.networktables.NetworkTable;
import org.wpilib.networktables.NetworkTableInstance;
import org.wpilib.networktables.StringPublisher;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Teleop;

@Teleop(name = "NT Telemetry Example")
public class NetworkTableTelemetryExampleTeleMode extends PeriodicOpMode {
  /*
   * NetworkTables telemetry-only example
   * ------------------------------------
   * This OpMode does not drive the robot. It only reads gamepad values and publishes
   * them to NetworkTables so students can see how telemetry works.
   *
   * FTC SDK mental model:
   *
   *   telemetry.addData("left x", leftX);
   *   telemetry.addData("button pressed", isPressed);
   *   telemetry.addData("status", "Robot is idle");
   *   telemetry.update();
   *
   * NetworkTables mental model:
   *
   *   make publishers once
   *   call publisher.set(value) every loop
   *   view the values in OutlineViewer or Elastic
   *
   * To view these values, read:
   *
   *   OUTLINEVIEWER_AND_ELASTIC_NETWORKTABLES_README.md
   *
   * The structure below creates these dashboard paths:
   *
   *   /TelemetryExample/leftX
   *   /TelemetryExample/leftY
   *   /TelemetryExample/rightX
   *   /TelemetryExample/leftTrigger
   *   /TelemetryExample/rightTrigger
   *   /TelemetryExample/leftStickMagnitude
   *   /TelemetryExample/southFaceButton
   *   /TelemetryExample/leftBumperButton
   *   /TelemetryExample/rightBumperButton
   *   /TelemetryExample/leftStickActive
   *   /TelemetryExample/driveDirectionStatus
   *   /TelemetryExample/buttonStatus
   *
   * Flow of the NetworkTables pieces:
   *
   *   gamepad values
   *       |
   *       v
   *   periodic()
   *       |
   *       v
   *   publishTelemetry(...)
   *       |
   *       v
   *   NetworkTableInstance.getDefault()
   *       |
   *       v
   *   Table: "TelemetryExample"       -> /TelemetryExample
   *       |
   *       +-- DoublePublisher values  -> numbers
   *       +-- BooleanPublisher values -> true/false
   *       +-- StringPublisher values  -> readable status text
   *       |
   *       v
   *   OutlineViewer or Elastic displays the newest values
   */
  private static final double STICK_ACTIVE_THRESHOLD = 0.15;

  private final Robot robot;
  private final DefaultUserControls userControls;

  private final NetworkTable telemetryExampleTable =
      NetworkTableInstance.getDefault().getTable("TelemetryExample");

  private final DoublePublisher leftXPublisher =
      telemetryExampleTable.getDoubleTopic("leftX").publish();
  private final DoublePublisher leftYPublisher =
      telemetryExampleTable.getDoubleTopic("leftY").publish();
  private final DoublePublisher rightXPublisher =
      telemetryExampleTable.getDoubleTopic("rightX").publish();
  private final DoublePublisher leftTriggerPublisher =
      telemetryExampleTable.getDoubleTopic("leftTrigger").publish();
  private final DoublePublisher rightTriggerPublisher =
      telemetryExampleTable.getDoubleTopic("rightTrigger").publish();
  private final DoublePublisher leftStickMagnitudePublisher =
      telemetryExampleTable.getDoubleTopic("leftStickMagnitude").publish();

  private final BooleanPublisher southFaceButtonPublisher =
      telemetryExampleTable.getBooleanTopic("southFaceButton").publish();
  private final BooleanPublisher leftBumperButtonPublisher =
      telemetryExampleTable.getBooleanTopic("leftBumperButton").publish();
  private final BooleanPublisher rightBumperButtonPublisher =
      telemetryExampleTable.getBooleanTopic("rightBumperButton").publish();
  private final BooleanPublisher leftStickActivePublisher =
      telemetryExampleTable.getBooleanTopic("leftStickActive").publish();

  private final StringPublisher driveDirectionStatusPublisher =
      telemetryExampleTable.getStringTopic("driveDirectionStatus").publish();
  private final StringPublisher buttonStatusPublisher =
      telemetryExampleTable.getStringTopic("buttonStatus").publish();

  public NetworkTableTelemetryExampleTeleMode(
      Robot robot, DefaultUserControls userControls) {
    this.robot = robot;
    this.userControls = userControls;
  }

  @Override
  public void start() {
    robot.motor0.setThrottle(0.0);
    robot.motor1.setThrottle(0.0);
    robot.motor2.setThrottle(0.0);
    robot.motor3.setThrottle(0.0);
  }

  @Override
  public void periodic() {
    var gamepad = userControls.getGamepad(0);

    double leftX = gamepad.getLeftX();
    double leftY = -gamepad.getLeftY();
    double rightX = gamepad.getRightX();
    double leftTrigger = gamepad.getLeftTriggerAxis();
    double rightTrigger = gamepad.getRightTriggerAxis();
    double leftStickMagnitude = Math.hypot(leftX, leftY);

    boolean southFaceButton = gamepad.getSouthFaceButton();
    boolean leftBumperButton = gamepad.getLeftBumperButton();
    boolean rightBumperButton = gamepad.getRightBumperButton();
    boolean leftStickActive = leftStickMagnitude > STICK_ACTIVE_THRESHOLD;

    String driveDirectionStatus =
        getDriveDirectionStatus(leftX, leftY, leftStickActive);
    String buttonStatus =
        getButtonStatus(southFaceButton, leftBumperButton, rightBumperButton);

    /*
     * This is the telemetry.update() moment for this example. Every loop, the robot
     * publishes the newest number, boolean, and string values to NetworkTables.
     */
    publishTelemetry(
        leftX,
        leftY,
        rightX,
        leftTrigger,
        rightTrigger,
        leftStickMagnitude,
        southFaceButton,
        leftBumperButton,
        rightBumperButton,
        leftStickActive,
        driveDirectionStatus,
        buttonStatus);
  }

  private void publishTelemetry(
      double leftX,
      double leftY,
      double rightX,
      double leftTrigger,
      double rightTrigger,
      double leftStickMagnitude,
      boolean southFaceButton,
      boolean leftBumperButton,
      boolean rightBumperButton,
      boolean leftStickActive,
      String driveDirectionStatus,
      String buttonStatus) {
    leftXPublisher.set(leftX);
    leftYPublisher.set(leftY);
    rightXPublisher.set(rightX);
    leftTriggerPublisher.set(leftTrigger);
    rightTriggerPublisher.set(rightTrigger);
    leftStickMagnitudePublisher.set(leftStickMagnitude);

    southFaceButtonPublisher.set(southFaceButton);
    leftBumperButtonPublisher.set(leftBumperButton);
    rightBumperButtonPublisher.set(rightBumperButton);
    leftStickActivePublisher.set(leftStickActive);

    driveDirectionStatusPublisher.set(driveDirectionStatus);
    buttonStatusPublisher.set(buttonStatus);
  }

  private static String getDriveDirectionStatus(
      double leftX, double leftY, boolean leftStickActive) {
    if (!leftStickActive) {
      return "Left stick is centered";
    }

    if (Math.abs(leftY) >= Math.abs(leftX)) {
      if (leftY > 0.0) {
        return "Left stick says forward";
      }
      return "Left stick says backward";
    }

    if (leftX > 0.0) {
      return "Left stick says right";
    }
    return "Left stick says left";
  }

  private static String getButtonStatus(
      boolean southFaceButton, boolean leftBumperButton, boolean rightBumperButton) {
    if (southFaceButton) {
      return "South face button is pressed";
    }

    if (leftBumperButton && rightBumperButton) {
      return "Both bumper buttons are pressed";
    }

    if (leftBumperButton) {
      return "Left bumper is pressed";
    }

    if (rightBumperButton) {
      return "Right bumper is pressed";
    }

    return "No example buttons are pressed";
  }
}
