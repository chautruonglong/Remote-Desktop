package com.gui.chat;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

public class ConnectionItem extends JMenuItem {
    private ChatPanel chat_panel;

    public ConnectionItem(ChatPanel chat_panel, ArrayList<ChatPanel> chat_panels) {
        // TODO: style ConnectionItem
        this.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("online.png")));
        this.setText(chat_panel.getHostName());

        this.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for(ChatPanel panel : chat_panels) {
                    panel.setVisible(false);
                }
                chat_panel.setVisible(true);
            }
        });

        this.chat_panel = chat_panel;
    }
}
