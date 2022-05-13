package zad1.Admin;

import javax.swing.*;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class AdminLogicHandler {
    private final JTextArea messageArea;
    private final JTextArea topicArea;
    private final JComboBox<String> comboBox;
    private SocketChannel socketChannel;

    public AdminLogicHandler(JTextArea messageArea, JTextArea topicArea, JComboBox<String> comboBox) {
        this.messageArea = messageArea;
        this.topicArea = topicArea;
        this.comboBox = comboBox;
        reconnect();

    }

    private ByteBuffer buffer = ByteBuffer.allocate(1024);
    private CharBuffer charBuffer = buffer.asCharBuffer();
    private final Charset charset = Charset.forName("ISO-8859-2");
    public boolean getMessage() {
        String message = "";
        if (!socketChannel.isConnected()) {
            System.out.println("Admin disconnected");
            JOptionPane.showMessageDialog(null, "Connection lost");
            return false;
        }
        while(true) {
            try {
                buffer.clear();
                int read = socketChannel.read(buffer);
                if (read == 0) {
                    continue;
                }
                if (read == -1) {
                    JOptionPane.showMessageDialog(null, "Connection lost. (Could not read)");
                    break;
                } else {
                    buffer.flip();
                    charBuffer = charset.decode(buffer);
                    message = charBuffer.toString();
                    System.out.println("Admin received: " + message);
                    charBuffer.clear();

                    ArrayList<String> messageParts = new ArrayList<>(Arrays.asList(message.split("&")));

                    if (messageParts.get(0).equals("success")) {
                        System.out.println("returning true");
                        return true;
                    }
                    else if (messageParts.get(0).equals("failure")) {
                        System.out.println("returning false");
                        return false;
                    }
                    else if (messageParts.get(0).equals("get")) {
                        System.out.println("getting topics");
                        if (messageParts.get(2).equals("null")){
                            JOptionPane.showMessageDialog(null, "No topics loaded");
                            return false;
                        }
                        for(int i = 1; i < messageParts.size(); i++) {
                            comboBox.addItem(messageParts.get(i));
                        }
                        return true;
                    } else {
                        messageArea.setText("error");
                        return false;
                    }
                }
            } catch (Exception e) {
                messageArea.setText("Unknown error");
                reconnect();
            }
            return false;
        }
        return false;
    }

    public void sendNews(String topic, String news) {
        if (topic.equals("")) {
            JOptionPane.showMessageDialog(null, "Topic cannot be empty");
        }
        if(news.equals("")) {
            JOptionPane.showMessageDialog(null, "News cannot be empty");
        }
        sendMessage("news&" + topic + "&" + news);
        if (!getMessage()) {
            JOptionPane.showMessageDialog(null, "Sending news failed");
        }else{
            JOptionPane.showMessageDialog(null, "Sending news successful");
        }
    }

    public void add(String topic) {
        sendMessage("add&" + topic);
        if (!getMessage()) {
            JOptionPane.showMessageDialog(null, "Addition failed");
        }else{
            comboBox.addItem(topic);
            JOptionPane.showMessageDialog(null, "Addition successful");
        }
    }

    public void delete(String topic){
        sendMessage("delete&" + topic);
        if (!getMessage()) {
            JOptionPane.showMessageDialog(null, "Deletion failed");
        }else{
            JOptionPane.showMessageDialog(null, "Deletion successful");
        }
        getMessage();
    }

    public void change(String topic, String newTopic) {
        sendMessage("change&" + topic + "&" + newTopic);
        if (!getMessage()) {
            JOptionPane.showMessageDialog(null, "Changing failed");
        }else{
            comboBox.removeItem(topic);
            comboBox.addItem(newTopic);
            JOptionPane.showMessageDialog(null, "Changing successful");
        }
    }

    public void get(){
        sendMessage("get");
        if (!getMessage()) {
            JOptionPane.showMessageDialog(null, "Getting topics failed");
        }
    }
    public void sendMessage(String message) {
        System.out.println("Admin sending: " + message);
        ByteBuffer msg = charset.encode(CharBuffer.wrap("admin&" + message + '\n'));
        while(true) {
            try {
                socketChannel.write(msg);
                return;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Could not send message, trying to reconnect");
                reconnect();
            }
        }
    }

    private void reconnect() {
        while(true) {
            if(socketChannel != null) {
                try {
                    socketChannel.close();
                } catch (Exception e) {
                    System.out.println("Could not close socket");
                }
            }
            try {
                InetSocketAddress address = new InetSocketAddress("localhost", 8888);
                try {
                    socketChannel = SocketChannel.open();
                    socketChannel.configureBlocking(false);
                    socketChannel.connect(address);
                    while (!socketChannel.finishConnect()) {
                    }
                    System.out.println("Admin connected");
                    return;
                } catch (UnknownHostException e) {
                    JOptionPane.showMessageDialog(null, "Unknown host");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Unknown error");
                }
            } catch (Exception e) {
                System.out.println("Could not open socket");
            }
        }
    }
}
