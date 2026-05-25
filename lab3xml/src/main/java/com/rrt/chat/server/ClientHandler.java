package com.rrt.chat.server;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

import com.rrt.chat.model.Message;
import com.rrt.chat.model.User;
import com.rrt.chat.model.UserManager;
import com.rrt.chat.model.XmlHelper;

public class ClientHandler implements Runnable {
    private Socket socket;
    private UserManager userManager;
    private DataInputStream in; 
    private DataOutputStream out;
    private final Server server;

    private long lastMessageTime = 0;
    private static final long RATE_LIMIT_MS = 500;
    private String nickname;
    private String currentSessionId;
    User newUser;

    public ClientHandler(Socket socket, Server server, UserManager usManager) throws IOException {
        this.socket = socket;
        this.userManager = usManager;
        this.server = server;
    }

    // Вспомогательный метод для отправки в этот конкретный сокет
    private void sendMsg(Message msg) throws IOException {
        byte[] data = XmlHelper.toXml(msg).getBytes("UTF-8");
        out.writeInt(data.length);
        out.write(data);
        out.flush();
    }

    @Override
    public void run() {
        try {
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            while (nickname == null) {
                // Читаем по ТЗ: 4 байта длины, потом массив байтов
                int length = in.readInt();
                byte[] buffer = new byte[length];
                in.readFully(buffer);
                String line = new String(buffer, "UTF-8");
                
                Message authMessage = XmlHelper.fromXml(line);
                if (authMessage.getType() == Message.Type.LOGIN) {
                    String requestedNick = authMessage.getSender();
                    currentSessionId = UUID.randomUUID().toString(); 
                    
                    this.newUser = new User(requestedNick, socket, out, currentSessionId);

                    if (userManager.tryRegister(newUser)) {
                        this.nickname = requestedNick;
                        Message successMsg = new Message(Message.Type.LOGIN_SUCCESS, "Server", nickname, "");
                        successMsg.setSessionId(currentSessionId);
                        sendMsg(successMsg);
                    } else {
                        sendMsg(new Message(Message.Type.ERROR, "Server", requestedNick, "Ник занят"));
                    }
                }
            }

            if (nickname == null) return;
            
            for (Message msg : server.messageHistory) sendMsg(msg);
            server.broadcast(new Message(Message.Type.NOTIFICATION, "Server", "all", nickname + " присоединился к чату."));
            server.broadcastOnlineLists();

            while (true) {
                // Читаем по ТЗ: 4 байта длины, потом массив байтов
                int length = in.readInt();
                byte[] buffer = new byte[length];
                in.readFully(buffer);
                String line = new String(buffer, "UTF-8");

                Message message = XmlHelper.fromXml(line);
                
                if (!currentSessionId.equals(message.getSessionId())) {
                    sendMsg(new Message(Message.Type.ERROR, "Server", nickname, "Неверная сессия!"));
                    continue;
                }

                message = new Message(message.getType(), nickname, message.getTarget(), message.getContent());
                if (message.getType() == Message.Type.FILE) {
                     message = new Message(Message.Type.FILE, nickname, message.getTarget(), XmlHelper.fromXml(line).getFileData(), XmlHelper.fromXml(line).getFileName());
                }

                long currentTime = System.currentTimeMillis();
                if (currentTime - lastMessageTime < RATE_LIMIT_MS) {
                    sendMsg(new Message(Message.Type.ERROR, "Server", nickname, "Слишком часто!"));
                    continue;
                }
                lastMessageTime = currentTime;

                if (message.getType() == Message.Type.LOGOUT) break;
                else if (message.getType() == Message.Type.CREATE_ROOM) server.createRoom(message.getContent(), nickname);
                else if (message.getType() == Message.Type.JOIN_ROOM) server.joinRoom(message.getContent(), nickname);
                else {
                    String target = message.getTarget();
                    if (target.equalsIgnoreCase("all")) server.broadcast(message);
                    else if (server.isRoom(target)) server.sendToRoom(target, message);
                    else {
                        User targetUser = userManager.getUser(target);
                        if (targetUser != null) {
                            targetUser.sendMessage(message);
                            if (!target.equals(nickname)) sendMsg(message);
                        } else {
                            sendMsg(new Message(Message.Type.ERROR, "Server", nickname, "Юзер оффлайн."));
                        }
                    }
                }
            }
        } catch (EOFException e) {
            // Нормальное отключение сокета
        } catch (Exception e) {
            System.err.println("Отключение/Ошибка: " + nickname);
        } finally {
            closeConnections();
        }
    }

    private void closeConnections() {
        if (nickname != null) {
            userManager.removeUser(nickname);
            server.broadcast(new Message(Message.Type.NOTIFICATION, "Server", "all", nickname + " покинул чат."));
            server.broadcastOnlineLists();
        }
        try { if (in != null) in.close(); if (out != null) out.close(); if (socket != null) socket.close(); } catch(IOException e) {}
    }
}