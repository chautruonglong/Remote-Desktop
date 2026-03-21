package com.gui.server;

import com.bus.common.CommonBus;
import com.bus.tcp.TcpServer;
import com.gui.AppStyles;
import com.gui.Icons;
import com.gui.MainFrame;
import com.gui.common.CommonLabel;
import com.gui.common.CommonPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.SocketException;
import java.util.List;
import java.util.Objects;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * Panel that lets the user start and stop the local remote desktop server.
 *
 * <p>The user selects a local IP address (or binds to all interfaces with {@code 0.0.0.0}),
 * sets a port and password, then clicks "Start listening". Incoming client connections are
 * accepted on a background thread that loops while the server is active.</p>
 *
 * <p>The status label updates to reflect whether the server is stopped or listening.</p>
 */
public class ServerPanel extends JPanel implements Runnable {


    private CommonPanel mainPanel;
    private JLabel statusLabel;
    private CommonLabel listenLabel;
    private CommonLabel stopLabel;

    private final CommonBus commonBus;
    private Thread listenThread;

    /**
     * Creates the server panel.
     *
     * @param commonBus the shared transport facade used to start and stop the server
     */
    public ServerPanel(CommonBus commonBus) {
        this.commonBus = commonBus;

        this.setLocation(0, MainFrame.HEIGHT_TASKBAR);
        this.setSize(MainFrame.WIDTH_FRAME, MainFrame.HEIGHT_FRAME - MainFrame.HEIGHT_TASKBAR);
        this.setBackground(Color.decode(AppStyles.COLOR_BACKGROUND));
        this.setLayout(null);

        this.initComponents();
    }

    private void initComponents() {
        this.mainPanel   = new CommonPanel();
        this.statusLabel = new JLabel();
        this.listenLabel = new CommonLabel();
        this.stopLabel   = new CommonLabel();

        this.add(this.mainPanel);

        // Populate host combo with local IPv4 addresses whenever it drops down.
        this.mainPanel.getHostCombo().addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                try {
                    mainPanel.getHostCombo().removeAllItems();
                    mainPanel.getHostCombo().addItem("0.0.0.0");
                    List<String> localAddresses = TcpServer.getLocalIpv4Addresses();
                    for (String address : localAddresses) {
                        mainPanel.getHostCombo().addItem(address);
                    }
                } catch (SocketException ex) {
                    System.err.println("Failed to enumerate local network interfaces: " + ex.getMessage());
                }
            }

            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
            @Override public void popupMenuCanceled(PopupMenuEvent e) {}
        });

        // Status label — shows "Stopped" or "Listening…"
        this.statusLabel.setText("Status: Stopped");
        this.statusLabel.setBounds(30, 200, 150, 20);
        this.statusLabel.setFont(new Font(AppStyles.FONT_UI, Font.ITALIC | Font.BOLD, 13));
        this.statusLabel.setForeground(Color.decode(AppStyles.COLOR_STATUS_STOPPED));
        this.mainPanel.add(this.statusLabel);

        // "Start listening" action label.
        this.listenLabel.setText("Start listening");
        this.listenLabel.setIcon(Icons.LISTEN);
        this.listenLabel.setBounds(50, 290, 150, 50);
        this.listenLabel.setForeground(Color.decode(AppStyles.COLOR_FOREGROUND));
        this.listenLabel.setFont(new Font(AppStyles.FONT_UI, Font.PLAIN, 15));
        this.listenLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onListenClicked(e);
            }
        });
        this.add(this.listenLabel);

        // "Stop listening" action label — initially disabled.
        this.stopLabel.setText("Stop listening");
        this.stopLabel.setIcon(Icons.STOP);
        this.stopLabel.setBounds(220, 290, 150, 50);
        this.stopLabel.setForeground(Color.decode(AppStyles.COLOR_FOREGROUND));
        this.stopLabel.setFont(new Font(AppStyles.FONT_UI, Font.PLAIN, 15));
        this.stopLabel.setEnabled(false);
        this.stopLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onStopClicked(e);
            }
        });
        this.add(this.stopLabel);
    }

    /**
     * Handles the "Start listening" click: reads form values, starts the server, and
     * launches the background accept loop.
     */
    private void onListenClicked(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1 || !this.listenLabel.isEnabled()) return;

        try {
            String host     = Objects.requireNonNull(this.mainPanel.getHostCombo().getSelectedItem()).toString().trim();
            int    port     = Integer.parseInt(this.mainPanel.getPortTextField().getText().trim());
            String password = this.mainPanel.getPasswordTextField().getText().trim();

            this.commonBus.startServer(host, port, password);

            this.listenThread = Thread.ofPlatform().daemon().name("server-listener").unstarted(this);
            this.listenThread.start();

            this.mainPanel.setEnabled(false);
            this.listenLabel.resetFont();
            this.listenLabel.setEnabled(false);
            this.stopLabel.setEnabled(true);
            this.statusLabel.setText("Status: Listening...");
            this.statusLabel.setForeground(Color.decode(AppStyles.COLOR_STATUS_LISTENING));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Could not start server:\n" + ex.getMessage()
            );
        }
    }

    /**
     * Handles the "Stop listening" click: stops the server and interrupts the accept thread.
     */
    private void onStopClicked(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1 || !this.stopLabel.isEnabled()) return;

        try {
            this.commonBus.stopServer();
            this.listenThread.interrupt();

            this.mainPanel.setEnabled(true);
            this.stopLabel.resetFont();
            this.stopLabel.setEnabled(false);
            this.listenLabel.setEnabled(true);
            this.statusLabel.setText("Status: Stopped");
            this.statusLabel.setForeground(Color.decode(AppStyles.COLOR_STATUS_STOPPED));
        } catch (Exception ex) {
            System.err.println("Error stopping server: " + ex.getMessage());
            JOptionPane.showMessageDialog(
                this,
                "Could not stop server:\n" + ex.getMessage()
            );
        }
    }

    /**
     * Background accept loop — blocks on {@link com.bus.tcp.TcpServer#acceptNextClient()}
     * and loops while the server is listening. Exceptions from individual accept attempts
     * (e.g., socket closed on stop) are expected and silently continue the loop.
     */
    @Override
    public void run() {
        while (this.commonBus.isServerListening()) {
            try {
                this.commonBus.getTcpServer().acceptNextClient();
            } catch (Exception e) {
                // A closed socket throws when stopServer() is called — this is expected.
                if (!this.commonBus.isServerListening()) break;
                System.err.println("Error accepting client connection: " + e.getMessage());
            }
        }
    }
}
