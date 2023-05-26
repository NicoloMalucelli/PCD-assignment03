package ex1.command.root;

import ex1.model.SetupInfo;

public record Start(SetupInfo setupInfo) implements RootCommand{
}