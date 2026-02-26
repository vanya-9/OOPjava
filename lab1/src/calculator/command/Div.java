package calculator.command;

import java.util.List;

import calculator.context.ExecutionContext;

public class Div implements Command {
    @Override
    public void execute(ExecutionContext context, List<String> commandArgs){
        if (!commandArgs.isEmpty()) {
            throw new IllegalArgumentException("DIV does not accept arguments");
        }
        
        if (context.getStackSize() < 2) {
            throw new RuntimeException("Not enough elements on stack for DIV (need 2, have " + context.getStackSize() + ")");
        }

        Double numberOne = context.pop();
        Double numberTwo = context.pop();

        if (numberTwo != 0.0){
            context.push(numberOne / numberTwo);
        }else{
            throw new ArithmeticException("Division by zero");
        }
    }
}
