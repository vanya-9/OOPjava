package calculator.command;

import java.util.List;

import calculator.context.ExecutionContext;

public class Mul implements Command {
    @Override
    public void execute(ExecutionContext context, List<String> commandArgs){
        if (!commandArgs.isEmpty()) {
            throw new IllegalArgumentException("MUL does not accept arguments");
        }
        
        if (context.getStackSize() < 2) {
            throw new RuntimeException("Not enough elements on stack for MUL (need 2, have " + context.getStackSize() + ")");
        }

        Double numberOne = context.pop();
        Double numberTwo = context.pop();
        
        context.push(numberTwo * numberOne);
        
    }
}
