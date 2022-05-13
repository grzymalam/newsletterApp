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
    private final HashMap<String, String> recentTopicNameChanges = new HashMap<>();
    private final Map<SocketChannel, List<String>> clients = new HashMap<>();
    private ServerSocketChannel serverSocketChannel;

    public Server() {
        topics.put("sport", new ArrayList<>());
        topics.put("tech", new ArrayList<>());
        recentTopicNameChanges.put("sport", "");
        recentTopicNameChanges.put("tech", "");
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
                int i = 0;
                while (it.hasNext()) {
                    System.out.println("iteration " + i++);
                    SelectionKey key = it.next();
                    it.remove();
                    if (key.isAcceptable()) { //nowy klient
                        SocketChannel client = serverSocketChannel.accept();
                        System.out.println("new connection " + client.getRemoteAddress());
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                        clients.put(client, new ArrayList<>());
                        System.out.println("New client connected");
                        continue;
                    }

                    if (key.isReadable()) { //klient wysyla dane
                        SocketChannel client = (SocketChannel) key.channel();
                        System.out.println("Client sent data");
                        handleRequest(client);
                        continue;
                    }
                }
            }catch (IOException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    private static final Charset CHARSET = Charset.forName("ISO-8859-2");
    private static final int BUFFER_SIZE = 1024;

    private ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    private StringBuffer stringBuffer = new StringBuffer();

    private void handleRequest(SocketChannel client) {
        if(!client.isOpen()) return;
        System.out.println("handleRequest");
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
                        if (c == '\n' || c == '\r') break petla;
                        stringBuffer.append(c);
                    }
                    System.out.println("Client sent: " + stringBuffer.toString());
                    break;
                }
            }
            //admin
            //message/add/delete/change

            //client
            //subscribe/unsubscribe/get <tematy>

            //cala magia
            ArrayList<String> requestContent = new ArrayList<>(Arrays.asList(stringBuffer.toString().split("&")));
            String requestSource = requestContent.get(0); //admin albo client
            System.out.println("received request: " + requestContent);
            String command = requestContent.get(1);
            switch (requestSource){
                case "admin":
                    switch (command){
                        case "news":
                            System.out.println("admin news");
                            String topic = requestContent.get(2);
                            String news = requestContent.get(3);
                            topics.putIfAbsent(topic, new ArrayList<>());
                            topics.get(topic).add(new News(news));
                            sendMessage(client, "success");
                            break;
                        case "add":
                            System.out.println("admin add");
                            String topicName = requestContent.get(2);
                            topics.putIfAbsent(topicName, new ArrayList<>());
                            recentTopicNameChanges.putIfAbsent(topicName, "");
                            sendMessage(client, "success");
                            break;
                        case "delete":
                            System.out.println("admin delete");
                            String topicNameToDelete = requestContent.get(2);
                            topics.remove(topicNameToDelete);
                            recentTopicNameChanges.remove(topicNameToDelete);
                            sendMessage(client, "success");
                            break;
                        case "change":
                            System.out.println("admin change");
                            String topicNameToChange = requestContent.get(2);
                            String newTopicName = "";
                            try {
                                newTopicName = requestContent.get(3);
                            }catch (IndexOutOfBoundsException e){
                                sendMessage(client, "failure");
                            }
                            if(topics.containsKey(newTopicName)){
                                sendMessage(client, "failure");
                                break;
                            }
                            //updating the clients subscriptions
                            for(List<String> t: clients.values()){
                                if(t.contains(topicNameToChange)){
                                    String finalNewTopicName = newTopicName;
                                    t.replaceAll(s -> s.equals(topicNameToChange) ? finalNewTopicName : s);
                                }
                            }
                            topics.put(newTopicName, topics.get(topicNameToChange));
                            topics.remove(topicNameToChange);
                            recentTopicNameChanges.replace(topicNameToChange, newTopicName);
                            sendMessage(client, "success");
                            break;
                        case "get":
                            System.out.println("admin get");
                            String messageToSend = "get&";
                            if(topics.size() == 0){
                                messageToSend = "null";
                            }else{
                                for(int i = 0; i < topics.size(); i++){
                                    if(i == topics.size() - 1){
                                        messageToSend += topics.keySet().toArray()[i];
                                    }else{
                                        messageToSend += topics.keySet().toArray()[i] + "&";
                                    }
                                }
                            }
                            sendMessage(client, messageToSend);
                            break;
                    }
                    break;

                case "client":
                    String topic;
                    clientSwitch:
                    switch (command){
                        case "subscribe":
                            System.out.println("client subscribe");
                            topic = requestContent.get(2);
                            System.out.println("subscribing to topic: " + topic);
                            System.out.println("topics " + topics.keySet());
                            System.out.println("contains key? " + topics.containsKey(topic));
                            if (topics.containsKey(topic)) {
                                clients.get(client).add(topic);
                                sendMessage(client, "success");
                            }else{
                                sendMessage(client, "fail");
                            }
                            break;
                        case "unsubscribe":
                            System.out.println("client unsubscribe");
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
                            System.out.println("client update");
                            String topicToGetNewsFor = requestContent.get(2);
                            String changedTopicName = "";
                            boolean hasTheTopicBeenUpdated = false;
                            if(!topics.containsKey(topicToGetNewsFor)){
                                if(recentTopicNameChanges.containsKey(topicToGetNewsFor)) {
                                    System.out.println("topic has been changed");
                                    changedTopicName = recentTopicNameChanges.get(topicToGetNewsFor);
                                    hasTheTopicBeenUpdated = true;
                                    clients.get(client).remove(topicToGetNewsFor);
                                    clients.get(client).add(changedTopicName);
                                }else {
                                    System.out.println("topic was deleted");
                                    sendMessage(client, "update&" + topicToGetNewsFor + "&deleted");
                                    break;
                                }
                            }
                            //loop po klientach, jesli ktos jest subskrybowany na topic, to wyslij do niego newsa
                            for (String t : clients.get(client)) {
                                if(t.equals(topicToGetNewsFor)){
                                    topics.get(topicToGetNewsFor).stream().max(Comparator.comparing(News::getId)).ifPresent(news -> sendMessage(client,
                                            "update&" + topicToGetNewsFor + "&" + news.getData()));
                                    break clientSwitch;
                                }else if(t.equals(changedTopicName)){
                                    boolean finalHasTheTopicBeenUpdated = hasTheTopicBeenUpdated;
                                    String finalChangedTopicName1 = changedTopicName;
                                    topics.get(changedTopicName).stream().max(Comparator.comparing(News::getId)).ifPresent(news -> sendMessage(client,
                                            "update&" + topicToGetNewsFor + "&" + news.getData() +
                                                    (finalHasTheTopicBeenUpdated ? "&updated&" + finalChangedTopicName1 : "")));
                                    break clientSwitch;
                                }
                            }

                            for (String t: topics.keySet()){
                                if(t.equals(topicToGetNewsFor)){
                                    clients.get(client).add(topicToGetNewsFor);
                                    topics.get(topicToGetNewsFor).stream().max(Comparator.comparing(News::getId)).ifPresent(news -> sendMessage(client,
                                            "update&" + topicToGetNewsFor + "&" + news.getData()));
                                    break clientSwitch;
                                }else if(t.equals(changedTopicName)){
                                    clients.get(client).add(topicToGetNewsFor);
                                    boolean finalHasTheTopicBeenUpdated1 = hasTheTopicBeenUpdated;
                                    String finalChangedTopicName = changedTopicName;
                                    topics.get(changedTopicName).stream().max(Comparator.comparing(News::getId)).ifPresent(news -> sendMessage(client,
                                            "update&" + topicToGetNewsFor + "&" + news.getData() +
                                                    (finalHasTheTopicBeenUpdated1 ? "&updated&" + finalChangedTopicName : "")));
                                    break clientSwitch;
                                }
                            }
                            for(String t: recentTopicNameChanges.keySet()){
                                if(t.equals(topicToGetNewsFor)){
                                    clients.get(client).add(topicToGetNewsFor);
                                }
                            }
                            sendMessage(client, "update&" + topicToGetNewsFor + "&" + "null");
                            break;
                    }
                    break;
                default:
                    System.out.println("Unknown request");
            }

        }catch (IOException e) {
            try {
                client.close();
                System.out.println("Client disconnected");
            }catch (IOException e1){
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    //wysylanie danych do klienta
    private void sendMessage(SocketChannel client, String message){
        ByteBuffer buffer = CHARSET.encode(CharBuffer.wrap(message));
        System.out.println("sending message: " + message);
        try {
            client.write(buffer);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
