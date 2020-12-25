package com.gui.chat;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

public class ConnectionItem extends JMenuItem {
    private ChatPanel chat_panel;
    private ArrayList<ChatPanel> chat_panels;

    public ConnectionItem(ChatPanel chat_panel, ArrayList<ChatPanel> chat_panels) {
        // TODO: style ConnectionItem
        this.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("online.png")));
        this.setText(chat_panel.getHostName());
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                connectionItemMousePressed(e);
            }
        });

        this.chat_panel = chat_panel;
        this.chat_panels = chat_panels;
    }

    private void connectionItemMousePressed(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1) {
            for(ChatPanel panel : this.chat_panels) {
                panel.setVisible(false);
            }
            this.chat_panel.setVisible(true);
        }
    }
}
