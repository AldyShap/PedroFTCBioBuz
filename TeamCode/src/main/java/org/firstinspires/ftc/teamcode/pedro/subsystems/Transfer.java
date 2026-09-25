package org.firstinspires.ftc.teamcode.pedro.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Transfer {
    private DcMotor stopper;

    public Transfer(HardwareMap hardwareMap) {
        this.stopper = hardwareMap.get(DcMotor.class, "stopper");
    }

    public void spinOn() {
        stopper.setPower(1.0);
    }

    public void spinOff() {
        stopper.setPower(0.0);
    }
}
