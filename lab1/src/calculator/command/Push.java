package calculator.command;

import java.util.List;

import calculator.context.ExecutionContext;

public class Push implements Command {
    @Override
    public void execute(ExecutionContext context, List<String> commandArgs){
        String arg = commandArgs.get(0);

        double value;

        if (context.hasVariable(arg)){
            value = context.getVariable(arg);
        }
        else{
            value = Double.parseDouble(arg);
        }
        
        context.push(value);
    } 
}
