package ex1.command.scanfolder;

import ex1.model.Folder;

import java.io.File;
import java.io.IOException;

public class Scan implements ScanFolderCommand{
    private final Folder startDirectory;
    public Scan (String startDirectoryPath){
        try {
            this.startDirectory = Folder.fromDirectory(new File(startDirectoryPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Folder getStartDirectory(){
        return this.startDirectory;
    }

}
