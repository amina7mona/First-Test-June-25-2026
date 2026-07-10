# WPILib Test Project Folder

> [!IMPORTANT]
> **Open this individual test-project folder as the WPILib / VS Code project root.**
>
> Do **not** open the parent repository folder as the robot project.

## Project Context

This folder is part of a fork of the WPILib SystemCore testing repository. The larger repository exists to hold SystemCore-related test projects, but this specific folder is the actual WPILib robot project.

The intended structure is:

```text
SystemcoreTesting/
└── testprojects/
    └── <project-folder>/          <-- open this folder in WPILib VS Code
        ├── .vscode/
        ├── .wpilib/
        ├── gradle/
        ├── src/
        ├── vendordeps/
        ├── build.gradle
        ├── gradlew
        ├── gradlew.bat
        └── settings.gradle
```

## What Was Added

This test project is based on the Expansion Hub Hybrid project template/example offered through WPILib tooling.

Current project-specific addition:

| Area | Status |
| --- | --- |
| Base project | Expansion Hub Hybrid example/template |
| Added drive support | KiwiDrive |
| Intended target | SystemCore / WPILib robot-style Gradle project |

## NetworkTables Telemetry

The teleop mode `Kiwi Drive + NT Telemetry` publishes joystick inputs and computed Kiwi drive outputs to NetworkTables under:

```text
KiwiDriveExample
```

The teleop mode `NT Telemetry Example` is a telemetry-only example. It does not drive the robot; it publishes joystick numbers, booleans, and strings under:

```text
TelemetryExample
```

For the exact steps to view those values with OutlineViewer or Elastic, including the workaround for WPILib `Start Tool` not finding installed tools, see:

[OUTLINEVIEWER_AND_ELASTIC_NETWORKTABLES_README.md](OUTLINEVIEWER_AND_ELASTIC_NETWORKTABLES_README.md)

## FTC Student Structure Guide

For a comparison between familiar FTC OpMode structure and the SystemCore / WPILib-style structure used in this project, see:

[FTC_TO_SYSTEMCORE_STRUCTURE_README.md](FTC_TO_SYSTEMCORE_STRUCTURE_README.md)

For the expanded student guide with side-by-side FTC and SystemCore code comparisons, a lifecycle walkthrough, a Kiwi drive case study, and a glossary, see:

[FTC_STUDENT_ULTIMATE_SYSTEMCORE_GUIDE.md](FTC_STUDENT_ULTIMATE_SYSTEMCORE_GUIDE.md)

## VS Code / WPILib Setup

When using Visual Studio Code with the WPILib extension, import or open the individual project folder inside `testprojects/`:

```text
SystemcoreTesting/testprojects/<project-folder>
```

This is the project root because it contains the files WPILib Gradle expects:

| Required project-root item | Why it matters |
| --- | --- |
| `build.gradle` | Defines the robot/SystemCore Gradle build |
| `settings.gradle` | Configures Gradle plugin resolution |
| `gradlew` / `gradlew.bat` | Project-specific Gradle wrapper scripts |
| `gradle/` | Gradle wrapper files |
| `.wpilib/` | WPILib project metadata |
| `.vscode/` | VS Code project configuration |

> [!CAUTION]
> The `gradle/` folder is **not** the folder to open by itself.
>
> Open the project folder that **contains** `gradle/`, `build.gradle`, `settings.gradle`, and `gradlew`.

## Common Mistake

If someone clones or downloads the larger repository, it is natural to open the top-level folder:

```text
SystemcoreTesting
```

That is the repository root, but it is **not** the robot project root for this test project.

Opening the parent folder can cause WPILib VS Code, Robot Gradle, or SystemCore build tasks to resolve the wrong directory. The result is usually a confusing build failure even though the actual project files are present one level deeper.

## Quick Check Before Building

Before building, make sure the VS Code Explorer top-level folder is the individual test-project folder, whatever it is currently named.

For example, the folder might be named:

```text
First Test June 25 2026
```

or it might be renamed later. The name is less important than the structure.

What matters is that these files are visible at the top level of the folder opened in VS Code:

- `build.gradle`
- `settings.gradle`
- `gradlew`
- `gradle/`
- `.wpilib/`
- `src/`

If those files are not visible at the top level of the VS Code window, close the folder and reopen:

```text
SystemcoreTesting/testprojects/<project-folder>
```

## Adding More Test Projects

More test projects can be added under `SystemcoreTesting/testprojects/`, but each one should remain isolated as its own WPILib Gradle project.

For every new test project, open the individual project folder in WPILib VS Code, not the parent repository folder.
