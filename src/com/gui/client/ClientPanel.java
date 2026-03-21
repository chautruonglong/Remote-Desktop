package com.gui.client;

import com.bus.common.CommonBus;
import com.gui.Icons;
import com.gui.MainFrame;
import com.gui.common.CommonLabel;
import com.gui.common.CommonPanel;
import com.gui.remote.RemoteFrame;
import com.model.ImageQuality;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.TitledBorder;

/**
 * Panel that lets the user configure and initiate a remote desktop connection to a server.
 *
 * <p>The user enters the server's IP address, port, and password, selects an image quality,
 * and clicks "Connect now". The connection is established on a background thread to avoid
 * blocking the EDT. On success a {@link RemoteFrame} is opened.</p>
 *
 * <p>Colour constants ({@link #BACKGROUND}, {@link #FOREGROUND}) are shared with other
 * panels so the application maintains a consistent colour scheme.</p>
 */
public class ClientPanel extends JPanel {

    /** Hex colour string for the panel background — used by other panels for consistency. */
    public static final String BACKGROUND = "0x00A571";

    /** Hex colour string for the panel foreground text — used by other panels for consistency. */
    public static final String FOREGROUND = "0x003927";

    private static final String FONT_FAMILY = "segoe ui";

    private CommonPanel mainPanel;
    private CommonLabel connectLabel;
    private JRadioButton lowQualityRadio;
    private JRadioButton highQualityRadio;
    private JLabel loaderLabel;

    private final CommonBus commonBus;

    /**
     * Creates the client panel.
     *
     * @param commonBus the shared transport facade used to establish the remote connection
     */
    public ClientPanel(CommonBus commonBus) {
        this.commonBus = commonBus;

        this.setLocation(0, MainFrame.HEIGHT_TASKBAR);
        this.setSize(MainFrame.WIDTH_FRAME, MainFrame.HEIGHT_FRAME - MainFrame.HEIGHT_TASKBAR);
        this.setBackground(Color.decode(BACKGROUND));
        this.setLayout(null);

        this.initComponents();
    }

    private void initComponents() {
        this.mainPanel         = new CommonPanel();
        this.connectLabel      = new CommonLabel();
        ButtonGroup qualityButtonGroup = new ButtonGroup();
        this.lowQualityRadio   = new JRadioButton();
        this.highQualityRadio  = new JRadioButton();
        this.loaderLabel       = new JLabel();

        // Override the shared panel's border title for the client context.
        this.mainPanel.setBorder(BorderFactory.createTitledBorder(
            null,
            "Connect To Server",
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            new Font(FONT_FAMILY, Font.BOLD, 16),
            Color.decode(FOREGROUND)
        ));
        this.add(this.mainPanel);

        // Customise shared panel labels for the client perspective.
        this.mainPanel.getHostLabel().setText("Remote IP:");
        this.mainPanel.getPortLabel().setText("Remote port:");
        this.mainPanel.getPasswordLabel().setText("Password:");

        // Show host text field (type address); hide host combo (for server's local IPs).
        this.mainPanel.getHostCombo().setVisible(false);
        this.mainPanel.getHostTextField().setVisible(true);

        // Show masked password field; hide plain text field.
        this.mainPanel.getPasswordTextField().setVisible(false);
        this.mainPanel.getPasswordField().setVisible(true);

        // Context-sensitive help text.
        this.mainPanel.getHelpLabel().setText(
            "<html>" +
            "&gt;&gt; Help: Enter the IP address and port of the server you want to control.<br>" +
            "&gt;&gt; Example: 192.168.0.1:9999" +
            "</html>"
        );

        // "Connect now" action label.
        this.connectLabel.setIcon(Icons.CONNECT);
        this.connectLabel.setText("Connect now");
        this.connectLabel.setBounds(220, 290, 150, 50);
        this.connectLabel.setForeground(Color.decode(FOREGROUND));
        this.connectLabel.setFont(new Font(FONT_FAMILY, Font.PLAIN, 15));
        this.connectLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                onConnectClicked(e);
            }
        });
        this.add(this.connectLabel);

        // Image quality radio buttons.
        this.lowQualityRadio.setText("Low quality");
        this.lowQualityRadio.setBounds(60, 290, 100, 30);
        this.lowQualityRadio.setOpaque(false);
        this.lowQualityRadio.setSelected(true);
        qualityButtonGroup.add(this.lowQualityRadio);
        this.add(this.lowQualityRadio);

        this.highQualityRadio.setText("High quality");
        this.highQualityRadio.setBounds(60, 310, 100, 30);
        this.highQualityRadio.setOpaque(false);
        qualityButtonGroup.add(this.highQualityRadio);
        this.add(this.highQualityRadio);

        // Spinner shown while connecting.
        this.loaderLabel.setIcon(Icons.LOADER);
        this.loaderLabel.setBounds(340, 307, 16, 16);
        this.loaderLabel.setVisible(false);
        this.add(this.loaderLabel);
    }

    /**
     * Enables or disables all interactive controls on this panel.
     *
     * @param enabled {@code true} to enable, {@code false} to disable
     */
    @Override
    public void setEnabled(boolean enabled) {
        this.mainPanel.setEnabled(enabled);
        this.lowQualityRadio.setEnabled(enabled);
        this.highQualityRadio.setEnabled(enabled);
        this.connectLabel.setEnabled(enabled);
    }

    /**
     * Returns {@code true} if the given host string is either a valid IPv4 dotted-quad
     * (exactly three dots) or a hostname (no dots — resolved via DNS).
     *
     * @param host the user-supplied host string
     * @return {@code true} if the format is acceptable
     */
    private static boolean isValidHostFormat(String host) {
        long dotCount = host.chars().filter(c -> c == '.').count();
        // Three dots → IPv4 address; zero dots → simple hostname.
        return dotCount == 3 || dotCount == 0;
    }

    /**
     * Handles the "Connect now" click: validates input, connects in a background thread,
     * and opens a {@link RemoteFrame} on success.
     */
    private void onConnectClicked(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1 || !this.connectLabel.isEnabled()) return;

        this.setEnabled(false);
        this.loaderLabel.setVisible(true);

        Thread connectThread = new Thread(() -> {
            try {
                String host     = this.mainPanel.getHostTextField().getText().trim();
                int    port     = Integer.parseInt(this.mainPanel.getPortTextField().getText().trim());
                // Use getPassword() to avoid leaving the password as a String in the pool.
                String password = new String(this.mainPanel.getPasswordField().getPassword()).trim();

                if (!isValidHostFormat(host)) throw new Exception("Invalid host format — expected IPv4 or hostname");

                this.commonBus.connectToServer(host, port, password);

                ImageQuality quality = this.highQualityRadio.isSelected()
                    ? ImageQuality.HIGH
                    : ImageQuality.LOW;

                EventQueue.invokeLater(() -> {
                    try {
                        new RemoteFrame(this, this.commonBus, quality);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                            this,
                            "Could not start remote session:\n" + ex.getMessage()
                        );
                    }
                });
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Could not connect to server:\n" + ex.getMessage()
                );
            } finally {
                this.setEnabled(true);
                this.loaderLabel.setVisible(false);
            }
        });
        connectThread.setDaemon(true);
        connectThread.start();
    }
}
