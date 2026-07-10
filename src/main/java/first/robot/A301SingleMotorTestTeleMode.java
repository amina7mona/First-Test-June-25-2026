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

@Teleop(name = "A301 Single Motor Test")
public class A301SingleMotorTestTeleMode extends PeriodicOpMode {
  /*
   * The NYC-FIRST fork-era A301 docs recommend CANBusMap.CAN_D0, but REVLib
   * 2027 alpha-3 does not publish that helper class. Systemcore exposes can_s0
   * through can_s4 first, then Motioncore can_d0, so bus ID 5 is the current
   * raw value for can_d0. With alpha-3, this test expects the motor to still
   * have its default CAN ID of 3.
   */
  private static final int MOTIONCORE_CAN_D0_BUS_ID = 5;
  private static final int DEFAULT_A301_CAN_ID = 3;
  private static final double DEADBAND = 0.05;
  private static final double PRECISION_SCALE = 0.30;
  private static final double FIXED_TEST_THROTTLE = 0.20;

  private final DefaultUserControls userControls;

  private final NetworkTable telemetryTable =
      NetworkTableInstance.getDefault().getTable("A301SingleMotorTest");
  private final DoublePublisher rawLeftYPublisher =
      telemetryTable.getDoubleTopic("rawLeftY").publish();
  private final DoublePublisher throttleCommandPublisher =
      telemetryTable.getDoubleTopic("throttleCommand").publish();
  private final DoublePublisher speedScalePublisher =
      telemetryTable.getDoubleTopic("speedScale").publish();
  private final DoublePublisher busVoltagePublisher =
      telemetryTable.getDoubleTopic("busVoltage").publish();
  private final DoublePublisher appliedOutputPublisher =
      telemetryTable.getDoubleTopic("appliedOutput").publish();
  private final DoublePublisher motorCurrentPublisher =
      telemetryTable.getDoubleTopic("motorCurrent").publish();
  private final DoublePublisher encoderVelocityPublisher =
      telemetryTable.getDoubleTopic("encoderVelocity").publish();
  private final BooleanPublisher precisionModePublisher =
      telemetryTable.getBooleanTopic("precisionMode").publish();
  private final BooleanPublisher fixedForwardPublisher =
      telemetryTable.getBooleanTopic("fixedForward").publish();
  private final BooleanPublisher fixedReversePublisher =
      telemetryTable.getBooleanTopic("fixedReverse").publish();
  private final BooleanPublisher motorConnectedPublisher =
      telemetryTable.getBooleanTopic("motorConnected").publish();
  private final BooleanPublisher busVoltageValidPublisher =
      telemetryTable.getBooleanTopic("busVoltageValid").publish();
  private final BooleanPublisher appliedOutputValidPublisher =
      telemetryTable.getBooleanTopic("appliedOutputValid").publish();
  private final StringPublisher statusPublisher =
      telemetryTable.getStringTopic("status").publish();
  private final StringPublisher firmwarePublisher =
      telemetryTable.getStringTopic("firmware").publish();
  private final StringPublisher busVoltageErrorPublisher =
      telemetryTable.getStringTopic("busVoltageError").publish();
  private final StringPublisher appliedOutputErrorPublisher =
      telemetryTable.getStringTopic("appliedOutputError").publish();

  private A301 motor;

  public A301SingleMotorTestTeleMode(DefaultUserControls userControls) {
    this.userControls = userControls;
  }

  @Override
public void start() {
    System.out.println("START METHOD CALLED");

    try {
        motor = new A301(MOTIONCORE_CAN_D0_BUS_ID, DEFAULT_A301_CAN_ID);
        motor.setThrottle(0.0);
        motorConnectedPublisher.set(true);
    } catch (RuntimeException e) {
        motor = null;
        motorConnectedPublisher.set(false);
    }
}

  // @Override
  // public void start() {
  //   try {
  //     motor = new A301(MOTIONCORE_CAN_D0_BUS_ID, DEFAULT_A301_CAN_ID);
  //     motor.setThrottle(0.0);
  //     motorConnectedPublisher.set(true);
  //     statusPublisher.set(
  //         "A301 connected on Motioncore D0, CAN ID " + DEFAULT_A301_CAN_ID);
  //     firmwarePublisher.set(motor.getFirmwareString());
  //   } catch (RuntimeException e) {
  //     motor = null;
  //     motorConnectedPublisher.set(false);
  //     statusPublisher.set("A301 connection failed: " + e.getMessage());
  //     firmwarePublisher.set("");
  //   }
  // }

@Override
public void periodic() {
    System.out.println("Periodic is running!");

    if (motor != null) {
        motor.setThrottle(0.2);
    }
}
  // public void periodic() {
  //   var gamepad = userControls.getGamepad(0);

  //   double rawLeftY = gamepad.getLeftY();
  //   double throttle = applyDeadband(-rawLeftY);
  //   double speedScale = gamepad.getLeftBumperButton() ? PRECISION_SCALE : 1.0;
  //   boolean fixedForward = gamepad.getSouthFaceButton();
  //   boolean fixedReverse = gamepad.getRightBumperButton();
  //   double throttleCommand = getThrottleCommand(throttle * speedScale, fixedForward, fixedReverse);

  //   rawLeftYPublisher.set(rawLeftY);
  //   throttleCommandPublisher.set(throttleCommand);
  //   speedScalePublisher.set(speedScale);
  //   precisionModePublisher.set(gamepad.getLeftBumperButton());
  //   fixedForwardPublisher.set(fixedForward);
  //   fixedReversePublisher.set(fixedReverse);

  //   if (motor != null) {
  //     motor.setThrottle(throttleCommand);
  //     publishMotorTelemetry();
  //   }
  // }

  // @Override
  // public void end() {
  //   if (motor != null) {
  //     motor.setThrottle(0.0);
  //   }
  // }

  // @Override
  // public void close() {
  //   if (motor != null) {
  //     motor.close();
  //   }
  // }

  // private static double applyDeadband(double value) {
  //   if (Math.abs(value) < DEADBAND) {
  //     return 0.0;
  //   }

  //   return value;
  // }

  // private static double getThrottleCommand(
  //     double joystickThrottle, boolean fixedForward, boolean fixedReverse) {
  //   if (fixedForward) {
  //     return FIXED_TEST_THROTTLE;
  //   }

  //   if (fixedReverse) {
  //     return -FIXED_TEST_THROTTLE;
  //   }

  //   return joystickThrottle;
  // }

  // private void publishMotorTelemetry() {
  //   Signal<Double> busVoltage = motor.getBusVoltage();
  //   Signal<Double> appliedOutput = motor.getAppliedOutput();
  //   Signal<Double> motorCurrent = motor.getMotorCurrent();
  //   Signal<Double> encoderVelocity = motor.getEncoderVelocity();

  //   busVoltagePublisher.set(busVoltage.get(0.0));
  //   appliedOutputPublisher.set(appliedOutput.get(0.0));
  //   motorCurrentPublisher.set(motorCurrent.get(0.0));
  //   encoderVelocityPublisher.set(encoderVelocity.get(0.0));

  //   busVoltageValidPublisher.set(busVoltage.isValid());
  //   appliedOutputValidPublisher.set(appliedOutput.isValid());
  //   busVoltageErrorPublisher.set(busVoltage.getError().toString());
  //   appliedOutputErrorPublisher.set(appliedOutput.getError().toString());
  // }
}
