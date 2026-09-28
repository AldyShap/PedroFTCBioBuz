package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.follower.Follower;
import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.ManualDrive;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

@TeleOp(name = "BioBuzz_Optimal")
public class BioBuzzTeleOp extends LinearOpMode {

    // Ходовая
    private DcMotor frontLeft, frontRight, backLeft, backRight;
    private IMU imu;

    private Follower follower;

    // Механизмы
    private DcMotor intake, stopper;
    private Servo servo;

    private GoBildaPinpointDriver pinpointDriver;

    // Шутер на DcMotorEx для ПИД-контроля
    private DcMotorEx shooterLeft, shooterRight;

    // Настройки шутера
    // Настройки мотора REV HD Hex
    private static final double MOTOR_TICKS_PER_REV = 28.0;

    // Укажите передаточное число вашего UltraPlanetary (например, 3.0 для 3:1 или 1.0 если без картриджей)
    private static final double GEAR_RATIO = 1.0;

    // Результирующее кол-во тиков на оборот ВЫХОДНОГО вала
    private static final double TICKS_PER_REV = MOTOR_TICKS_PER_REV * GEAR_RATIO;

    // Желаемые обороты ВЫХОДНОГО ВАЛА (для 3:1 разумно ставить 1500-1800 RPM)
    private double targetRPM = 2700.0;
    private boolean isShooting = false;
    private boolean previousXState = false;

    double P = 7;
    double F = 14;

    @Override
    public void runOpMode() {

        // 1. Инициализация ходовой
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        backLeft = hardwareMap.get(DcMotor.class, "backLeft");
        backRight = hardwareMap.get(DcMotor.class, "backRight");

//        frontLeft.setDirection(DcMotor.Direction.FORWARD);
//        frontRight.setDirection(DcMotor.Direction.REVERSE);
//        backLeft.setDirection(DcMotor.Direction.FORWARD);
//        backRight.setDirection(DcMotor.Direction.REVERSE);

        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // 2. Инициализация IMU
        imu = hardwareMap.get(IMU.class, "imu");
        IMU.Parameters parameters = new IMU.Parameters(new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.BACKWARD,
                RevHubOrientationOnRobot.UsbFacingDirection.DOWN));
        imu.initialize(parameters);

        follower = Constants.create(hardwareMap);

        pinpointDriver = hardwareMap.get(GoBildaPinpointDriver.class, "pinpoint");

        pinpointDriver.resetPosAndIMU();

        // 3. Интейк и Серво
        intake = hardwareMap.get(DcMotor.class, "intake");
        stopper = hardwareMap.get(DcMotor.class, "stopper");
        servo = hardwareMap.get(Servo.class, "servo");

        // 4. Шутер (DcMotorEx + FLOAT)
        shooterLeft = hardwareMap.get(DcMotorEx.class, "shooterLeft");
        shooterRight = hardwareMap.get(DcMotorEx.class, "shooterRight");

        shooterLeft.setDirection(DcMotor.Direction.FORWARD);
        shooterRight.setDirection(DcMotor.Direction.FORWARD);

        shooterLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        shooterRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // Защита редукторов шутера
        shooterLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shooterRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        PIDFCoefficients new_pidfCoefficients = new PIDFCoefficients(P, 0, 0, F);
        shooterLeft.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, new_pidfCoefficients);
        shooterRight.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, new_pidfCoefficients);

        telemetry.addData("Status", "Initialized");
        telemetry.addData("servo position: ",servo.getPosition());
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {

            double drive = gamepad1.left_stick_y;
            double strafe = gamepad1.left_stick_x;
            double rotate = gamepad1.right_stick_x;
//
//            // Сброс нулевого угла поля по кнопке Options / Start
//            if (gamepad1.options) {
//                imu.resetYaw();
//            }
//
//            // Получаем текущий угол
//            double botHeading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);
//            // Поворот векторов движения на угол робота
//
//            double rotX = x * Math.cos(-botHeading) - y * Math.sin(-botHeading);
//            double rotY = x * Math.sin(-botHeading) + y * Math.cos(-botHeading);

//            double frontLeftPower = drive - strafe + rotate*0.7;
//            double frontRightPower = drive + strafe - rotate*0.7;
//            double backLeftPower = drive + strafe + rotate*0.7;
//            double backRightPower = drive - strafe - rotate*0.7;
//
//            // Нормализация мощности
//            double max = Math.max(Math.abs(frontLeftPower),
//                    Math.max(Math.abs(frontRightPower), Math.max(Math.abs(backLeftPower), Math.abs(backRightPower))));
//
//            if (max > 1.0) {
//                frontLeftPower /= max;
//                frontRightPower /= max;
//                backLeftPower /= max;
//                backRightPower /= max;
//            }
//
//            frontLeft.setPower(frontLeftPower);
//            frontRight.setPower(frontRightPower);
//            backLeft.setPower(backLeftPower);
//            backRight.setPower(backRightPower);

            DrivePowers powers = ManualDrive.fieldCentric(
                    -gamepad1.left_stick_y,
                    -gamepad1.left_stick_x,
                    -gamepad1.right_stick_x,
                    follower.pose().heading()
            );
            follower.manual(powers);
            follower.update();

            // --- Единая логика интейка без     конфликтов ---
            if (gamepad2.b) {
                intake.setPower(-1.0);
                stopper.setPower(0);
            } else if (gamepad2.a) {
                intake.setPower(1.0);
                stopper.setPower(0.0);
            } else if (gamepad2.dpad_left) {
                intake.setPower(1.0);
                stopper.setPower(-1.0);
            } else if (gamepad2.dpad_right) {
                intake.setPower(-1.0);
                stopper.setPower(1.0);
            } else if (gamepad1.dpadDownWasPressed()) {
                servo.setPosition(0.9);
            } else if (gamepad1.dpadUpWasPressed()) {
                servo.setPosition(0.0);
            } else {
                intake.setPower(0.0);
                stopper.setPower(0.0);
            }

            // --- ПИД-управление шутером (Кнопка X) ---
            boolean currentXState = gamepad2.x;
            if (currentXState && !previousXState) {
                isShooting = !isShooting;
            }
            previousXState = currentXState;

            double targetTicksPerSec = isShooting ? (targetRPM * TICKS_PER_REV) / 60.0 : 0.0;
            shooterLeft.setVelocity(targetTicksPerSec);
            shooterRight.setVelocity(targetTicksPerSec);

            // Телеметрия
            telemetry.addData("Shooter", isShooting ? "ON (%.0f RPM)": "OFF", targetRPM);
            telemetry.addData("Real RPM Left", "%.1f", (shooterLeft.getVelocity() * 60.0) / TICKS_PER_REV);
            telemetry.addData("Real RPM Right", "%.1f", (shooterRight.getVelocity() * 60.0) / TICKS_PER_REV);
            telemetry.update();
        }
    }
}