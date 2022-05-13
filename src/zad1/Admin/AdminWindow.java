package zad1.Admin;

import zad1.Client.ClientLogicHandler;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;

public class AdminWindow extends JFrame {
    private final AdminLogicHandler adminLogicHandler;
    private final ArrayList<String> topics;
    private final HashMap<String, String> topicAndLatestNews;
    private final JPanel mainPanel;
    private final JTextArea messageArea;
    private final JTextArea topicArea;
    private final JButton messageButton;
    private final JButton addButton;
    private final JButton deleteButton;
    private final JButton changeButton;
    private final JComboBox<String> topicsComboBox;
    private final GridBagConstraints constraints;
    private final GridBagLayout layout;

    private final int WIDTH = 800, HEIGHT = 600;
    public AdminWindow(){
        super("Admin");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        messageArea = new JTextArea();
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);

        topicArea = new JTextArea();
        topicArea.setLineWrap(false);
        topicArea.setWrapStyleWord(false);

        //borders
        LineBorder grayBorder = new LineBorder(Color.GRAY, 1);
        Border messageAreaBorder = BorderFactory.createTitledBorder(grayBorder, "message");
        Border topicAreaBorder = BorderFactory.createTitledBorder(grayBorder, "topic");
        messageArea.setBorder(messageAreaBorder);
        topicArea.setBorder(topicAreaBorder);

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
        constraints.gridwidth = 2;
        topicsComboBox = new JComboBox<>();
        adminLogicHandler = new AdminLogicHandler(messageArea, topicArea, topicsComboBox);
        mainPanel.add(topicsComboBox, constraints);

        constraints.gridx = 2;
        mainPanel.add(topicArea, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 4;
        constraints.gridheight = 1;
        constraints.weightx = 1;
        constraints.weighty = 0.6;
        mainPanel.add(messageArea, constraints);

        messageButton = new JButton("Send message");
        addButton = new JButton("Add topic");
        deleteButton = new JButton("Delete topic");
        changeButton = new JButton("Change topic");

        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.weighty = 0.25;
        mainPanel.add(messageButton, constraints);
        constraints.gridx = 1;
        mainPanel.add(addButton, constraints);
        constraints.gridx = 2;
        mainPanel.add(deleteButton, constraints);
        constraints.gridx = 3;
        mainPanel.add(changeButton, constraints);

        //listeners
        messageButton.addActionListener(this::messageButtonClicked);
        addButton.addActionListener(this::addButtonClicked);
        deleteButton.addActionListener(this::deleteButtonClicked);
        changeButton.addActionListener(this::changeButtonClicked);

        adminLogicHandler.get();
        setVisible(true);
        add(mainPanel);
        pack();
    }
    //send a message about a topic
    private void messageButtonClicked(ActionEvent e) {
        String news = messageArea.getText();
        String topic = (String) topicsComboBox.getSelectedItem();
        adminLogicHandler.sendNews(topic, news);
    }

    //add a topic
    private void addButtonClicked(ActionEvent e) {
        String topic = topicArea.getText();
        adminLogicHandler.add(topic);
    }
    //delete a topic
    private void deleteButtonClicked(ActionEvent e) {
        String topic = topicsComboBox.getSelectedItem().toString();
        adminLogicHandler.delete(topic);
    }

    //change a topic
    private void changeButtonClicked(ActionEvent e) {
        String topic = topicsComboBox.getSelectedItem().toString();
        String newTopic = topicArea.getText();
        adminLogicHandler.change(topic, newTopic);
    }
}
