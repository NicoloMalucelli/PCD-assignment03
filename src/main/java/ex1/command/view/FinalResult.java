package ex1.command.view;

import ex1.model.Report;

public record FinalResult(Report report) implements ViewCommand{

}
