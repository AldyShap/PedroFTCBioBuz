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


@Autonomous(name="RedAuto")
public class BioBuzzRedAuto  extends OpMode {

    private Follower follower;
    private Shooter shooter;
    private Intake intake;
    private final PoseFactory p = PoseFactory.degrees();


    // Need to change the poses
    private Pose startPose = p.of(57.112, 9.112,90);
    private Pose parkPose = p.of(10.67878192534382, 105.95776031434183, 0);

    private Pose shootPose = p.of(57.7, 31.6, 90);

    private Pose controlPose = p.of(21.486247544204314, 36.362475442043205, 0);

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
        return curve(shootPose, controlPose, parkPose).linear(startPose, parkPose);
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
                        .setStart(() -> {
                            intake.feedOff();
                            shooter.stop();
                        })
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
