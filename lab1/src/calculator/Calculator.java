package calculator;

import java.io.BufferedReader; 
import java.io.FileReader; 
import java.io.InputStreamReader; 
import java.util.Arrays;
import java.util.List;

import calculator.command.Command;
import calculator.context.ExecutionContext;
import calculator.factory.CommandFactory; 

public class Calculator {
    private static ExecutionContext context = new ExecutionContext();
    private static CommandFactory factory = new CommandFactory(); 
    public static void main(String[] args) throws Exception{ 
        BufferedReader reader;

        if (args.length > 0){
            reader = new BufferedReader(new FileReader(args[0]));
        }
        else{ 
            reader = new BufferedReader(new InputStreamReader(System.in));
        }
        
        String line;

        while (context.shouldContinue() && (line = reader.readLine()) != null) { 
            if (line.isEmpty() || line.startsWith("#")) 
                continue;

            String[] parts = line.split(" "); 
            String name = parts[0];

            List<String> commandArgs = Arrays.asList(parts).subList(1, parts.length);

            Command command = factory.create(name);
            command.execute(context, commandArgs);
        }
    }
}