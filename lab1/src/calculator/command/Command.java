package calculator.command;

import java.util.List;

import calculator.context.ExecutionContext;

public interface Command {  //важен контракт, а не общая реализация метода, у всех свой метод, но нужен контракт!
    void execute(ExecutionContext context, List<String> commandArgs);
}
