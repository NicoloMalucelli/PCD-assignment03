package ex1.command.result;

import ex1.model.AnalyzedFile;

public record AddNewResult(AnalyzedFile analyzedFile) implements ResultCommand{
}
