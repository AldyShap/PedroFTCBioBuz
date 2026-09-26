package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import com.pedropathing.follower.Follower;
import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.firstinspires.ftc.teamcode.pedro.subsystems.Shooter;
import org.firstinspires.ftc.teamcode.pedro.subsystems.Intake;

import com.pedropathing.ivy.Scheduler;
import static com.pedropathing.ivy.Scheduler.schedule;
import static com.pedropathing.ivy.commands.Commands.waitMs;
import static com.pedropathing.ivy.groups.Groups.parallel;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;
import static com.pedropathing.ivy.groups.Groups.sequential;
import com.pedropathing.ivy.Command;

import static com.pedropathing.api.Paths.*;
import com.pedropathing.paths.Path;


@Autonomous(name="BlueAuto")
public class BioBuzzBlueAuto  extends OpMode {

    private Follower follower;
    private Shooter shooter;
    private Intake intake;
    private final PoseFactory p = PoseFactory.degrees();

    private Pose shootPose = p.of(83.816, 109.5, 270);

    private Pose curvePose = p.of(124.05500982318271, 112.5992141453831, 270);

    private Pose startPose = p.of(83.816, 131.645, 270);
    private Pose parkPose = p.of(130.48722986247543, 35.00000000000001,  180);



    @Override
    public void init() {
        Scheduler.reset();
        follower = Constants.create(hardwareMap);
        follower.setPose(startPose);

        shooter = new Shooter(hardwareMap);
        intake = new Intake(hardwareMap);

    }

    @Override
    public void start() {
        schedule(
                AutoRoutine()
        );
    }

    @Override
    public void loop() {
        follower.update();
        Scheduler.execute();

        telemetry.addData("X", follower.pose().x());
        telemetry.addData("Y", follower.pose().y());
        telemetry.addData("Heading", Math.toDegrees(follower.pose().heading()));
        telemetry.addData("Follower Mode", follower.mode());

        telemetry.addData("Current RPM", shooter.getRPM());
        telemetry.addData("Is ready to shoot: ", shooter.isReady()? "Yes": "Not yet");
        telemetry.update();
    }

    private Path StartToShoot() {
        return line(startPose, parkPose).linear(startPose, parkPose);
    }

    private Path ShootToPark() {
        return curve(shootPose, curvePose, parkPose).linear(startPose, parkPose);
    }

    private Command AutoRoutine() {
        return sequential(
                follow(follower, StartToShoot()),
                Shoot(shooter),
                follow(follower, ShootToPark())
        );
    }

    private Command Shoot(Shooter shooter) {
        return sequential(
                SpinUp(shooter),
                Feed(intake)
        );
    }

    private Command Feed(Intake intake) {
        return sequential(
                Command.build()
                    .setStart(() -> intake.feedOn())
                    .setDone(() -> true),
                waitMs(5000),
                Command.build()
                    .setStart(() -> intake.feedOff())
                    .setDone(() -> true)
        );


    }

    private Command TakeBalls(Intake intake) {
        return sequential(
                Command.build()
                .setStart(() -> intake.spin())
                .setDone(() -> true),
        waitMs(5000),
                Command.build()
                        .setStart(() -> intake.stop())
                        .setDone(() -> true)
        );

    }

    private Command SpinUp(Shooter shooter) {
        return Command.build()
                .setStart(() -> shooter.setRPM())
                .setDone(() -> shooter.isReady());
    }

}
