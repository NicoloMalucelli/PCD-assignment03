package ex1.command.result;

import akka.actor.typed.ActorRef;
import ex1.command.view.ViewCommand;

public record Subscribe(ActorRef<ViewCommand> subscriber) implements ResultCommand {

}
