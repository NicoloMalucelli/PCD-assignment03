package ex1.command.view;

import ex1.model.Report;

public record IncrementalResult(Report report) implements ViewCommand{

}
