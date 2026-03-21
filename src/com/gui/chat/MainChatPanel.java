package com.gui.chat;

import com.bus.chat.ChatBus;
import com.bus.common.CommonBus;
import com.gui.MainFrame;
import com.gui.client.ClientPanel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * Container panel that manages all active chat sessions.
 *
 * <p>Each new connection (server or client side) is registered here via
 * {@link #addNewConnection(ChatBus)}, which creates a {@link ChatPanel} and a
 * {@link ConnectionItem} so the user can switch between active sessions using the
 * connections pop-up menu.</p>
 *
 * <p>A connection counter is shown in the top menu bar and updated whenever sessions
 * are added or removed.</p>
 */
public class MainChatPanel extends JPanel {

    private static final String FONT_FAMILY = "segoe ui";

    private JLabel connectionsLabel;
    private JMenuBar menuBar;
    private JPopupMenu popupMenu;

    private final List<ChatPanel> chatPanels;
    private final CommonBus commonBus;
    private int connectionCount;

    /**
     * Creates the main chat panel.
     *
     * @param commonBus the shared transport facade forwarded to each new {@link ChatPanel}
     */
    public MainChatPanel(CommonBus commonBus) {
        this.commonBus   = commonBus;
        this.chatPanels  = new ArrayList<>();
        this.connectionCount = 0;

        this.setLocation(0, MainFrame.HEIGHT_TASKBAR);
        this.setSize(MainFrame.WIDTH_FRAME, MainFrame.HEIGHT_FRAME - MainFrame.HEIGHT_TASKBAR);
        this.setBackground(Color.decode(ClientPanel.BACKGROUND));
        this.setLayout(null);

        this.initComponents();
    }

    private void initComponents() {
        this.menuBar          = new JMenuBar();
        this.connectionsLabel = new JLabel();
        this.popupMenu        = new JPopupMenu();

        updateConnectionsLabel();

        this.connectionsLabel.setBounds(0, 0, 100, 15);
        this.connectionsLabel.setFont(new Font(FONT_FAMILY, Font.PLAIN, 13));
        this.connectionsLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                onConnectionsLabelClicked(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                onConnectionsLabelEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                onConnectionsLabelExited(e);
            }
        });

        this.menuBar.add(this.connectionsLabel);
        this.menuBar.setBounds(0, 0, MainFrame.WIDTH_FRAME, 20);
        this.menuBar.setLayout(new GridBagLayout());
        this.add(this.menuBar);
    }

    /**
     * Registers a new connection and creates the associated UI components.
     *
     * <p>A {@link ChatPanel} and a {@link ConnectionItem} are created, linked, and added
     * to this panel and the pop-up menu respectively.</p>
     *
     * @param chatBus the communication bus for the new connection
     */
    public void addNewConnection(ChatBus chatBus) {
        ChatPanel chatPanel = new ChatPanel(this, this.commonBus, chatBus);
        ConnectionItem item = new ConnectionItem(chatPanel, this.chatPanels);
        chatPanel.setConnectionItem(item);

        this.add(chatPanel);
        this.chatPanels.add(chatPanel);
        this.popupMenu.add(item);

        adjustConnectionCount(1);
        this.validate();
        this.revalidate();
        this.repaint();
    }

    /**
     * Adjusts the connection counter by {@code delta} and refreshes the label.
     * Pass {@code +1} when a connection opens and {@code -1} when it closes.
     *
     * @param delta the amount to add to the current count (may be negative)
     */
    public void adjustConnectionCount(int delta) {
        this.connectionCount += delta;
        updateConnectionsLabel();
    }

    /**
     * Returns the pop-up menu that holds all {@link ConnectionItem}s.
     *
     * @return the connections pop-up menu
     */
    public JPopupMenu getPopupMenu() {
        return popupMenu;
    }

    /**
     * Returns the list of all active {@link ChatPanel}s.
     *
     * @return the list of chat panels
     */
    public List<ChatPanel> getChatPanels() {
        return chatPanels;
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /** Re-renders the connections label with the current count. */
    private void updateConnectionsLabel() {
        this.connectionsLabel.setText(
            "<html>All connections <font color='red'>(" + connectionCount + ")</font></html>"
        );
    }

    private void onConnectionsLabelClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            this.popupMenu.show(this.connectionsLabel, 130, 5);
        }
    }

    private void onConnectionsLabelEntered(MouseEvent e) {
        this.connectionsLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, 13));
        this.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void onConnectionsLabelExited(MouseEvent e) {
        this.connectionsLabel.setFont(new Font(FONT_FAMILY, Font.PLAIN, 13));
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
}
