package calculator.factory;

import java.io.InputStream;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import calculator.command.Command; 
public class CommandFactory {
    private Map<String, String> commands = new HashMap<>(); 
    private static final Logger logger = Logger.getLogger(CommandFactory.class.getName());

    public CommandFactory(){
        try(InputStream input = CommandFactory.class.getResourceAsStream("commands.properties")){ 
            Properties properties = new Properties(); 
            if(input==null){
                logger.severe("File not found, properties");
                throw new RuntimeException("Can not load properties");
            }
            properties.load(input); 

            for(String key : properties.stringPropertyNames()){ 
                commands.put(key, properties.getProperty(key)); 
            }
        }
        catch(Exception e){
            logger.severe("Mistake working file properties");
        }
    }

    public Command create(String commandName){ 
        String className = commands.get(commandName);
        if (className == null){
            logger.severe("Command not found");
            throw new RuntimeException("Unknow command"); 
        }
                
        try {
            Class<?> commandClass = Class.forName(className); 
            return (Command) commandClass.getDeclaredConstructor().newInstance(); 
        } catch (Exception e) {
            logger.severe("Mistake create command");
            throw new RuntimeException(e);
        }
    }
}

