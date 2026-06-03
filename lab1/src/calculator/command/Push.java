package calculator.command;

import java.util.List;

import calculator.context.ExecutionContext;

public class Push implements Command {
    @Override
    public void execute(ExecutionContext context, List<String> commandArgs){
        String arg = commandArgs.get(0);

        double value;

        if (context.hasVariable(arg)){
            value = context.getVariable(arg); //смотрим есть ли в variable
        }
        else{
            value = Double.parseDouble(arg); //если нет, то парсим строку
        }
        
        context.push(value);
    } 
}
