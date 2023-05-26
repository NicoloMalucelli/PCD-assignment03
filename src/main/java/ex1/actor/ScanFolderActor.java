package ex1.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import ex1.command.documentanalyzer.CountLines;
import ex1.command.documentanalyzer.DocumentAnalyzerCommand;
import ex1.command.root.RootCommand;
import ex1.command.root.Stop;
import ex1.command.scanfolder.ChildTerminated;
import ex1.command.scanfolder.Scan;
import ex1.command.scanfolder.ScanFolderCommand;
import ex1.model.Document;
import ex1.model.Folder;

import java.util.LinkedList;
import java.util.List;

public class ScanFolderActor extends AbstractBehavior<ScanFolderCommand> {

    private int nAliveChild;
    private boolean allChildStarted;
    private final List<ActorRef<ScanFolderCommand>> subfolders = new LinkedList<>();
    private final List<ActorRef<DocumentAnalyzerCommand>> documents = new LinkedList<>();
    private ScanFolderActor(ActorContext<ScanFolderCommand> context) {
        super(context);
    }

    public static Behavior<ScanFolderCommand> create(){
        return Behaviors.setup(ScanFolderActor::new);
    }

    @Override
    public Receive<ScanFolderCommand> createReceive() {
        ReceiveBuilder<ScanFolderCommand> builder = newReceiveBuilder();

        builder.onMessage(Scan.class, this::onScan);
        builder.onMessage(ChildTerminated.class, this::onChildTerminated);
        builder.onMessage(Stop.class, this::onStop);

        return builder.build();
    }

    private Behavior<ScanFolderCommand> onChildTerminated(ChildTerminated childTerminated) {
        nAliveChild--;
        if(allChildStarted && nAliveChild == 0){
            this.terminate();
        }
        return this;
    }

    private void terminate(){
        getContext().classicActorContext()
                .parent()
                .tell(new ChildTerminated(), getContext().classicActorContext().self());
    }

    private Behavior<ScanFolderCommand> onScan(Scan scan) {
        Folder folder = scan.getStartDirectory();
        for(Folder subFolder : folder.getSubFolders()){
            ActorRef<ScanFolderCommand> scanFolderActor = getContext().spawn(ScanFolderActor.create(), subFolder.getName().replaceAll("[^A-Za-z0-9]", "_"));
            subfolders.add(scanFolderActor);
            scanFolderActor.tell(new Scan(subFolder.getPath()));
            nAliveChild++;
        }
        for(Document doc : folder.getDocuments()){
            ActorRef<DocumentAnalyzerCommand> documentAnalyzerActor = getContext().spawn(DocumentAnalyzerActor.create(), doc.getName().replaceAll("[^A-Za-z0-9]", "_"));
            documents.add(documentAnalyzerActor);
            documentAnalyzerActor.tell(new CountLines(doc.getPath()));
            nAliveChild++;
        }

        if(nAliveChild == 0){
            this.terminate();
        }

        allChildStarted = true;
        return this;
    }

    private Behavior<ScanFolderCommand> onStop(Stop stop){
        subfolders.forEach(folder -> {
            folder.tell(new Stop());
            this.getContext().stop(folder);
        });
        documents.forEach(document -> {
            this.getContext().stop(document);
        });
        return this;
    }
}
