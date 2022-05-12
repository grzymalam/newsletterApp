package zad1.Client;

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

public class ClientLogicHandler {
    private final JTextArea textArea;
    private final JComboBox<String> comboBox;
    private final HashMap<String, String> topicsAndLastNews;
    private final ArrayList<String> topics;
    private SocketChannel socketChannel;

    public ClientLogicHandler(JTextArea textArea, HashMap<String, String> topicsAndLastNews, ArrayList<String> topics) {
        this.textArea = textArea;
        this.topicsAndLastNews = topicsAndLastNews;
        this.topics = topics;
        comboBox = new JComboBox<>();
        InetSocketAddress address = new InetSocketAddress("localhost", 8888);
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(address);

        }catch (UnknownHostException e){
            textArea.setText("Unknown host");
        }catch (Exception e){
            textArea.setText("Unknown error");
        }
    }

    private ByteBuffer buffer = ByteBuffer.allocate(1024);
    private CharBuffer charBuffer = buffer.asCharBuffer();
    private Charset charset = Charset.forName("UTF-8");
    public boolean getMessage() {
        String message = "";
        if (!socketChannel.isConnected()) {
            JOptionPane.showMessageDialog(null, "Connection lost");
            return false;
        }
        while(true) {
            try {
                buffer.clear();
                int read = socketChannel.read(buffer);
                if (read == -1) {
                    JOptionPane.showMessageDialog(null, "Connection lost. (Could not read)");
                } else {
                    buffer.flip();
                    charBuffer = charset.decode(buffer);
                    message = charBuffer.toString();
                    charBuffer.clear();

                    ArrayList<String> messageParts = new ArrayList<>(Arrays.asList(message.split("&")));

                    if (messageParts.get(0).equals("success")) return true;
                    else if (messageParts.get(0).equals("failure")) return false;
                    else if (messageParts.get(0).equals("update")) {
                        topicsAndLastNews.put(messageParts.get(1), messageParts.get(2));
                        textArea.setText(messageParts.get(2));
                        return true;
                    } else {
                        textArea.setText("error");
                        return false;
                    }
                }
            } catch (Exception e) {
                textArea.setText("Unknown error");
            }
        }
    }

    public void subscribe(String topic) {
        sendMessage("subscribe&" + topic);
        if (!getMessage()) {
            JOptionPane.showMessageDialog(null, "Subscription failed");
        }else{
            topics.add(topic);
            JOptionPane.showMessageDialog(null, "Subscription successful");
        }
    }

    public void unsubscribe(String topic) {
        sendMessage("unsubscribe&" + topic);
        if (!getMessage()) {
            JOptionPane.showMessageDialog(null, "Unsubscription failed");
        }else{
            topics.remove(topic);
            comboBox.removeItem(topic);
            JOptionPane.showMessageDialog(null, "Unsubscription successful");
        }
    }

    public void update(String topic){
        sendMessage("update&" + topic);
        if (!getMessage()) {
            JOptionPane.showMessageDialog(null, "Update failed");
        }
        getMessage();
    }

    public void sendMessage(String message) {
        try {
            socketChannel.write(charset.encode(CharBuffer.wrap("client&" + message)));
        } catch (Exception e) {
            textArea.setText("Unknown error");
        }
    }
}
