package calculator.command;

import java.util.List;

import calculator.context.ExecutionContext;

public class Print implements Command {
    @Override
    public void execute(ExecutionContext context, List<String> commandArgs){
        System.out.println(context.peek());
    }
}
