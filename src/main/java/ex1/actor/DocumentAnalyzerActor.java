package ex1.actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import ex1.command.documentanalyzer.CountLines;
import ex1.command.documentanalyzer.DocumentAnalyzerCommand;
import ex1.command.result.AddNewResult;
import ex1.command.result.ComputationTerminated;
import ex1.command.root.Stop;
import ex1.command.scanfolder.ChildTerminated;
import ex1.command.scanfolder.ScanFolderCommand;
import ex1.model.AnalyzedFile;

public class DocumentAnalyzerActor extends AbstractBehavior<DocumentAnalyzerCommand> {

    private DocumentAnalyzerActor(ActorContext<DocumentAnalyzerCommand> context) {
        super(context);
    }

    public static Behavior<DocumentAnalyzerCommand> create(){
        return Behaviors.setup(DocumentAnalyzerActor::new);
    }

    @Override
    public Receive<DocumentAnalyzerCommand> createReceive() {
        ReceiveBuilder<DocumentAnalyzerCommand> builder = newReceiveBuilder();

        builder.onMessage(CountLines.class, this::onCountLines);

        return builder.build();
    }

    private Behavior<DocumentAnalyzerCommand> onCountLines(CountLines countLines) {
        AnalyzedFile result = new AnalyzedFile(countLines.getDocument().getPath(), countLines.getDocument().countLines());

        getContext().classicActorContext()
                .actorSelection(getContext().getSystem().path() + "/rootActor/resultActor")
                .tell(new AddNewResult(result), getContext().classicActorContext().self());

        getContext().classicActorContext()
                .parent()
                .tell(new ChildTerminated(), getContext().classicActorContext().self());
        return this;
    }

}
