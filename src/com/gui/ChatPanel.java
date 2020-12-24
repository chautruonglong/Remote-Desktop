package com.gui;

import com.bus.*;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

public class ChatPanel extends JPanel implements Runnable {
    public final static String CONTENT_BACKGROUND = "0xFFFFFF";
    public final static int MAX_LENGTH_LINE = 20;

    private JPanel content_panel;
    private JScrollPane scroll_panel;
    private JTextArea message_text;
    private JScrollPane scroll_text;
    private CommonLabel send_label;
    private CommonLabel file_label;
    private JLabel loader_label;

    private GroupLayout layout;
    private GroupLayout.ParallelGroup h_parallel;
    private GroupLayout.SequentialGroup v_sequential;

    private MainChatPanel root;

    private CommonBus common_bus;
    private ChatBus chat_bus;

    private Thread recv_thread;

    public ChatPanel(MainChatPanel root, CommonBus common_bus, ChatBus chat_bus) {
        // TODO: style ChatPanel
        this.setLocation(0, MainFrame.HEIGHT_TASKBAR - 42);
        this.setSize(MainFrame.WIDTH_FRAME, MainFrame.HEIGHT_FRAME - MainFrame.HEIGHT_TASKBAR);
        this.setBackground(Color.decode(ClientPanel.BACKGROUND));
        this.setLayout(null);

        // TODO: class for handle events
        this.common_bus = common_bus;
        this.chat_bus = chat_bus;
        this.root = root;

        // TODO: add components
        this.initComponents();

        // TODO: Disable chat
        this.setEnabled(false);

        // TODO: start recv_thread
        this.recv_thread = new Thread(this);
        this.recv_thread.setDaemon(true);
        this.recv_thread.start();
    }

