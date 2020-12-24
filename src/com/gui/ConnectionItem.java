package com.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.Timer;

public class ConnectionItem extends JMenuItem {
    private ChatPanel chat_panel;

    public ConnectionItem(ChatPanel chat_panel, ArrayList<ConnectionItem> items) {
        // TODO: style ConnectionItem
        this.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("online.png")));
        this.setText("mycodex");

        this.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for(ConnectionItem item : items) {
                    item.chat_panel.setVisible(false);
                }
                chat_panel.setVisible(true);
            }
        });

        this.chat_panel = chat_panel;
    }

    public ChatPanel getChat_panel() {
        return this.chat_panel;
    }

    public void setChat_panel(ChatPanel chat_panel) {
        this.chat_panel = chat_panel;
    }
}
