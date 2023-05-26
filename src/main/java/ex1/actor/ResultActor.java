package ex1.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import ex1.command.result.AddNewResult;
import ex1.command.result.ResultCommand;
import ex1.command.result.Subscribe;
import ex1.command.view.ViewCommand;
import ex1.model.SetupInfo;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ResultActor extends AbstractBehavior<ResultCommand> {

    final List<ActorRef<ViewCommand>> subscribers = new LinkedList<>();
    private final SetupInfo setupInfo;

    private ResultActor(ActorContext<ResultCommand> context, SetupInfo setupInfo) {
        super(context);
        this.setupInfo = setupInfo;
    }

    public static Behavior<ResultCommand> create(SetupInfo setupInfo){
        return Behaviors.setup(context -> new ResultActor(context, setupInfo));
    }

    @Override
    public Receive<ResultCommand> createReceive() {
        ReceiveBuilder<ResultCommand> builder = newReceiveBuilder();

        builder.onMessage(Subscribe.class, this::onSubscribe);
        builder.onMessage(AddNewResult.class, this::addNewResult);

        return builder.build();
    }

    private Behavior<ResultCommand> onSubscribe(Subscribe subscribe){
        subscribers.add(subscribe.subscriber());
        return this;
    }

    private Behavior<ResultCommand> addNewResult(AddNewResult addNewResult){

        return this;
    }
}
