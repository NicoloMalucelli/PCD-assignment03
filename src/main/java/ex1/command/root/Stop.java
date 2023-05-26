package ex1.command.root;

import ex1.command.documentanalyzer.DocumentAnalyzerCommand;
import ex1.command.scanfolder.ScanFolderCommand;

public record Stop() implements RootCommand, ScanFolderCommand {
}
