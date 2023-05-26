package ex1.command.scanfolder;

import ex1.command.root.RootCommand;

public record ChildTerminated() implements ScanFolderCommand, RootCommand {
}
