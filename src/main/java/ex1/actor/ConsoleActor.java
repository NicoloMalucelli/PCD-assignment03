package ex1.actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import ex1.command.view.ViewCommand;

public class ConsoleActor extends AbstractBehavior<ViewCommand> {

    private ConsoleActor(ActorContext<ViewCommand> context) {
        super(context);
    }

    public static Behavior<ViewCommand> create(){
        return Behaviors.setup(ConsoleActor::new);
    }

    @Override
    public Receive<ViewCommand> createReceive() {
        return null;
    }




}