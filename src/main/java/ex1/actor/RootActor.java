package ex1.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import ex1.command.result.ResultCommand;
import ex1.command.result.Subscribe;
import ex1.command.root.RootCommand;
import ex1.command.root.Start;
import ex1.command.view.ViewCommand;

public class RootActor extends AbstractBehavior<RootCommand> {

    public enum ViewType {CONSOLE, GUI}

    private ActorRef<ViewCommand> viewActor;

    private RootActor(ActorContext<RootCommand> context, ViewType viewType) {
        super(context);
        switch (viewType){
            case CONSOLE -> this.viewActor = context.spawn(ConsoleActor.create(), "ConsoleActor");
            case GUI -> this.viewActor = context.spawn(GuiActor.create(), "GuiActor");
        }
    }

    public static Behavior<RootCommand> create(ViewType viewType){
        return Behaviors.setup(context -> new RootActor(context, viewType));
    }

    @Override
    public Receive<RootCommand> createReceive() {
        ReceiveBuilder<RootCommand> builder = newReceiveBuilder();
        builder.onMessage(Start.class, this::onStart);
        return builder.build();
    }

    private Behavior<RootCommand> onStart(Start start){
        ActorRef<ResultCommand> resultActor = getContext().spawn(ResultActor.create(start.setupInfo()), "resultActor");
        resultActor.tell(new Subscribe(this.viewActor));
        return this;
    }
}
