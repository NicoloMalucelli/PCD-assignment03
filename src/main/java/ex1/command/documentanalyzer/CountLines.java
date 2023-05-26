package ex1.command.documentanalyzer;

import ex1.model.Document;
import ex1.model.Folder;

import java.io.File;
import java.io.IOException;

public class CountLines implements DocumentAnalyzerCommand{
    private final Document document;
    public CountLines(String path){
        try {
            this.document = Document.fromFile(new File(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Document getDocument(){
        return this.document;
    }
}
