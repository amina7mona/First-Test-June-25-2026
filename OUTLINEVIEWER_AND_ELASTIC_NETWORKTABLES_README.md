# Viewing NetworkTables Telemetry with OutlineViewer or Elastic

> [!IMPORTANT]
> This guide documents the **macOS workflow** tested with the WPILib 2027 alpha tools.
>
> If the WPILib VS Code **Start Tool** command cannot find or launch dashboard tools, use the direct app paths below.

## Goal

This project uses direct NetworkTables telemetry instead of treating `SmartDashboard` as the main telemetry API.

The goal is for students to see robot data as named tables and topics:

```text
Robot code -> NetworkTables table -> OutlineViewer or Elastic
```

That makes the dashboard structure visible and intentional. Instead of publishing everything under `/SmartDashboard`, this project publishes examples under readable tables such as:

```text
KiwiDriveExample
TelemetryExample
```

## Contents

- [Tool Options](#tool-options)
- [WPILib Install Folder](#wpilib-install-folder)
- [If WPILib Start Tool Does Not Work](#if-wpilib-start-tool-does-not-work)
- [Robot Modes and Tables](#robot-modes-and-tables)
- [Option 1: OutlineViewer](#option-1-outlineviewer)
- [Option 2: Elastic](#option-2-elastic)
- [Telemetry Values](#telemetry-values)
- [Troubleshooting](#troubleshooting)

## Tool Options

| Tool | Best For | What You See |
| --- | --- | --- |
| OutlineViewer | Confirming raw NetworkTables values exist | A tree of tables and values |
| Elastic | Building a student-facing dashboard view | Widgets you can drag onto a grid |

Use **OutlineViewer** when you want the simplest possible check that values are publishing.

Use **Elastic** when you want a nicer dashboard layout for students or driver-station-style viewing.

## WPILib Install Folder

In the examples below, replace:

```text
<WPILIB_INSTALL>
```

with the folder where WPILib is installed on your machine.

For a default user-folder install on macOS, that may look like:

```text
~/wpilib/2027_alpha5
```

Do not assume the exact folder is the same for every computer. Use the folder where your WPILib alpha tools were installed.

## If WPILib Start Tool Does Not Work

Sometimes VS Code's WPILib `Start Tool` command may fail to launch tools. In the tested setup, it reported that tools could not be found, tried to resolve or install tools through Gradle, and still did not launch the dashboard tools after restarting VS Code.

If that happens, skip the VS Code launcher and open the tools directly.

OutlineViewer:

```text
<WPILIB_INSTALL>/tools/OutlineViewer.app
```

Elastic folder:

```text
<WPILIB_INSTALL>/elastic/
```

Elastic app:

```text
<WPILIB_INSTALL>/elastic/elastic_dashboard
```

If `elastic_dashboard` is not present, unpack:

```text
<WPILIB_INSTALL>/elastic/Elastic-WPILib-macOS.tar.gz
```

If `OutlineViewer.app` is not present, check:

```text
<WPILIB_INSTALL>/tools/artifacts/
```

There may be an archive named like:

```text
OutlineViewer-2027.0.0-alpha-6-osxuniversal.zip
```

Unpack the archive only if the app is missing from `tools/`.

## Robot Modes and Tables

This project includes two NetworkTables examples.

| Driver Station Mode | Purpose | NetworkTables Table |
| --- | --- | --- |
| `Kiwi Drive + NT Telemetry` | Drives the Kiwi robot and publishes drive telemetry | `KiwiDriveExample` |
| `NT Telemetry Example` | Publishes joystick telemetry only; does not drive the robot | `TelemetryExample` |

In dashboard tools, these usually appear as table names. You do **not** type the leading slash.

For example, look for:

```text
KiwiDriveExample
```

not:

```text
/KiwiDriveExample
```

## Option 1: OutlineViewer

OutlineViewer is the most direct way to verify that NetworkTables publishing is working.

### Open OutlineViewer

Open:

```text
<WPILIB_INSTALL>/tools/OutlineViewer.app
```

### Connect OutlineViewer

In OutlineViewer, open:

```text
Options -> Settings
```

Use these settings:

```text
Mode: Client
Team/IP: your configured team number
Port: 5810 / Default
Network Identity: outlineviewer
Set Address from DS: checked
```

For the tested setup, the configured team number was:

```text
666
```

Use your own configured team number.

Click:

```text
Apply
```

### View the Tables

After the robot code is deployed:

1. Select `Kiwi Drive + NT Telemetry` or `NT Telemetry Example` in Driver Station.
2. Enable TeleOp.
3. Move the gamepad joysticks or press example buttons.
4. In OutlineViewer, look under:

```text
Transitory Values
```

5. Expand:

```text
KiwiDriveExample
```

or:

```text
TelemetryExample
```

The values should update live while TeleOp is enabled.

## Option 2: Elastic

Elastic is better for building a visual dashboard from the NetworkTables values.

### Open Elastic

Open the WPILib install folder:

```text
<WPILIB_INSTALL>
```

Then open:

```text
elastic
```

If needed, unpack:

```text
Elastic-WPILib-macOS.tar.gz
```

Then double-click:

```text
elastic_dashboard
```

### Connect Elastic

In Elastic, click:

```text
Settings
```

On the `Network` tab, use the robot/team settings.

The tested setup used:

```text
Team Number: 666
IP Address Mode: Driver Station
Target Server: Robot Code
```

Use your own configured team number.

Elastic should show a connected status at the bottom of the window, such as:

```text
Network Tables: Connected (172.30.0.1)
Team 666
```

### Add a NetworkTables Widget

After Elastic is connected and TeleOp is enabled:

1. Click:

```text
+ Add Widget
```

2. Stay on the `Network Tables` tab.
3. Find one of these tables:

```text
KiwiDriveExample
TelemetryExample
```

4. Drag the table onto the main grid.
5. Move the joystick while TeleOp is enabled.

The widget values should update live.

## Telemetry Values

### `KiwiDriveExample`

| Value | Type | Meaning |
| --- | --- | --- |
| `xInput` | double | Left stick X after deadband |
| `yInput` | double | Left stick Y after deadband, with forward made positive |
| `rotationInput` | double | Right stick X after deadband |
| `frontLeftOutput` | double | Computed throttle sent to motor 3 |
| `frontRightOutput` | double | Computed throttle sent to motor 1 |
| `backOutput` | double | Computed throttle sent to motor 2 |

### `TelemetryExample`

| Value | Type | Meaning |
| --- | --- | --- |
| `leftX` | double | Raw left stick X |
| `leftY` | double | Left stick Y, with forward made positive |
| `rightX` | double | Raw right stick X |
| `leftTrigger` | double | Left trigger axis |
| `rightTrigger` | double | Right trigger axis |
| `leftStickMagnitude` | double | Distance of the left stick from center |
| `southFaceButton` | boolean | Whether the south face button is pressed |
| `leftBumperButton` | boolean | Whether the left bumper is pressed |
| `rightBumperButton` | boolean | Whether the right bumper is pressed |
| `leftStickActive` | boolean | Whether the left stick is far enough from center to count as active |
| `driveDirectionStatus` | string | A readable message describing the left stick direction |
| `buttonStatus` | string | A readable message describing selected button states |

## Troubleshooting

If a table does not appear:

- Make sure the latest code has been deployed.
- Make sure Driver Station is running `Kiwi Drive + NT Telemetry` or `NT Telemetry Example`.
- Make sure TeleOp is enabled.
- Move the joysticks so values change.
- In OutlineViewer, reopen `Options -> Settings`, confirm the team number, keep `Set Address from DS` checked, and click `Apply`.
- In Elastic, reopen `Settings`, confirm the team number, use `IP Address Mode: Driver Station`, use `Target Server: Robot Code`, and check the bottom status bar for `Network Tables: Connected`.
- If you are testing local simulation instead of the real robot/SystemCore connection, try `localhost` in the Team/IP or connection field. For the tested robot setup, the configured team number worked.

If VS Code Start Tool keeps failing, open the dashboard apps directly from `<WPILIB_INSTALL>` instead of spending time in the restart/retry loop.
