package ex1.actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import ex1.command.view.ViewCommand;

public class GuiActor extends AbstractBehavior<ViewCommand> {
    private GuiActor(ActorContext<ViewCommand> context) {
        super(context);
    }

    public static Behavior<ViewCommand> create(){
        return Behaviors.setup(GuiActor::new);
    }

    @Override
    public Receive<ViewCommand> createReceive() {
        return null;
    }
}
