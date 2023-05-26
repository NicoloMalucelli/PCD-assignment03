package ex1.actor;

import akka.actor.Actor;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import ex1.command.root.Start;
import ex1.command.view.FinalResult;
import ex1.command.view.IncrementalResult;
import ex1.command.view.ViewCommand;
import ex1.model.AnalyzedFile;
import ex1.model.Interval;
import ex1.model.Report;
import ex1.model.SetupInfo;
import utils.Strings;

import java.util.Map;
import java.util.Scanner;

public class ConsoleActor extends AbstractBehavior<ViewCommand> {

    private ConsoleActor(ActorContext<ViewCommand> context) {
        super(context);

        getContext().classicActorContext().parent().tell(
                new Start(this.getSetupInfo()),
                getContext().classicActorContext().self());

    }

    private SetupInfo getSetupInfo(){
        Scanner scanner = new Scanner(System.in);

        getContext().getLog().info("Root directory: ");
        final String dir = scanner.nextLine();

        String tmp;
        do{
            getContext().getLog().info("Number of files to visualize: ");
            tmp = scanner.nextLine();
        }while (!Strings.isNumeric(tmp) || Integer.parseInt(tmp) <= 0);
        final Integer nFiles = Integer.parseInt(tmp);

        do{
            getContext().getLog().info("Number of intervals: ");
            tmp = scanner.nextLine();
        }while (!Strings.isNumeric(tmp) || Integer.parseInt(tmp) <= 0);
        final Integer nIntervals = Integer.parseInt(tmp);

        do{
            getContext().getLog().info("Last interval max: ");
            tmp = scanner.nextLine();
        }while (!Strings.isNumeric(tmp) || Integer.parseInt(tmp) <= 0);
        final Integer lastInterval = Integer.parseInt(tmp);

        return new SetupInfo(dir, nFiles, nIntervals, lastInterval);
    }

    public static Behavior<ViewCommand> create(){
        return Behaviors.setup(ConsoleActor::new);
    }

    @Override
    public Receive<ViewCommand> createReceive() {
        ReceiveBuilder<ViewCommand> builder = newReceiveBuilder();

        builder.onMessage(IncrementalResult.class, this::onIncrementalResult);
        builder.onMessage(FinalResult.class, this::onFinalResult);

        return builder.build();
    }

    private Behavior<ViewCommand> onFinalResult(FinalResult finalResult) {
        Report finalReport = finalResult.report();

        getContext().getLog().info("Files ranking:");
        for(AnalyzedFile result : finalReport.ranking()){
            getContext().getLog().info(result.filePath() + " has: " + result.lines() + " lines.");
        }
        getContext().getLog().info("\nFiles distribution:");
        for(Map.Entry<Interval, Integer> entry : finalReport.distribution().entrySet()){
            getContext().getLog().info(entry.getKey() + " : " + entry.getValue());
        }
        return this;
    }

    private Behavior<ViewCommand> onIncrementalResult(IncrementalResult a) {
        return this;
    }


}