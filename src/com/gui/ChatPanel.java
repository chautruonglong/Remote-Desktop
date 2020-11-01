package com.gui;

import com.bus.CommonBus;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public class ChatPanel extends JPanel implements Runnable {
    public final static String CONTENT_BACKGROUND = "0xFFFFFF";
    public final static int MAX_LENGTH_LINE = 20;

    private JPanel content_panel;
    private JScrollPane scroll_panel;
    private JTextArea message_text;
    private JScrollPane scroll_text;
    private CommonLabel send_label;
    private GroupLayout layout;
    private GroupLayout.ParallelGroup h_parallel;
    private GroupLayout.SequentialGroup v_sequential;

    private CommonBus common_bus;

    public ChatPanel(CommonBus common_bus) {
        // TODO: style ClientPanel
        this.setLocation(0, MainFrame.HEIGHT_TASKBAR);
        this.setSize(MainFrame.WIDTH_FRAME, MainFrame.HEIGHT_FRAME - MainFrame.HEIGHT_TASKBAR);
        this.setBackground(Color.decode(ClientPanel.BACKGROUND));
        this.setLayout(null);

        // TODO: class for handle events
        this.common_bus = common_bus;

        // TODO: add components
        this.initComponents();
    }

    private void initComponents() {
        // TODO: constructor
        this.content_panel = new JPanel();
        this.scroll_panel = new JScrollPane();
        this.message_text = new JTextArea();
        this.scroll_text = new JScrollPane();
        this.send_label = new CommonLabel();

        // TODO: create layout for content_panel
        this.layout = new GroupLayout(this.content_panel);
        this.h_parallel = this.layout.createParallelGroup();
        this.v_sequential = this.layout.createSequentialGroup();
        this.layout.setHorizontalGroup(h_parallel);
        this.layout.setVerticalGroup(v_sequential);

        // TODO: style content_panel;
        this.content_panel.setBackground(Color.decode(ChatPanel.CONTENT_BACKGROUND));
        this.content_panel.setLayout(layout);

        // TODO: style scroll_panel
        this.scroll_panel.setViewportView(this.content_panel);
        this.scroll_panel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.scroll_panel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.scroll_panel.setBounds(10, 10, MainFrame.WIDTH_FRAME - 20, MainFrame.HEIGHT_FRAME - 150);
        this.add(this.scroll_panel);

        // TODO: style message_text
        this.message_text.setFont(new Font("consolas", Font.PLAIN, 14));
        this.message_text.setLineWrap(true);
        this.message_text.getDocument().putProperty("filterNewlines", Boolean.TRUE);
        this.message_text.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                messageTextKeyPressed(e);
            }
        });

        // TODO: style scroll_text
        this.scroll_text.setViewportView(this.message_text);
        this.scroll_text.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.scroll_text.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.scroll_text = new JScrollPane(this.message_text);
        this.scroll_text.setBounds(30, scroll_panel.getHeight() + 20, MainFrame.WIDTH_FRAME - 150, MainFrame.HEIGHT_FRAME - MainFrame.HEIGHT_TASKBAR - scroll_panel.getHeight() - 30);
        this.add(this.scroll_text);

        // TODO: style send_label
        this.send_label.setText("Send");
        this.send_label.setIcon(new ImageIcon(".\\images\\send_icon.png"));
        this.send_label.setForeground(Color.decode(ClientPanel.FOREGROUND));
        this.send_label.setFont(new Font("segoe ui", Font.PLAIN, 13));
        this.send_label.setBounds(this.scroll_text.getWidth() + 50, this.scroll_text.getY(), 100, 30);
        this.send_label.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                sendLabelMousePressed(e);
            }
        });
        this.add(this.send_label);

        // TODO: run thread recv messages
        new Thread(this).start();
    }

    private void sendMessage() {
        try {
            String message = this.message_text.getText();
            if(!message.trim().equals("")) {
                this.common_bus.getChatBus().sendMessage(message);
                message = "You send a message:" + message;
                int gap = this.scroll_panel.getWidth() - 180;
                this.addMessageToPanel(message, gap, "green");
            }
            this.message_text.setText("");
        }
        catch(Exception exception) {
            JOptionPane.showMessageDialog(this, "Can't send message:\n" + exception.getMessage());
        }
    }

    private void messageTextKeyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER) {
            this.sendMessage();
        }
    }

    private void sendLabelMousePressed(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1) {
            this.sendMessage();
        }
    }

    // TODO: show message in panel
    public void addMessageToPanel(String message, int gap, String color_header) {
        message = this.handleMessage(message, color_header);
        JLabel label = new JLabel(message);
        label.setFont(new Font("consolas", Font.PLAIN, 14));
        label.setForeground(Color.BLACK);

        this.h_parallel.addGroup(
            this.layout.createSequentialGroup()
                .addContainerGap(gap, gap)
                .addComponent(label)
        );
        this.v_sequential.addComponent(label).addGap(10, 10, 10);
        this.scroll_panel.revalidate();
        this.scroll_panel.getViewport().setViewPosition(new Point(0, this.scroll_panel.getVerticalScrollBar().getMaximum()));
        this.scroll_panel.getViewport().setViewPosition(new Point(0, this.scroll_panel.getVerticalScrollBar().getMaximum()));
    }

    // TODO: format message
    public String handleMessage(String message, String color_header) {
        String formatted_message = "<html>";
        formatted_message += this.getHeaderName(message, color_header);
        formatted_message += this.multiLinesString(message);
        formatted_message += "</html>";
        return formatted_message;
    }

    public String getHeaderName(String message, String color_header) {
        if(message.contains(":")) {
            String header_name = "<font color='" + color_header + "'>";
            header_name += message.substring(0, message.indexOf(':') + 1) + "</font><br>";
            return header_name;
        }
        return "";
    }

    public String multiLinesString(String message) {
        message = message.substring(message.indexOf(':') + 1);
        if(message.length() > ChatPanel.MAX_LENGTH_LINE) {
            int loops = message.length() / ChatPanel.MAX_LENGTH_LINE;
            int index = 0;
            String content = "";
            for(int i = 0; i < loops; ++i) {
                content += message.substring(index, index + ChatPanel.MAX_LENGTH_LINE) + "<br>";
                index += ChatPanel.MAX_LENGTH_LINE;
            }
            content += message.substring(index);
            return content;
        }
        return message;
    }

    // TODO: recv messages
    @Override
    public void run() {
        while(true) {
            try {
                if(this.common_bus.getTcpServer().isHasPartner() || this.common_bus.getTcpClient().isConnectedServer()) {
                    String message = this.common_bus.getChatBus().recvMessage();
                    if(message != null) {
                        this.addMessageToPanel(message, 0, "blue");
                    }
                }
                Thread.sleep(100);
            }
            catch(Exception e) {
                this.common_bus.getTcpServer().setHasPartner(false);
                this.common_bus.getTcpClient().setConnectedServer(false);
                JOptionPane.showMessageDialog(this, "Your partner was closed");
            }
        }
    }
}