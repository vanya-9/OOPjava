package calculator.command;

import java.util.List;

import calculator.context.ExecutionContext;

public interface Command {  
    void execute(ExecutionContext context, List<String> commandArgs);
}
