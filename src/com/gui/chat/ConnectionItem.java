package com.gui.chat;

import com.gui.Icons;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JMenuItem;

/**
 * A menu item in the connections pop-up that represents one active chat session.
 *
 * <p>Clicking this item hides all other {@link ChatPanel}s and shows only the one
 * associated with this connection, effectively "switching" to that session.</p>
 *
 * <p>The item displays the remote host's name as its label and an online status icon.</p>
 */
public class ConnectionItem extends JMenuItem {

    private final ChatPanel chatPanel;
    private final List<ChatPanel> allChatPanels;

    /**
     * Creates a new {@code ConnectionItem} linked to the given chat panel.
     *
     * @param chatPanel     the chat session this item represents
     * @param allChatPanels the full list of active chat panels (used to hide all others on click)
     */
    public ConnectionItem(ChatPanel chatPanel, List<ChatPanel> allChatPanels) {
        this.chatPanel     = chatPanel;
        this.allChatPanels = allChatPanels;

        this.setIcon(Icons.ONLINE);
        this.setText(chatPanel.getRemoteHostName());

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                onItemClicked(e);
            }
        });
    }

    /**
     * Hides all chat panels except the one associated with this item, then shows it.
     */
    private void onItemClicked(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1) return;

        for (ChatPanel panel : allChatPanels) {
            panel.setVisible(false);
        }
        this.chatPanel.setVisible(true);
    }
}
