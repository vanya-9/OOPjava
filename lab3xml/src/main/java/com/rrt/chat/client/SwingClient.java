package com.rrt.chat.client;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

import com.rrt.chat.model.Message;
import com.rrt.chat.model.XmlHelper;

public class SwingClient extends JFrame {
    private JTextPane chatPane;
    private JTextField inputField;
    private JComboBox<String> targetComboBox;
    private JButton sendButton, attachFileButton, createRoomButton, joinRoomButton;

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private String nickname;
    private String sessionId = "";
    private java.util.List<String> activeUsers = new java.util.ArrayList<>();
    private java.util.List<String> activeRooms = new java.util.ArrayList<>();

    public SwingClient(String host, int port) {
        connectToServer(host, port);
    }

    private void connectToServer(String host, int port) {
        try {
            socket = new Socket(host, port);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            boolean registered = false;
            while (!registered) {
                nickname = JOptionPane.showInputDialog(this, "Введите никнейм:", "Авторизация XML", JOptionPane.PLAIN_MESSAGE);
                if (nickname == null || nickname.trim().isEmpty()) System.exit(0);

                // Отправка логина (команда в XML)
                sendToServer(new Message(Message.Type.LOGIN, nickname, "all", ""));

                // Чтение ответа по протоколу: сначала 4 байта длины
                int length = in.readInt();
                byte[] buffer = new byte[length];
                in.readFully(buffer);
                String responseLine = new String(buffer, "UTF-8");

                Message response = XmlHelper.fromXml(responseLine);
                if (response.getType() == Message.Type.LOGIN_SUCCESS) {
                    this.sessionId = response.getSessionId(); // Сохраняем уникальную сессию
                    registered = true;
                } else if (response.getType() == Message.Type.ERROR) {
                    JOptionPane.showMessageDialog(this, response.getContent(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }

            setupGUI();
            startListenerThread();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка подключения: " + e.getMessage());
            System.exit(1);
        }
    }

    private void setupGUI() {
        setTitle("XML Chat - " + nickname);
        setSize(750, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        chatPane = new JTextPane();
        chatPane.setEditable(false);
        add(new JScrollPane(chatPane), BorderLayout.CENTER);

        JPanel topPanel = new JPanel();
        createRoomButton = new JButton("Создать группу");
        joinRoomButton = new JButton("Войти в группу");
        topPanel.add(createRoomButton);
        topPanel.add(joinRoomButton);
        add(topPanel, BorderLayout.NORTH);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel(new BorderLayout());
        
        targetComboBox = new JComboBox<>(new String[]{"all"});
        targetComboBox.setEditable(true);
        
        inputField = new JTextField();
        inputPanel.add(targetComboBox, BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel();
        attachFileButton = new JButton("Файл");
        sendButton = new JButton("Отправить");
        buttonsPanel.add(attachFileButton);
        buttonsPanel.add(sendButton);

        bottomPanel.add(inputPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonsPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendTextMessage());
        inputField.addActionListener(e -> sendTextMessage());
        attachFileButton.addActionListener(e -> sendFileMessage());

        createRoomButton.addActionListener(e -> {
            String r = JOptionPane.showInputDialog(this, "Название группы:");
            if (r != null && !r.isEmpty()) sendToServer(new Message(Message.Type.CREATE_ROOM, nickname, "Server", r));
        });

        joinRoomButton.addActionListener(e -> {
            String r = JOptionPane.showInputDialog(this, "Имя группы (#):");
            if (r != null && !r.isEmpty()) sendToServer(new Message(Message.Type.JOIN_ROOM, nickname, "Server", r));
        });

        setVisible(true);
    }

    private synchronized void sendToServer(Message msg) {
        try {
            msg.setSessionId(sessionId); // Прикрепляем ID сессии к каждому сообщению
            byte[] data = XmlHelper.toXml(msg).getBytes("UTF-8");
            out.writeInt(data.length); // Пишем 4 байта длины (Java int)
            out.write(data);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startListenerThread() {
        new Thread(() -> {
            try {
                while (true) {
                    int length = in.readInt(); // Читаем длину следующего сообщения
                    byte[] buffer = new byte[length];
                    in.readFully(buffer);      // Читаем само тело сообщения
                    String line = new String(buffer, "UTF-8");
                    
                    handleIncomingMessage(XmlHelper.fromXml(line));
                }
            } catch (Exception e) {
                appendMessage("Соединение разорвано.");
            }
        }).start();
    }

    private void sendTextMessage() {
        String text = inputField.getText();
        String target = targetComboBox.getSelectedItem().toString();
        if (!text.trim().isEmpty()) {
            sendToServer(new Message(Message.Type.TEXT, nickname, target, text));
            inputField.setText("");
        }
    }

    private void sendFileMessage() {
        String target = targetComboBox.getSelectedItem().toString();
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                byte[] fileData = Files.readAllBytes(file.toPath());
                sendToServer(new Message(Message.Type.FILE, nickname, target, fileData, file.getName()));
                appendMessage("Вы отправили файл: " + file.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка чтения файла");
            }
        }
    }

    private void handleIncomingMessage(Message msg) {
        SwingUtilities.invokeLater(() -> {
            if (msg.getType() == Message.Type.USER_LIST || msg.getType() == Message.Type.ROOM_LIST) {
                updateComboBox(msg);
            } else if (msg.getType() == Message.Type.ERROR) {
                appendMessage("[ОШИБКА]: " + msg.getContent());
            } else if (msg.getType() == Message.Type.TEXT || msg.getType() == Message.Type.NOTIFICATION) {
                String prefix = msg.getTarget().equals("all") ? "" : "[" + msg.getTarget() + "] ";
                appendMessage(prefix + "[" + msg.getSender() + "]: " + msg.getContent());
            } else if (msg.getType() == Message.Type.FILE) {
                appendFileMessage(msg);
            }
        });
    }

    private void appendMessage(String text) {
        try {
            StyledDocument doc = chatPane.getStyledDocument();
            doc.insertString(doc.getLength(), text + "\n", null);
            chatPane.setCaretPosition(doc.getLength());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void appendFileMessage(Message msg) {
        try {
            StyledDocument doc = chatPane.getStyledDocument();
            doc.insertString(doc.getLength(), "[" + msg.getSender() + "] прислал файл: " + msg.getFileName() + " ", null);

            if (msg.getFileName().toLowerCase().endsWith(".wav")) {
                JButton playBtn = new JButton("▶ Play");
                playBtn.addActionListener(e -> playAudio(msg.getFileData()));
                chatPane.insertComponent(playBtn);
            }

            JButton saveBtn = new JButton("💾 Сохранить");
            saveBtn.addActionListener(e -> saveFile(msg));
            chatPane.insertComponent(saveBtn);

            doc.insertString(doc.getLength(), "\n", null);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updateComboBox(Message msg) {
        // 1. Обновляем нужный список в памяти
        if (msg.getType() == Message.Type.USER_LIST) {
            activeUsers.clear();
            for (String u : msg.getContent().split(",")) {
                if (!u.isEmpty() && !u.equals(nickname)) activeUsers.add(u);
            }
        } else if (msg.getType() == Message.Type.ROOM_LIST) {
            activeRooms.clear();
            for (String r : msg.getContent().split(",")) {
                if (!r.isEmpty()) activeRooms.add(r);
            }
        }

        // 2. Запоминаем текущий выбор пользователя, чтобы он не сбрасывался
        Object current = targetComboBox.getSelectedItem();
        
        // 3. Полностью пересобираем выпадающий список
        targetComboBox.removeAllItems();
        targetComboBox.addItem("all"); // Общий чат всегда первый
        
        for (String u : activeUsers) targetComboBox.addItem(u); // Добавляем личку
        for (String r : activeRooms) targetComboBox.addItem(r); // Добавляем группы

        // 4. Возвращаем выбор, если он всё ещё существует
        if (current != null) {
            targetComboBox.setSelectedItem(current);
        }
    }

    private void playAudio(byte[] audioData) {
        new Thread(() -> { 
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
                AudioInputStream ais = AudioSystem.getAudioInputStream(bais);
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                clip.start();
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void saveFile(Message msg) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(msg.getFileName()));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Files.write(fileChooser.getSelectedFile().toPath(), msg.getFileData());
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SwingClient("localhost", 12345));
    }
}

