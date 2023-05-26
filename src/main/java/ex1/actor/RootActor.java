package ex1.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import ex1.command.result.ComputationTerminated;
import ex1.command.result.ResultCommand;
import ex1.command.result.Subscribe;
import ex1.command.root.RootCommand;
import ex1.command.root.Start;
import ex1.command.root.Stop;
import ex1.command.scanfolder.ChildTerminated;
import ex1.command.scanfolder.Scan;
import ex1.command.scanfolder.ScanFolderCommand;
import ex1.command.view.ViewCommand;

public class RootActor extends AbstractBehavior<RootCommand> {

    public enum ViewType {CONSOLE, GUI}

    private ActorRef<ViewCommand> viewActor;
    private ActorRef<ResultCommand> resultActor;
    private ActorRef<ScanFolderCommand> scanRootFolderActor;


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
        builder.onMessage(ChildTerminated.class, this::onChildTerminated);
        builder.onMessage(Stop.class, this::onStop);

        return builder.build();
    }

    private Behavior<RootCommand> onChildTerminated(ChildTerminated a) {
        resultActor.tell(new ComputationTerminated());
        return this;
    }

    private Behavior<RootCommand> onStart(Start start){
        this.resultActor = getContext().spawn(ResultActor.create(start.setupInfo()), "resultActor");
        this.resultActor.tell(new Subscribe(this.viewActor));

        this.scanRootFolderActor = getContext().spawn(ScanFolderActor.create(), "rootScanFolderActor");
        this.scanRootFolderActor.tell(new Scan(start.setupInfo().directory()));
        return this;
    }

    private Behavior<RootCommand> onStop(Stop stop){
        scanRootFolderActor.tell(new Stop());
        getContext().stop(scanRootFolderActor);
        getContext().stop(resultActor);
        return this;
    }
}
