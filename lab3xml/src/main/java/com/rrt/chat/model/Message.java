package com.rrt.chat.model;

public class Message {
    public enum Type { LOGIN, LOGIN_SUCCESS, ERROR, TEXT, FILE, USER_LIST, ROOM_LIST, LOGOUT, NOTIFICATION, CREATE_ROOM, JOIN_ROOM }

    private Type type;
    private String sender;
    private String target;
    private String content;
    private byte[] fileData;
    private String fileName;
    private String sessionId = ""; // XML

    public Message(Type type, String sender, String target, String content) {
        this.type = type;
        this.sender = sender;
        this.target = target;
        this.content = content;
    }

    public Message(Type type, String sender, String target, byte[] fileData, String fileName) {
        this.type = type;
        this.sender = sender;
        this.target = target;
        this.fileData = fileData;
        this.fileName = fileName;
    }

    public Type getType() { return type; }
    public String getSender() { return sender; }
    public String getTarget() { return target; }
    public String getContent() { return content; }
    public byte[] getFileData() { return fileData; }
    public String getFileName() { return fileName; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String id) { this.sessionId = id; }
}