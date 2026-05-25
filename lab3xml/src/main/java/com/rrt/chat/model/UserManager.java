package com.rrt.chat.model;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {
    private static Map<String, User> userMap = new ConcurrentHashMap<>();

    public boolean IsNicknameTaken(String name){
        return userMap.containsKey(name);
    }

    public boolean tryRegister(User user) {
    return userMap.putIfAbsent(user.getNick(), user) == null;
    }

    public void removeUser(String nickname){
        if (nickname != null) userMap.remove(nickname);
    }

    public User getUser(String nickname){
        return userMap.get(nickname);
    }

    public Collection<User> getAllUsers(){
        return userMap.values();
    }
}
