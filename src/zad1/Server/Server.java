package zad1.Server;

import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;

public class Server {
    private static final int PORT = 8888;
    private Selector selector;
    private final HashMap<String, List<News>> topics = new HashMap<>();
    private final Map<SocketChannel, List<String>> clients = new HashMap<>();
    private ServerSocketChannel serverSocketChannel;

    public Server() {
        topics.put("sport", new ArrayList<>());
        topics.put("tech", new ArrayList<>());
        topics.get("sport").add(new News("sport is cool"));
        topics.get("sport").add(new News("sport is cool 2"));
        topics.get("tech").add(new News("tech is cool"));
        topics.get("tech").add(new News("tech is cool 2"));
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(PORT));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Server started");
        run();
    }

    private void run(){
        boolean run = true;
        while (run) {
            try {
                //przyjecie polaczenia
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    if (key.isAcceptable()) { //nowy klient
                        SocketChannel client = serverSocketChannel.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                        clients.put(client, new ArrayList<>());
                        System.out.println("New client connected");
                        continue;
                    }

                    if (key.isReadable()) { //klient wysyla dane
                        SocketChannel client = (SocketChannel) key.channel();
                        handleRequest(client);
                        System.out.println("Client sent data");
                        continue;
                    }
                }
            }catch (IOException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final int BUFFER_SIZE = 1024;

    private ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    private StringBuffer stringBuffer = new StringBuffer();

    private void handleRequest(SocketChannel client) {
        if(!client.isOpen()) return;

        stringBuffer.setLength(0);
        byteBuffer.clear();

        try{
            petla:
            while(true){
                int read = client.read(byteBuffer);
                if(read > 0){
                    byteBuffer.flip();
                    CharBuffer charBuffer = CHARSET.decode(byteBuffer);
                    while (charBuffer.hasRemaining()) {
                        char c = charBuffer.get();
                        if (c == '\n') break petla;
                        stringBuffer.append(c);
                    }
                }
            }
            //admin
            //message/add/delete/change

            //client
            //subscribe/unsubscribe/get <tematy>

            //cala magia
            ArrayList<String> requestContent = new ArrayList<>(Arrays.asList(stringBuffer.toString().split("&")));
            String requestSource = requestContent.get(0); //admin albo client

            String command = requestContent.get(1);
            switch (requestSource){
                case "admin":
                    switch (command){
                        case "message":
                            String topic = requestContent.get(2);
                            String message = requestContent.get(3);
                            topics.putIfAbsent(topic, new ArrayList<>());
                            topics.get(topic).add(new News(message));
                            break;
                        case "add":
                            String topicName = requestContent.get(2);
                            topics.putIfAbsent(topicName, new ArrayList<>());
                            break;
                        case "delete":
                            String topicNameToDelete = requestContent.get(2);
                            topics.remove(topicNameToDelete);
                            break;
                        case "change":
                            String topicNameToChange = requestContent.get(2);
                            String newTopicName = requestContent.get(3);
                            topics.put(newTopicName, topics.get(topicNameToChange));
                            break;
                    }
                    break;

                case "client":
                    String topic;
                    switch (command){
                        case "subscribe":
                            topic = requestContent.get(2);
                            if (topics.containsKey(topic)) {
                                clients.get(client).add(topic);
                                sendMessage(client, "success");
                            }else{
                                sendMessage(client, "fail");
                            }
                            break;
                        case "unsubscribe":
                            topic = requestContent.get(2);
                            if (topics.containsKey(topic)) {
                                if(clients.get(client).remove(requestContent.get(2))){
                                    sendMessage(client, "success");
                                }else{
                                    sendMessage(client, "fail");
                                }
                            }else{
                                sendMessage(client, "fail");
                            }
                            break;
                        case "update":
                            String topicToGetNewsFor = requestContent.get(2);
                            for (String t : clients.get(client)) {
                                if(t.equals(topicToGetNewsFor)){
                                    topics.get(topicToGetNewsFor).stream().max(Comparator.comparing(News::getId)).ifPresent(news -> sendMessage(client, "update&" + topicToGetNewsFor + "&" + news.getData()));
                                }
                            }
                            break;
                    }
                    break;
                default:
                    System.out.println("Unknown request");
            }

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private StringBuffer messageBuffer = new StringBuffer();

    //wysylanie danych do klienta
    private void sendMessage(SocketChannel client, String message){
        messageBuffer.setLength(0);
        messageBuffer.append(message);
        messageBuffer.append("\n");
        ByteBuffer buffer = CHARSET.encode(CharBuffer.wrap(messageBuffer));
        try {
            client.write(buffer);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
