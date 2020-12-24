package com.gui;

import com.bus.ChatBus;
import com.bus.CommonBus;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.*;

public class MainChatPanel extends JPanel {
    private JLabel connections_label;
    private JMenuBar menu_bar;
    private JPopupMenu popup_menu;

    private ArrayList<ConnectionItem> con_items;

    private CommonBus common_bus;

    public MainChatPanel(CommonBus common_bus) {
        // TODO: style MainChatPanel
        this.setLocation(0, MainFrame.HEIGHT_TASKBAR);
        this.setSize(MainFrame.WIDTH_FRAME, MainFrame.HEIGHT_FRAME - MainFrame.HEIGHT_TASKBAR);
        this.setBackground(Color.decode(ClientPanel.BACKGROUND));
        this.setLayout(null);

        // TODO: class for handle events
        this.common_bus = common_bus;

        // TODO: add all components
        this.initComponents();
    }

    private void initComponents() {
        // TODO: constructors
        this.menu_bar = new JMenuBar();
        this.connections_label = new JLabel();
        this.popup_menu = new JPopupMenu();
        this.con_items = new ArrayList<>();

        // TODO: style connections_label
        this.connections_label.setText("All connections");
        this.connections_label.setBounds(0, 0, 100, 15);
        this.connections_label.setFont(new Font("segoe ui", Font.PLAIN, 13));
        this.connections_label.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                popupMenuMousePressed(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                popupMenuMouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                popupMenuMouseExited(e);
            }
        });

        // TODO: style menu_bar
        this.menu_bar.add(this.connections_label);
        this.menu_bar.setBounds(0, 0, MainFrame.WIDTH_FRAME, 15);
        this.menu_bar.setLayout(new GridBagLayout());
        this.add(this.menu_bar);
    }

    private void popupMenuMousePressed(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1) {
            this.popup_menu.show(this.connections_label, 100, 10);
        }
    }

    private void popupMenuMouseEntered(MouseEvent e) {
        this.connections_label.setFont(new Font("segoe ui", Font.BOLD, 13));
        this.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void popupMenuMouseExited(MouseEvent e) {
        this.connections_label.setFont(new Font("segoe ui", Font.PLAIN, 13));
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    public JPopupMenu getPopupMenu() {
        return this.popup_menu;
    }

    public void addNewConnection(ChatBus chat_bus) {
        ChatPanel chat_panel = new ChatPanel(this, this.common_bus, chat_bus);
        ConnectionItem item = new ConnectionItem(chat_panel, this.con_items);
        chat_panel.setConnectionItem(item);
        this.add(chat_panel);
        this.con_items.add(item);
        this.popup_menu.add(item);

        this.validate();
        this.revalidate();
        this.repaint();
    }
}