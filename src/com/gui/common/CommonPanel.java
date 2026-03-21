package com.gui.common;

import com.gui.MainFrame;
import com.gui.client.ClientPanel;
import java.awt.Color;
import java.awt.Font;
import javax.swing.*;
import javax.swing.border.TitledBorder;

/**
 * Shared form panel containing the host, port, and password fields used by both the
 * server and client configuration panels.
 *
 * <p>The panel exposes a {@link JComboBox} for host selection (used by the server panel,
 * where the user picks a local network address) and a plain {@link JTextField} for host
 * entry (used by the client panel, where the user types a remote address). Only one of
 * these is visible at a time; each parent panel hides the unused control.</p>
 *
 * <p>Similarly, the password field appears as a plain {@link JTextField} on the server
 * side (so the admin can read the password they set) and as a {@link JPasswordField}
 * on the client side (so the typed password is masked).</p>
 *
 * <p>Callers customise the panel title, label text, and which controls are visible
 * after construction.</p>
 */
public class CommonPanel extends JPanel {

    private static final String FONT_FAMILY = "segoe ui";
    private static final int DEFAULT_PORT = 9999;

    private JLabel hostLabel;
    private JComboBox<String> hostCombo;
    private JTextField hostTextField;
    private JLabel portLabel;
    private JLabel passwordLabel;
    private JTextField portTextField;
    private JTextField passwordTextField;
    private JPasswordField passwordField;
    private JLabel helpLabel;

    /**
     * Creates a new {@code CommonPanel} with default "Server Listening" border title.
     * Parent panels should change the border title and customise visible controls
     * immediately after construction.
     */
    public CommonPanel() {
        this.setBorder(BorderFactory.createTitledBorder(
            null,
            "Server Listening",
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            new Font(FONT_FAMILY, Font.BOLD, 16),
            Color.decode(ClientPanel.FOREGROUND)
        ));
        this.setBounds(50, 30, MainFrame.WIDTH_FRAME - 100, 230);
        this.setBackground(Color.decode(ClientPanel.BACKGROUND));
        this.setForeground(Color.decode(ClientPanel.FOREGROUND));
        this.setLayout(null);

        this.initComponents();
    }

    private void initComponents() {
        this.hostLabel      = new JLabel();
        this.hostCombo      = new JComboBox<>();
        this.hostTextField  = new JTextField();
        this.portLabel      = new JLabel();
        this.passwordLabel  = new JLabel();
        this.portTextField  = new JTextField();
        this.passwordTextField = new JTextField();
        this.passwordField  = new JPasswordField();
        this.helpLabel      = new JLabel();

        // Host label
        this.hostLabel.setText("Server IP:");
        this.hostLabel.setBounds(30, 30, 100, 30);
        this.hostLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, 14));
        this.add(this.hostLabel);

        // Host combo (server panel: pick a local interface)
        this.hostCombo.setBounds(140, 35, 130, 20);
        this.add(this.hostCombo);

        // Host text field (client panel: type a remote address)
        this.hostTextField.setBounds(140, 35, 130, 20);
        this.hostTextField.setVisible(false);
        this.add(this.hostTextField);

        // Port label
        this.portLabel.setText("Server port:");
        this.portLabel.setBounds(30, 60, 100, 30);
        this.portLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, 14));
        this.add(this.portLabel);

        // Port text field
        this.portTextField.setBounds(140, 65, 130, 20);
        this.portTextField.setText(String.valueOf(DEFAULT_PORT));
        this.add(this.portTextField);

        // Password label
        this.passwordLabel.setText("Set password:");
        this.passwordLabel.setBounds(30, 90, 100, 30);
        this.passwordLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, 14));
        this.add(this.passwordLabel);

        // Plain password field (server panel: admin reads the password they set)
        this.passwordTextField.setBounds(140, 95, 130, 20);
        this.add(this.passwordTextField);

        // Masked password field (client panel: user enters remote password)
        this.passwordField.setBounds(140, 95, 130, 20);
        this.passwordField.setVisible(false);
        this.add(this.passwordField);

        // Help text
        this.helpLabel.setText(
            "<html>" +
            "&gt;&gt; Help: send name or IP address and port to your partner.<br>" +
            "&gt;&gt; Example: 192.168.0.1:9999" +
            "</html>"
        );
        this.helpLabel.setBounds(40, 130, 230, 70);
        this.helpLabel.setForeground(Color.decode(ClientPanel.FOREGROUND));
        this.helpLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, 13));
        this.add(this.helpLabel);
    }

    /**
     * Enables or disables all user-editable controls in this panel.
     *
     * @param enabled {@code true} to enable controls, {@code false} to disable them
     */
    @Override
    public void setEnabled(boolean enabled) {
        this.hostCombo.setEnabled(enabled);
        this.hostTextField.setEnabled(enabled);
        this.passwordField.setEnabled(enabled);
        this.passwordTextField.setEnabled(enabled);
        this.portTextField.setEnabled(enabled);
    }

    // -------------------------------------------------------------------------
    // Accessors — used by ServerPanel and ClientPanel to customise this panel
    // -------------------------------------------------------------------------

    /** Returns the host label, allowing the parent to change its text. */
    public JLabel getHostLabel() { return hostLabel; }

    /** Returns the host combo box used on the server panel to pick a local interface. */
    public JComboBox<String> getHostCombo() { return hostCombo; }

    /** Returns the host text field used on the client panel to enter a remote address. */
    public JTextField getHostTextField() { return hostTextField; }

    /** Returns the port label, allowing the parent to change its text. */
    public JLabel getPortLabel() { return portLabel; }

    /** Returns the password label, allowing the parent to change its text. */
    public JLabel getPasswordLabel() { return passwordLabel; }

    /** Returns the port text field. */
    public JTextField getPortTextField() { return portTextField; }

    /**
     * Returns the plain-text password field used on the server panel.
     * The password is visible so the admin can confirm what was set.
     */
    public JTextField getPasswordTextField() { return passwordTextField; }

    /**
     * Returns the masked {@link JPasswordField} used on the client panel.
     * Use {@link JPasswordField#getPassword()} to read the value securely.
     */
    public JPasswordField getPasswordField() { return passwordField; }

    /** Returns the help label, allowing the parent to set context-specific guidance text. */
    public JLabel getHelpLabel() { return helpLabel; }
}
