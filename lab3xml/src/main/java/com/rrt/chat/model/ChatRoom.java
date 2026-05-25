package com.rrt.chat.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatRoom {
    private final String roomName;
    private final String owner;
    private final Set<String> users;
    private final List<Message> history = new ArrayList<>();
    private static final int MAX_GROUP_HISTORY = 10;

    public ChatRoom(String name, String owner){
        this.roomName = name;
        this.owner = owner;
        this.users = ConcurrentHashMap.newKeySet();
        this.users.add(owner);
    }

    public String getRoomName() { return roomName; }
    public String getOwner() { return owner; }
    public Set<String> getUsersRoom() { return users; }
    public void addUser(String nickname){ users.add(nickname); }
    public void removeUser(String nickname) { users.remove(nickname); }

    public synchronized void addMessage(Message msg) {
        history.add(msg);
        if (history.size() > MAX_GROUP_HISTORY) {
            history.remove(0);
        }
    }

    public synchronized List<Message> getHistory() {
        return new ArrayList<>(history);
    }

    public boolean isUserInRoom(String nickname) {
        return users.contains(nickname);
    }
}