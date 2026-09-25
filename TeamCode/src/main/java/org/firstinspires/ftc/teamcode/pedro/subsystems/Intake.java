package org.firstinspires.ftc.teamcode.pedro.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.teamcode.pedro.subsystems.Transfer;

public class Intake {

    private DcMotor intake;
    private Transfer stopper;

    public Intake(HardwareMap hardwareMap) {
        this.intake = hardwareMap.get(DcMotor.class, "intake");
        this.stopper = new Transfer(hardwareMap);
    }

    public void spin() {
        intake.setPower(1.0);

    }

    public void stop() {
        intake.setPower(0.0);
    }

    public void feedOn() {
        intake.setPower(-1.0);
        stopper.spinOn();
    }

    public void feedOff() {
        intake.setPower(0.0);
        stopper.spinOff();
    }
}
