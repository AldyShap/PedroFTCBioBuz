package org.firstinspires.ftc.teamcode.pedro.subsystems;


import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.teamcode.pedro.subsystems.Intake;

public class Shooter {

    private Intake intake;
    private DcMotorEx shooterLeft, shooterRight;
    private static final double MOTOR_TICKS_PER_REV = 28.0;

    // Укажите передаточное число вашего UltraPlanetary (например, 3.0 для 3:1 или 1.0 если без картриджей)
    private static final double GEAR_RATIO = 1.0;

    // Результирующее кол-во тиков на оборот ВЫХОДНОГО вала
    private static final double TICKS_PER_REV = MOTOR_TICKS_PER_REV * GEAR_RATIO;

    // Желаемые обороты ВЫХОДНОГО ВАЛА (для 3:1 разумно ставить 1500-1800 RPM)
    private double targetRPM = 2700.0;
    private double targetVel = 1260;

    double targetTicksPerSec = (targetRPM * TICKS_PER_REV) / 60.0;

    public Shooter(HardwareMap hardwareMap) {

        this.intake = new Intake(hardwareMap);

        this.shooterLeft = hardwareMap.get(DcMotorEx.class, "shooterLeft");
        this.shooterRight = hardwareMap.get(DcMotorEx.class, "shooterRight");

        this.shooterLeft.setDirection(DcMotorSimple.Direction.FORWARD);
        this.shooterRight.setDirection(DcMotorSimple.Direction.FORWARD);

        this.shooterLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        this.shooterRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        this.shooterLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        this.shooterRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
    }

    public void setRPM() {
        shooterLeft.setVelocity(targetTicksPerSec);
        shooterRight.setVelocity(targetTicksPerSec);
    }

    public double getRPM() {
        return shooterLeft.getVelocity();
    }
    public boolean isReady() {
        return Math.abs(getRPM() - targetVel) < 100;
    }

    public void spinOn() {
        intake.spin(); // intake.setPower(1.0);
    }

    public void spinOff() {
        intake.stop(); // intake.setPower(0.0);
    }
    public void stop() {
        shooterLeft.setPower(0);
        shooterRight.setPower(0);
    }

}
