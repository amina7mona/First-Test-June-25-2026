// package first.robot;

// import org.wpilib.opmode.PeriodicOpMode;
// import org.wpilib.opmode.Teleop;
// import edu.wpi.first.cameraserver.CameraServer;
// import edu.wpi.first.cscore.UsbCamera;
// org.wpilib.vision.camera.UsbCamera
// import edu.wpi.first.wpilibj.Timer;

// @Teleop(name = "Webcam Test")
// public class WebcamTest extends PeriodicOpMode {

//     private org.wpilib.vision.camera.UsbCamera camera;

//     @Override
//     public void start() {
//         System.out.println("Starting Webcam Test...");

//         try {
//             camera = org.wpilib.vision.stream.CameraServer.startAutomaticCapture(0);

//             camera.setResolution(640, 480);
//             camera.setFPS(30);

//             System.out.println("Webcam started successfully!");
//         } catch (Exception e) {
//             System.out.println("Failed to start webcam.");
//             e.printStackTrace();
//         }
//     }

//     @Override
//     public void periodic() {
//         // CameraServer handles the streaming automatically.
//     }

//     public void stop() {
//         System.out.println("Stopping Webcam Test...");
//     }
// }