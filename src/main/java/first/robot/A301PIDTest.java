package first.robot;

import com.revrobotics.spark.A301;
import com.revrobotics.util.Signal;
import org.wpilib.driverstation.DefaultUserControls;
import org.wpilib.math.controller.PIDController;
import org.wpilib.networktables.BooleanPublisher;
import org.wpilib.networktables.DoublePublisher;
import org.wpilib.networktables.NetworkTable;
import org.wpilib.networktables.NetworkTableInstance;
import org.wpilib.networktables.StringPublisher;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Teleop;


@Teleop(name = "A301 PID Position Test")
public class A301PIDTest extends PeriodicOpMode {

  // Motioncore D0 bus and default A301 CAN ID
  private static final int MOTIONCORE_CAN_D0_BUS_ID = 5;
  private static final int DEFAULT_A301_CAN_ID = 3;

  // PID constants (safe starting values)
  private static final double kP = 0.6;
  private static final double kI = 0.0;
  private static final double kD = 0.0;

  // Move distance in motor rotations
  private static final double POSITION_STEP = 1.0;

  // Limit motor power so it moves slowly while tuning
  private static final double MAX_OUTPUT = 0.35;

  // Consider target reached within this many rotations
  private static final double POSITION_TOLERANCE = 0.03;

  private final DefaultUserControls userControls;

  // NetworkTables telemetry
  private final NetworkTable telemetryTable =
      NetworkTableInstance.getDefault().getTable("A301PIDTest");

  private final DoublePublisher positionPublisher =
      telemetryTable.getDoubleTopic("position").publish();

  private final DoublePublisher targetPublisher =
      telemetryTable.getDoubleTopic("target").publish();

  private final DoublePublisher outputPublisher =
      telemetryTable.getDoubleTopic("pidOutput").publish();

  private final DoublePublisher busVoltagePublisher =
      telemetryTable.getDoubleTopic("busVoltage").publish();

  private final DoublePublisher appliedOutputPublisher =
      telemetryTable.getDoubleTopic("appliedOutput").publish();

  private final DoublePublisher currentPublisher =
      telemetryTable.getDoubleTopic("motorCurrent").publish();

  private final BooleanPublisher atSetpointPublisher =
      telemetryTable.getBooleanTopic("atSetpoint").publish();

  private final BooleanPublisher connectedPublisher =
      telemetryTable.getBooleanTopic("connected").publish();

  private final StringPublisher statusPublisher =
      telemetryTable.getStringTopic("status").publish();

  private final StringPublisher firmwarePublisher =
      telemetryTable.getStringTopic("firmware").publish();

  private A301 motor;

  // WPILib PID controller
  private final PIDController pid = new PIDController(kP, kI, kD);

  // Desired motor position (rotations)
  private double targetPosition = 0.0;

  // Button edge detection
  private boolean lastCircle = false;
  private boolean lastSquare = false;

  public A301PIDTest(DefaultUserControls userControls) {
    this.userControls = userControls;
  }

  @Override
  public void start() {
    try {
      motor = new A301(MOTIONCORE_CAN_D0_BUS_ID, DEFAULT_A301_CAN_ID);

      // Start by holding current position
      targetPosition = motor.getAbsoluteEncoderPosition().get(0.0);

      pid.setTolerance(POSITION_TOLERANCE);

      motor.setThrottle(0.0);

      connectedPublisher.set(true);
      statusPublisher.set(
          "Connected - Circle = +1 rotation, Square = -1 rotation");
      firmwarePublisher.set(motor.getFirmwareString());

    } catch (RuntimeException e) {
      motor = null;
      connectedPublisher.set(false);
      statusPublisher.set("Connection failed: " + e.getMessage());
      firmwarePublisher.set("");
    }
  }


  @Override
  public void periodic() {
    if (motor == null) {
      return;
    }

    var gamepad = userControls.getGamepad(0);

    boolean circle = gamepad.getEastFaceButton();   // Circle on PS5
    boolean square = gamepad.getWestFaceButton();   // Square on PS5

    // Current motor position
    double currentPosition = motor.getAbsoluteEncoderPosition().get(0.0);

    // Press Circle once -> move forward
    if (circle && !lastCircle) {
      targetPosition = currentPosition + POSITION_STEP;
      statusPublisher.set("Moving forward to " + targetPosition);
    }

    // Press Square once -> move backward
    if (square && !lastSquare) {
      targetPosition = currentPosition - POSITION_STEP;
      statusPublisher.set("Moving backward to " + targetPosition);
    }

    lastCircle = circle;
    lastSquare = square;

    // PID calculation
    double output = pid.calculate(currentPosition, targetPosition);

    // Clamp output
    output = Math.max(-MAX_OUTPUT, Math.min(MAX_OUTPUT, output));

    // Stop tiny oscillations when at target
    if (pid.atSetpoint()) {
      output = 0.0;
    }

    // Command motor
    motor.setThrottle(output);

    // Publish telemetry
    positionPublisher.set(currentPosition);
    targetPublisher.set(targetPosition);
    outputPublisher.set(output);
    atSetpointPublisher.set(pid.atSetpoint());

    Signal<Double> busVoltage = motor.getBusVoltage();
    Signal<Double> appliedOutput = motor.getAppliedOutput();
    Signal<Double> current = motor.getMotorCurrent();

    busVoltagePublisher.set(busVoltage.get(0.0));
    appliedOutputPublisher.set(appliedOutput.get(0.0));
    currentPublisher.set(current.get(0.0));

    // Debug print to RioLog
    System.out.printf(
        "Pos: %.3f  Target: %.3f  Out: %.3f  At: %b%n",
        currentPosition,
        targetPosition,
        output,
        pid.atSetpoint());
  }

  @Override
  public void end() {
    if (motor != null) {
      motor.setThrottle(0.0);
    }
  }

  @Override
  public void close() {
    if (motor != null) {
      motor.close();
    }
  }
}