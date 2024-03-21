package frc.robot.driver;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import frc.crevolib.util.ExpCurve;
import frc.crevolib.util.XboxGamepad;
import frc.robot.commands.RobotCommands;
import frc.robot.drivetrain.Drivetrain;
import frc.robot.drivetrain.commands.DrivetrainCommands;
import frc.robot.intakepivot.commands.IntakePivotCommands;
import frc.robot.intakepivot.commands.SetStateIntakePivot;
import frc.robot.intakeroller.commands.IntakeRollerCommands;
import frc.robot.shooterpivot.commands.SetAngleShooterPivot;

public class DriverXbox extends XboxGamepad {
    private static class Settings {
        static final int port = 0;
        static final String name = "driver";

        static final double kTranslationExpVal = 2.0;
        static final double kRotationExpVal = 1.0;
        static final double kDeadzone = 0.1;
    }

    private static DriverXbox mInstance;
    private final ExpCurve translationStickCurve, rotationStickCurve;

    private DriverXbox() {
        super(DriverXbox.Settings.name, DriverXbox.Settings.port);

        translationStickCurve = new ExpCurve(DriverXbox.Settings.kTranslationExpVal, 0, 1, DriverXbox.Settings.kDeadzone);
        rotationStickCurve = new ExpCurve(DriverXbox.Settings.kRotationExpVal, 0, 1, DriverXbox.Settings.kDeadzone);
    }

    public static DriverXbox getInstance() {
        if (mInstance == null) {
            mInstance = new DriverXbox();
        }
        return mInstance;
    }

    @Override
    public void setupTeleopButtons() {
        // Drivetrain Commands
        controller.y().onTrue(new InstantCommand(() -> Drivetrain.getInstance().zeroHeading()));
        controller.leftTrigger().whileTrue(DrivetrainCommands.driveSlowMode(this::getDriveTranslation, this::getDriveRotation));

        // Intake Commands
        controller.rightTrigger().whileTrue(IntakeRollerCommands.setOutput(() -> -1));
        controller.rightTrigger().onTrue(IntakePivotCommands.setPivotState(SetStateIntakePivot.State.kDeployed));
        controller.rightTrigger().onFalse(IntakePivotCommands.setPivotState(SetStateIntakePivot.State.kStowed));

        controller.b().whileTrue(IntakeRollerCommands.setOutput(() -> -1));


        controller.x().whileTrue(RobotCommands.primeSpeaker(SetAngleShooterPivot.Preset.kShooterNear));
        controller.a().whileTrue(RobotCommands.prime());
        //controller.circle().whileTrue(DrivetrainCommands.driveAndLockTarget(this::getDriveTranslation));
        //controller.povDown().whileTrue(ShooterPivotCommands.tuneLockSpeaker(() -> Rotation2d.fromDegrees(45)));

        controller.rightBumper().onTrue(RobotCommands.spitNote());

        controller.leftBumper().onTrue(RobotCommands.zero());



        // controller.L1().whileTrue(new ConditionalCommand(
        //     DrivetrainCommands.turnToAngle(Rotation2d.fromDegrees(28.51)),
        //     DrivetrainCommands.turnToAngle(Rotation2d.fromDegrees(-28.51)),
        //     () -> {
        //         var alliance = DriverStation.getAlliance();
        //         if (alliance.isPresent()) {
        //             return alliance.get() == DriverStation.Alliance.Red;
        //         }

        //         return false;
        //     }
        // ));
    }

    @Override
    public void setupDisabledButtons() {}

    @Override
    public void setupTestButtons() {}

    public Translation2d getDriveTranslation() {
        final var xComponent = translationStickCurve.calculate(-controller.getLeftX());
        final var yComponent = translationStickCurve.calculate(-controller.getLeftY());
        // Components are reversed because field coordinates are opposite of joystick coordinates
        return new Translation2d(yComponent, xComponent);
    }

    public double getDriveRotation() {
        return rotationStickCurve.calculate(-controller.getRightX());
    }
}