package com.rrt.chat.server;
import java.io.*;
// import java.io.ObjectInputFilter.Config;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.rrt.chat.Config;
import com.rrt.chat.model.ChatRoom;
import com.rrt.chat.model.Message;
import com.rrt.chat.model.User;
import com.rrt.chat.model.UserManager;



public class Server{
    private static ServerSocket server; 
    private final ExecutorService executor;
    private final int port;
    private final boolean enableLogging;

    public List<Message> messageHistory = new CopyOnWriteArrayList<>();
    private static final int HISTORY_SIZE = 10;

    private static UserManager userManager;
    private final Map<String, ChatRoom> chatRooms = new ConcurrentHashMap<>();

    public Server(Config cfg, UserManager usManager){
        this.port = cfg.getInt("serverPort");
        this.enableLogging = cfg.getBool("logEnabled");
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.userManager = usManager;

        
    }

    public void work()throws IOException{
        server = new ServerSocket(port);

        try{
            while(true){
            
                Socket socket = server.accept();
                socket.setSoTimeout(1800000);
                ClientHandler handler = new ClientHandler(socket, this, userManager);
                executor.submit(handler);

                
            }
        } finally{
            server.close();
        }
    }

    public synchronized void broadcast(Message message){
        if(message.getType() == Message.Type.TEXT || message.getType() == Message.Type.FILE){
            messageHistory.add(message);
            if (messageHistory.size() > HISTORY_SIZE){
                messageHistory.remove(0);
            }
        }

        for(User user : userManager.getAllUsers()){
            user.sendMessage(message);
        }
    }

    public void sendToUser(String targetNick, Message message){
        User user = userManager.getUser(targetNick);
        if (user != null){
            user.sendMessage(message);
        }
    }

    
    public void sendToRoom(String roomName, Message message) {
    ChatRoom room = chatRooms.get(roomName);
        if (room != null) {
            if (!room.isUserInRoom(message.getSender())) {
                sendToUser(message.getSender(), new Message(Message.Type.ERROR, "Server", message.getSender(), 
                    "Ошибка: Вы не участник группы " + roomName));
                return;
            }

            room.addMessage(message);

            for (String memberNick : room.getUsersRoom()) {
                sendToUser(memberNick, message);
            }
        }
    }


    public synchronized void createRoom(String roomName, String ownerNickname){
        String formatedRoomName = "#" + roomName;
        if(!chatRooms.containsKey(formatedRoomName)){
            chatRooms.put(formatedRoomName, new ChatRoom(formatedRoomName, ownerNickname));
            User u = userManager.getUser(ownerNickname);
            if (u != null) u.joinRoom(formatedRoomName);
            sendToUser(ownerNickname, new Message(Message.Type.NOTIFICATION, "Server", ownerNickname, "Группа " + formatedRoomName + " создана."));
            broadcastOnlineLists();
        } else{
            sendToUser(ownerNickname, new Message(Message.Type.ERROR, "Server", ownerNickname, "Группа с таким именем уже существует."));
        }
    }

    public void broadcastOnlineLists(){ //кому написать, куда войти
        StringBuilder sbUsers = new StringBuilder();
        for(User u : userManager.getAllUsers()) sbUsers.append(u.getNick()).append(",");
        broadcast(new Message(Message.Type.USER_LIST, "Server", "all", sbUsers.toString()));

        StringBuilder sbRooms = new StringBuilder();
        for (String room : chatRooms.keySet()) sbRooms.append(room).append(",");
        broadcast(new Message(Message.Type.ROOM_LIST, "Server", "all", sbRooms.toString()));
    }

    public synchronized void joinRoom(String roomName, String nickname) {
    String formattedName = roomName.startsWith("#") ? roomName : "#" + roomName;
    ChatRoom room = chatRooms.get(formattedName);
    
    if (room != null) {
        if (room.isUserInRoom(nickname)) {
            sendToUser(nickname, new Message(Message.Type.ERROR, "Server", nickname, "Вы уже в этой группе."));
            return;
        }

        room.addUser(nickname);
        User u = userManager.getUser(nickname);
        if (u != null) u.joinRoom(formattedName);
        
        //вступил
        sendToUser(nickname, new Message(Message.Type.NOTIFICATION, "Server", nickname, "Вы вступили в " + formattedName));

        // история
        for (Message histMsg : room.getHistory()) {
            sendToUser(nickname, histMsg);
        }
        
    } else {
        sendToUser(nickname, new Message(Message.Type.ERROR, "Server", nickname, "Группа " + formattedName + " не найдена."));
    }
}

    public boolean isRoom(String name) {
        return chatRooms.containsKey(name);
    }


}


