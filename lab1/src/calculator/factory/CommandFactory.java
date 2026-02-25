package calculator.factory;

import java.io.InputStream;
import java.util.*;

import calculator.command.Command; 
public class CommandFactory {
    private Map<String, String> commands = new HashMap<>(); 

    public CommandFactory(){
        try(InputStream input = CommandFactory.class.getResourceAsStream("commands.properties")){ 
            Properties properties = new Properties(); 
            properties.load(input); 

            for(String key : properties.stringPropertyNames()){ 
                commands.put(key, properties.getProperty(key)); 
            }
        }
        catch(Exception e){
            throw new RuntimeException("Can not load properties");
        }
    }

    public Command create(String commandName){ 
        String className = commands.get(commandName);
            if (className == null)
                throw new RuntimeException("Unknow command"); 
        try {
            Class<?> commandClass = Class.forName(className); 
            return (Command) commandClass.getDeclaredConstructor().newInstance(); 
        } catch (Exception e) { 
            throw new RuntimeException(e);
        }
    }
}

