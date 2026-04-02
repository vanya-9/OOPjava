package calculator.command;

import calculator.context.ExecutionContext;
import java.util.List;

public class Stop implements Command {
    @Override
    public void execute(ExecutionContext context, List<String> commandArgs){
        System.out.println("Stop the working");
        context.stop();
    }
}
