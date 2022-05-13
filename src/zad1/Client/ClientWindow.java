package zad1.Client;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;


public class ClientWindow extends JFrame {
    private final ClientLogicHandler clientLogicHandler;
    private final ArrayList<String> topics;
    private final HashMap<String, String> topicAndLatestNews;
    private final JPanel mainPanel;
    private final JTextArea textArea;
    private final JTextArea topicInput;
    private final JButton subscribeButton;
    private final JButton unsubscribeButton;
    private final JButton updateButton;
    private final JComboBox<String> topicsComboBox;
    private final GridBagConstraints constraints;
    private final GridBagLayout layout;

    private final int WIDTH = 800, HEIGHT = 600;
    public ClientWindow(){
        super("Client");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        topicInput = new JTextArea();
        topicInput.setLineWrap(false);
        topicInput.setWrapStyleWord(false);

        //borders
        LineBorder grayBorder = new LineBorder(Color.GRAY, 1);
        Border messageAreaBorder = BorderFactory.createTitledBorder(grayBorder, "news");
        Border topicAreaBorder = BorderFactory.createTitledBorder(grayBorder, "topic input");
        textArea.setBorder(messageAreaBorder);
        topicInput.setBorder(topicAreaBorder);
        //data
        topics = new ArrayList<>();
        topicAndLatestNews = new HashMap<>();
        //gui
        mainPanel = new JPanel();
        layout = new GridBagLayout();
        constraints = new GridBagConstraints();
        mainPanel.setLayout(layout);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 0;
        topicsComboBox = new JComboBox<>();
        clientLogicHandler = new ClientLogicHandler(textArea, topicAndLatestNews, topics, topicsComboBox);
        mainPanel.add(topicsComboBox, constraints);

        constraints.gridx = 1;
        constraints.gridwidth = 2;
        mainPanel.add(topicInput, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 3;
        constraints.gridheight = 1;
        constraints.weightx = 1;
        constraints.weighty = 0.6;
        mainPanel.add(textArea, constraints);

        subscribeButton = new JButton("Subscribe");
        unsubscribeButton = new JButton("Unsubscribe");
        updateButton = new JButton("Update");
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.weighty = 0.3;
        mainPanel.add(subscribeButton, constraints);
        constraints.gridx = 1;
        mainPanel.add(unsubscribeButton, constraints);
        constraints.gridx = 2;
        mainPanel.add(updateButton, constraints);

        //listeners
        subscribeButton.addActionListener(this::subscribeButtonClicked);
        unsubscribeButton.addActionListener(this::unsubscribeButtonClicked);
        updateButton.addActionListener(this::updateButtonClicked);
        setVisible(true);
        add(mainPanel);
        pack();
    }
    private void subscribeButtonClicked(ActionEvent e) {
        String topic = topicInput.getText();
        if(!topics.contains(topic)){
            clientLogicHandler.subscribe(topic);
        }
    }
    private void unsubscribeButtonClicked(ActionEvent e) {
        String topic = (String) topicsComboBox.getSelectedItem();
        if(topics.contains(topic)){
            clientLogicHandler.unsubscribe(topic);
        }
    }
    private void updateButtonClicked(ActionEvent e) {
        String topic = (String) topicsComboBox.getSelectedItem();
        clientLogicHandler.update(topic);
    }
}
