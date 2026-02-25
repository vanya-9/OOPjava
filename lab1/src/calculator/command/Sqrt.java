package calculator.command;

import java.util.List;

import calculator.context.ExecutionContext;

public class Sqrt implements Command {
    @Override
    public void execute(ExecutionContext context, List<String> commandArgs){
        double a = context.pop();
        context.push(Math.sqrt(a));
    }
}
