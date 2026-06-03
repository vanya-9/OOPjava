package com.rrt.chat.model;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import java.io.StringReader;
import java.util.Base64;

public class XmlHelper {
    
    public static String toXml(Message msg) {
        StringBuilder xml = new StringBuilder();
        
        switch (msg.getType()) {
            case LOGIN:
                xml.append("<command name=\"login\">")
                   .append("<name>").append(msg.getSender()).append("</name>")
                   .append("<type>SwingClient</type>")
                   .append("</command>");
                break;
            case LOGIN_SUCCESS:
                xml.append("<success>")
                   .append("<session>").append(msg.getSessionId()).append("</session>")
                   .append("</success>");
                break;
            case ERROR:
                xml.append("<error>")
                   .append("<message>").append(msg.getContent()).append("</message>")
                   .append("</error>");
                break;
            case NOTIFICATION:
            case TEXT:
                // Для отправки на сервер command, от сервера - event
                if (msg.getSessionId() != null && !msg.getSessionId().isEmpty()) { // От клиента серверу
                    xml.append("<command name=\"message\">")
                       .append("<message>").append(msg.getContent()).append("</message>")
                       .append("<session>").append(msg.getSessionId()).append("</session>")
                       .append("<target>").append(msg.getTarget()).append("</target>") // Расширение
                       .append("</command>");
                } else { // От сервера клиенту
                    xml.append("<event name=\"message\">")
                       .append("<message>").append(msg.getContent()).append("</message>")
                       .append("<name>").append(msg.getSender()).append("</name>")
                       .append("<target>").append(msg.getTarget()).append("</target>") // Расширение
                       .append("</event>");
                }
                break;
            case FILE:
                // Расширение для передачи файлов
                String base64Data = Base64.getEncoder().encodeToString(msg.getFileData());
                if (msg.getSessionId() != null && !msg.getSessionId().isEmpty()) {
                    xml.append("<command name=\"file\">")
                       .append("<filename>").append(msg.getFileName()).append("</filename>")
                       .append("<data>").append(base64Data).append("</data>")
                       .append("<session>").append(msg.getSessionId()).append("</session>")
                       .append("<target>").append(msg.getTarget()).append("</target>")
                       .append("</command>");
                } else {
                    xml.append("<event name=\"file\">")
                       .append("<filename>").append(msg.getFileName()).append("</filename>")
                       .append("<data>").append(base64Data).append("</data>")
                       .append("<name>").append(msg.getSender()).append("</name>")
                       .append("</event>");
                }
                break;
            case USER_LIST:
                xml.append("<success><listusers>");
                for (String u : msg.getContent().split(",")) {
                    if (!u.isEmpty()) xml.append("<user><name>").append(u).append("</name></user>");
                }
                xml.append("</listusers></success>");
                break;
            case JOIN_ROOM:
            case CREATE_ROOM:
                xml.append("<command name=\"").append(msg.getType().toString()).append("\">")
                   .append("<room>").append(msg.getContent()).append("</room>")
                   .append("<session>").append(msg.getSessionId()).append("</session>")
                   .append("</command>");
                break;
            case ROOM_LIST:
                xml.append("<event name=\"rooms\">")
                   .append("<rooms>").append(msg.getContent()).append("</rooms>")
                   .append("</event>");
                break;
            case LOGOUT:
                xml.append("<command name=\"logout\">")
                   .append("<session>").append(msg.getSessionId()).append("</session>")
                   .append("</command>");
                break;
        }
        // Убираем переносы строк, чтобы отправлять 1 команду = 1 строка
        return xml.toString().replaceAll("\n", "").replaceAll("\r", ""); 
    }

    public static Message fromXml(String xmlStr) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlStr)));
            Element root = doc.getDocumentElement();
            String rootName = root.getNodeName();

            if (rootName.equals("command")) {
                String cmdName = root.getAttribute("name");
                if (cmdName.equals("login")) {
                    return new Message(Message.Type.LOGIN, getTag(root, "name"), "Server", "");
                } else if (cmdName.equals("message")) {
                    Message m = new Message(Message.Type.TEXT, "unknown", getTag(root, "target"), getTag(root, "message"));
                    m.setSessionId(getTag(root, "session"));
                    return m;
                } else if (cmdName.equals("file")) {
                    byte[] data = Base64.getDecoder().decode(getTag(root, "data"));
                    Message m = new Message(Message.Type.FILE, "unknown", getTag(root, "target"), data, getTag(root, "filename"));
                    m.setSessionId(getTag(root, "session"));
                    return m;
                } else if (cmdName.equals("CREATE_ROOM")) {
                    Message m = new Message(Message.Type.CREATE_ROOM, "unknown", "Server", getTag(root, "room"));
                    m.setSessionId(getTag(root, "session"));
                    return m;
                } else if (cmdName.equals("JOIN_ROOM")) {
                    Message m = new Message(Message.Type.JOIN_ROOM, "unknown", "Server", getTag(root, "room"));
                    m.setSessionId(getTag(root, "session"));
                    return m;
                } else if (cmdName.equals("logout")) {
                    Message m = new Message(Message.Type.LOGOUT, "unknown", "Server", "");
                    m.setSessionId(getTag(root, "session"));
                    return m;
                }
            } else if (rootName.equals("success")) {
                if (root.getElementsByTagName("session").getLength() > 0) {
                    Message m = new Message(Message.Type.LOGIN_SUCCESS, "Server", "Client", "");
                    m.setSessionId(getTag(root, "session"));
                    return m;
                } else if (root.getElementsByTagName("listusers").getLength() > 0) {
                    NodeList users = root.getElementsByTagName("name");
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < users.getLength(); i++) sb.append(users.item(i).getTextContent()).append(",");
                    return new Message(Message.Type.USER_LIST, "Server", "Client", sb.toString());
                }
            } else if (rootName.equals("error")) {
                return new Message(Message.Type.ERROR, "Server", "Client", getTag(root, "message"));
            } else if (rootName.equals("event")) {
                String eventName = root.getAttribute("name");
                if (eventName.equals("message")) {
                    return new Message(Message.Type.TEXT, getTag(root, "name"), getTag(root, "target"), getTag(root, "message"));
                } else if (eventName.equals("file")) {
                    byte[] data = Base64.getDecoder().decode(getTag(root, "data"));
                    return new Message(Message.Type.FILE, getTag(root, "name"), "Client", data, getTag(root, "filename"));
                } else if (eventName.equals("rooms")) {
                    return new Message(Message.Type.ROOM_LIST, "Server", "Client", getTag(root, "rooms"));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new Message(Message.Type.ERROR, "System", "Client", "Parse error");
    }

    private static String getTag(Element element, String tag) {
        NodeList list = element.getElementsByTagName(tag);
        if (list.getLength() > 0) return list.item(0).getTextContent();
        return "all"; 
    } // возвращает содержимое тега
}