    private void initComponents() {
        // TODO: constructor
        this.content_panel = new JPanel();
        this.scroll_panel = new JScrollPane();
        this.message_text = new JTextArea();
        this.scroll_text = new JScrollPane();
        this.send_label = new CommonLabel();
        this.file_label = new CommonLabel();
        this.loader_label = new JLabel();

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
        this.send_label.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("send_icon.png")));
        this.send_label.setForeground(Color.decode(ClientPanel.FOREGROUND));
        this.send_label.setFont(new Font("segoe ui", Font.PLAIN, 13));
        this.send_label.setBounds(this.scroll_text.getWidth() + 50, this.scroll_text.getY(), 80, 30);
        this.send_label.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                sendLabelMousePressed(e);
            }
        });
        this.add(this.send_label);

        // TODO: style file_label
        this.file_label.setText("File");
        this.file_label.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("file_icon.png")));
        this.file_label.setForeground(Color.decode(ClientPanel.FOREGROUND));
        this.file_label.setFont(new Font("segoe ui", Font.PLAIN, 13));
        this.file_label.setBounds(this.scroll_text.getWidth() + 50, this.scroll_text.getY() + 40, 60, 30);
        this.file_label.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    fileLabelMousePressed(e);
                }
                catch(IOException exception) {
                    JOptionPane.showMessageDialog(null, "Can't send message:\n" + exception.getMessage());
                }
            }
        });
        this.add(this.file_label);

        // TODO: style loader_label
        this.loader_label.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("loader_icon2.gif")));
        this.loader_label.setBounds(this.file_label.getX() + 60, this.file_label.getY() + 7, 16, 16);
        this.loader_label.setVisible(false);
        this.add(this.loader_label);
    }

    @Override
    public void setEnabled(boolean b) {
        this.message_text.setEnabled(b);
        this.send_label.setEnabled(b);
        this.file_label.setEnabled(b);
    }

    @Override
    public void setVisible(boolean b) {
        // TODO: move scroll to bottom when show up
        this.scroll_panel.getViewport().setViewPosition(new Point(0, this.scroll_panel.getVerticalScrollBar().getMaximum()));
        this.scroll_panel.getViewport().setViewPosition(new Point(0, this.scroll_panel.getVerticalScrollBar().getMaximum()));
        super.setVisible(b);
    }

    private void messageTextKeyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER) {
            this.sendMessage();
        }
    }

    private void sendLabelMousePressed(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1 && this.send_label.isEnabled()) {
            this.sendMessage();
        }
    }

    // TODO: send a string message
    private void sendMessage() {
        try {
            String content = this.message_text.getText();
            if(!content.trim().equals("")) {
                StringMessage str_message = new StringMessage(InetAddress.getLocalHost().getHostName(), content);
                this.chat_bus.sendMessage(str_message);
                int gap = this.scroll_panel.getWidth() - 180;

                JLabel label = new JLabel("You send a message:" + content);
                label.setFont(new Font("consolas", Font.PLAIN, 14));
                label.setForeground(Color.BLACK);
                this.addMessageToPanel(label, gap, "green");
            }
            this.message_text.setText("");
        }
        catch(Exception exception) {
            JOptionPane.showMessageDialog(this, "Can't send message:\n" + exception.getMessage());
        }
    }

    // TODO: send a file message
    private void fileLabelMousePressed(MouseEvent e) throws IOException {
        if(e.getButton() == MouseEvent.BUTTON1 && this.file_label.isEnabled()) {
            JFileChooser file_chooser = new JFileChooser();
            file_chooser.setCurrentDirectory(FileSystemView.getFileSystemView().getHomeDirectory());
            file_chooser.showDialog(this, "Send");

            File dir = file_chooser.getSelectedFile();
            if(dir != null) {
                if(dir.length() > 30 * 1024 * 1024) throw new IOException("File too large, please send file < 30MB");

                this.loader_label.setVisible(true);
                this.setEnabled(false);
                Thread send_thread = new Thread(() -> {
                    try {
                        // TODO: send file in new thread
                        FileInputStream fis = new FileInputStream(dir);
                        FileMessage file_message = new FileMessage(InetAddress.getLocalHost().getHostName(), dir.getName(), dir.length(), fis.readAllBytes());
                        this.chat_bus.sendMessage(file_message);

                        int gap = this.scroll_panel.getWidth() - 180;
                        file_message.setSender("You");
                        FileLabel file_label = new FileLabel(file_message);
                        this.addMessageToPanel(file_label, gap, "green");

                        this.setEnabled(true);
                        this.loader_label.setVisible(false);
                    }
                    catch(IOException exception) {
                        this.setEnabled(false);
                        this.loader_label.setVisible(false);
                        JOptionPane.showMessageDialog(null, "Can't send file:\n" + exception.getMessage());
                    }
                });
                send_thread.setDaemon(true);
                send_thread.start();
            }
        }
    }

    // TODO: show message in panel
    public void addMessageToPanel(JLabel label, int gap, String color_header) {
        EventQueue.invokeLater(() -> {
            label.setText(this.handleMessage(label.getText(), color_header));

            this.h_parallel.addGroup(
                this.layout.createSequentialGroup()
                    .addContainerGap(gap, gap)
                    .addComponent(label)
            );
            this.v_sequential.addComponent(label).addGap(10, 10, 10);
            this.scroll_panel.revalidate();

            // TODO: move scroll to bottom
            this.scroll_panel.getViewport().setViewPosition(new Point(0, this.scroll_panel.getVerticalScrollBar().getMaximum()));
            this.scroll_panel.getViewport().setViewPosition(new Point(0, this.scroll_panel.getVerticalScrollBar().getMaximum()));
        });
    }

    // TODO: format message
    private String handleMessage(String message, String color_header) {
        String formatted_message = "<html>";
        formatted_message += this.getHeaderName(message, color_header);
        formatted_message += this.multiLinesString(message);
        formatted_message += "</html>";
        return formatted_message;
    }

    private String getHeaderName(String message, String color_header) {
        if(message.contains(":")) {
            String header_name = "<font color='" + color_header + "'>";
            header_name += message.substring(0, message.indexOf(':') + 1) + "</font><br>";
            return header_name;
        }
        return "";
    }

    private String multiLinesString(String message) {
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
                    this.setEnabled(true);
                    Message obj_message = this.chat_bus.recvMessage();
                    if(obj_message != null) {
                        if(obj_message.getCurrentType() == Message.STRING_MESSAGE) {
                            StringMessage str_message = (StringMessage) obj_message;

                            JLabel label = new JLabel(str_message.getSender() + " send a message:" + str_message.getContent());
                            label.setFont(new Font("consolas", Font.PLAIN, 14));
                            label.setForeground(Color.BLACK);
                            this.addMessageToPanel(label, 0, "blue");
                        }
                        else if(obj_message.getCurrentType() == Message.FILE_MESSAGE) {
                            FileMessage file_message = (FileMessage) obj_message;
                            FileLabel file_label = new FileLabel(file_message);
                            this.addMessageToPanel(file_label, 0, "blue");
                        }
                    }
                    continue; // TODO: pass sleep if connected
                }
                Thread.sleep(1000); // TODO: update status of client and server
            }
            catch(Exception e) {
                this.setEnabled(false);
                this.root.remove(this);
                this.root.getPopupMenu().remove(item);
                this.common_bus.getTcpServer().setHasPartner(false);
                this.common_bus.getTcpClient().setConnectedServer(false);

                this.root.validate();
                this.root.revalidate();
                this.root.repaint();
            }
        }
    }

    private ConnectionItem item;
    public void setConnectionItem(ConnectionItem item) {
        this.item = item;
    }
}