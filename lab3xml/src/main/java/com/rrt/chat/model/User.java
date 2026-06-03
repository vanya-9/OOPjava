package com.rrt.chat.model;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class User {
    private final String nickname;
    private final Socket socket;
    private final DataOutputStream out;
    private final String sessionId;
    private final Set<String> chatRooms = ConcurrentHashMap.newKeySet();

    public User(String name, Socket sock, DataOutputStream out, String sessionId) {
        this.nickname = name;
        this.socket = sock;
        this.out = out;
        this.sessionId = sessionId;
    }

    public String getNick() { return nickname; }
    public String getSessionId() { return sessionId; }
    
    public void joinRoom(String roomName) { chatRooms.add(roomName); }
    public void leaveRoom(String roomName) { chatRooms.remove(roomName); }
    public Set<String> getRooms() { return chatRooms; }

    public void sendMessage(Message msg) {
        try {
            byte[] data = XmlHelper.toXml(msg).getBytes("UTF-8");
            synchronized(out){
                out.writeInt(data.length); 
                out.write(data);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}