package ex1.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import ex1.command.result.AddNewResult;
import ex1.command.result.ComputationTerminated;
import ex1.command.result.ResultCommand;
import ex1.command.result.Subscribe;
import ex1.command.view.FinalResult;
import ex1.command.view.IncrementalResult;
import ex1.command.view.ViewCommand;
import ex1.model.AnalyzedFile;
import ex1.model.Interval;
import ex1.model.Report;
import ex1.model.SetupInfo;

import java.util.*;
import java.util.stream.Collectors;

public class ResultActor extends AbstractBehavior<ResultCommand> {

    final List<ActorRef<ViewCommand>> subscribers = new LinkedList<>();
    private final Set<AnalyzedFile> ranking = new TreeSet<>();
    private final Map<Interval, Integer> distribution = new TreeMap<>();
    private final SetupInfo setupInfo;

    private ResultActor(ActorContext<ResultCommand> context, SetupInfo setupInfo) {
        super(context);
        this.setupInfo = setupInfo;

        if(setupInfo.nIntervals() == 1){
            distribution.put(new Interval(0, Integer.MAX_VALUE), 0);
        }else {
            final int intervalSize = setupInfo.lastIntervalLowerBound() / (setupInfo.nIntervals() - 1);
            for (int i = 0; i < setupInfo.nIntervals() - 2; i++) {
                distribution.put(new Interval(intervalSize * i, intervalSize * (i + 1)), 0);
            }
            distribution.put(new Interval(intervalSize * (setupInfo.nIntervals() - 2), setupInfo.lastIntervalLowerBound()), 0);
            distribution.put(new Interval(setupInfo.lastIntervalLowerBound(), Integer.MAX_VALUE), 0);
        }
    }

    public static Behavior<ResultCommand> create(SetupInfo setupInfo){
        return Behaviors.setup(context -> new ResultActor(context, setupInfo));
    }

    @Override
    public Receive<ResultCommand> createReceive() {
        ReceiveBuilder<ResultCommand> builder = newReceiveBuilder();

        builder.onMessage(Subscribe.class, this::onSubscribe);
        builder.onMessage(AddNewResult.class, this::onAddNewResult);
        builder.onMessage(ComputationTerminated.class, this::onComputationTerminated);

        return builder.build();
    }

    private Behavior<ResultCommand> onComputationTerminated(ComputationTerminated computationTerminated) {
        for(ActorRef<ViewCommand> subscribed : subscribers){
            subscribed.tell(new FinalResult(
                    new Report(
                            ranking.stream().limit(setupInfo.nFiles()).collect(Collectors.toList()),
                            distribution
                    )
            ));
        }

        return this;
    }

    private Behavior<ResultCommand> onSubscribe(Subscribe subscribe){
        subscribers.add(subscribe.subscriber());
        return this;
    }

    private Behavior<ResultCommand> onAddNewResult(AddNewResult addNewResult){
        for(Map.Entry<Interval, Integer> entry : distribution.entrySet()){
            if(entry.getKey().contains(addNewResult.analyzedFile().lines())){
                entry.setValue(entry.getValue() + 1);
            }
        }
        this.ranking.add(addNewResult.analyzedFile());

        for(ActorRef<ViewCommand> subscribed : subscribers){
            subscribed.tell(new IncrementalResult(
                new Report(
                    ranking.stream().limit(setupInfo.nFiles()).collect(Collectors.toList()),
                    distribution
                )
            ));
        }

        return this;
    }

}
