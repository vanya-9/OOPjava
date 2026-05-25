package com.rrt.chat.server;

import com.rrt.chat.Config;
import com.rrt.chat.model.UserManager;

public class Main {
    public static void main(String[] args) throws Exception{
        UserManager userManager = new UserManager();
        try{
            Config configServer = new Config("src/main/resources/config.properties");
            Server serverChat = new Server(configServer, userManager);
            serverChat.work();

        } catch(Exception e){
            System.err.println("Ошибка старта сервера: " + e.getMessage());
            throw  new Exception("cannot open config");
        }


        
    }
}
