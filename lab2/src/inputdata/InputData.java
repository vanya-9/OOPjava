package src.inputdata;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class InputData {
    private final Properties prop = new Properties();
    
    public InputData(String path) throws IOException{
        try(FileInputStream fis = new FileInputStream(path)){
            prop.load(fis);
        }
    }

    public int getInt(String key){
        return Integer.parseInt(prop.getProperty(key));
    }

    public boolean getBoolean(String key){
        return Boolean.parseBoolean(prop.getProperty(key));
    }
}
