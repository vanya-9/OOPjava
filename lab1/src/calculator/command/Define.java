package calculator.command;

import java.util.List;

import calculator.context.ExecutionContext;

public class Define implements Command {
    @Override
    public void execute(ExecutionContext context, List<String> commandArgs){
        String name = commandArgs.get(0);
        double value = Double.parseDouble(commandArgs.get(1));
        context.define(name, value);
    }
    
}
