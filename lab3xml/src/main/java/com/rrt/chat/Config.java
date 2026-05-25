package com.rrt.chat;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private Properties conf = new Properties();

    public Config(String dst) throws IOException{
        try(FileInputStream fis = new FileInputStream(dst)){
            conf.load(fis);
        }
    }

    public int getInt(String target){
        return Integer.parseInt(conf.getProperty(target));
    }

    public boolean getBool(String target){
        return Boolean.parseBoolean(conf.getProperty(target));
    }
}
