package com.gui;

import com.bus.CommonBus;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Robot;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class ClientPanel extends JPanel {
    public final static String BACKGROUND = "0x00A571";
    public final static String FOREGROUND = "0x003927";

    private CommonPanel main_panel;
    private CommonLabel connect_label;

    private CommonBus common_bus;

    public ClientPanel(CommonBus common_bus) {
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
        this.main_panel = new CommonPanel();
        this.connect_label = new CommonLabel();

        // TODO: style main panel
        this.main_panel.setBorder(BorderFactory.createTitledBorder(null, "Connect To Server",
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            new Font("segoe ui", Font.BOLD, 16),
            Color.decode(ClientPanel.FOREGROUND))
        );
        this.add(this.main_panel);

        // TODO: style host_label
        this.main_panel.getHostLabel().setText("Remote IP:");

        // TODO: style host_text
        this.main_panel.getHostCombo().setVisible(false);
        this.main_panel.getHostText().setVisible(true);

        // TODO: style port_label
        this.main_panel.getPortLabel().setText("Remote port:");

        // TODO: style pass_label
        this.main_panel.getPassLabel().setText("Password:");

        // TODO: style pass_field
        this.main_panel.getPassText().setVisible(false);
        this.main_panel.getPassField().setVisible(true);

        // TODO: style help_label
        this.main_panel.getHelpLabel().setText("<html>>>Help: Enter a name or an IP address and port of server which you want to remote.<br>>>Example: 192.168.0.1:9999</html>");

        // TODO: style connect_label
        this.connect_label.setIcon(new ImageIcon(".\\images\\connect_icon.png"));
        this.connect_label.setText("Connect now");
        this.connect_label.setBounds(240, 250, 120, 100);
        this.connect_label.setForeground(Color.decode(ClientPanel.FOREGROUND));
        this.connect_label.setFont(new Font("segoe ui", Font.PLAIN, 13));
        this.connect_label.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                connectLabelMousePressed(e);
            }
        });
        this.add(this.connect_label);
    }

    // TODO: handle events of connect_label
    private void connectLabelMousePressed(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1) {
            try {
                String host = this.main_panel.getHostText().getText().trim();
                int port = Integer.parseInt(this.main_panel.getPortText().getText().trim());
                String password = this.main_panel.getPassField().getText().trim();
                this.common_bus.startConnectingToServer(host, port, password);

                // TODO: show remote screen
                EventQueue.invokeLater(() -> {
                    try {
                        new RemoteFrame(this.common_bus);
                    }
                    catch(Exception exception) {
                        JOptionPane.showMessageDialog(this, "Can't connecting to server:\n" + exception.getMessage());
                    }
                });
            }
            catch(Exception exception) {
                JOptionPane.showMessageDialog(this, "Can't connecting to server:\n" + exception.getMessage());
            }
        }
    }
}
