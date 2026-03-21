package com.gui.chat;

import com.bus.chat.ChatBus;
import com.bus.chat.FileMessage;
import com.bus.chat.Message;
import com.bus.chat.StringMessage;
import com.bus.common.CommonBus;
import com.gui.MainFrame;
import com.gui.client.ClientPanel;
import com.gui.common.CommonLabel;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

/**
 * Panel for a single peer-to-peer chat session over an established {@link ChatBus}.
 *
 * <p>Displays an incoming/outgoing message history, a text input area, and send/file buttons.
 * A background daemon thread continuously reads incoming {@link Message}s from the bus and
 * appends them to the history panel.</p>
 *
 * <p>When the connection drops (detected by the receive thread catching an exception), all
 * associated UI components are cleaned up and the parent {@link MainChatPanel} is notified.</p>
 *
 * <p>The local hostname is resolved once at construction and cached to avoid repeated
 * blocking DNS lookups during message sends.</p>
 *
 * <p>File sends are limited to {@value #MAX_FILE_SIZE_BYTES} bytes. Files are read and
 * transferred on a daemon thread so the EDT is not blocked.</p>
 */
public class ChatPanel extends JPanel implements Runnable {

    /** Background colour for the message history area. */
    public static final String CONTENT_BACKGROUND = "0xFFFFFF";

    /** Maximum characters per line before wrapping in the history display. */
    public static final int MAX_LINE_LENGTH = 20;

    /** Maximum file size (30 MB) allowed for file transfers. */
    public static final long MAX_FILE_SIZE_BYTES = 30L * 1024 * 1024;

    private static final String FONT_FAMILY_MONO = "consolas";
    private static final String FONT_FAMILY_UI   = "segoe ui";

    private JPanel contentPanel;
    private JScrollPane scrollPanel;
    private JTextArea messageTextArea;
    private JScrollPane scrollText;
    private CommonLabel sendLabel;
    private CommonLabel fileLabel;
    private JLabel loaderLabel;

    private GroupLayout contentLayout;
    private GroupLayout.ParallelGroup horizontalGroup;
    private GroupLayout.SequentialGroup verticalGroup;

    private final MainChatPanel parentPanel;
    private final CommonBus commonBus;
    private final ChatBus chatBus;

    /** Cached local hostname — resolved once to avoid repeated blocking DNS calls. */
    private final String localHostName;

    /** The menu item in the connections pop-up that links to this panel. */
    private ConnectionItem connectionItem;

    private final Thread receiveThread;

    /**
     * Creates a new {@code ChatPanel} for the given connection.
     *
     * @param parentPanel the {@link MainChatPanel} that owns this session
     * @param commonBus   the shared transport facade used to check connection state
     * @param chatBus     the communication bus for this session
     */
    public ChatPanel(MainChatPanel parentPanel, CommonBus commonBus, ChatBus chatBus) {
        this.parentPanel = parentPanel;
        this.commonBus   = commonBus;
        this.chatBus     = chatBus;
        this.localHostName = resolveLocalHostName();

        this.setLocation(0, MainFrame.HEIGHT_TASKBAR - 42);
        this.setSize(MainFrame.WIDTH_FRAME, MainFrame.HEIGHT_FRAME - MainFrame.HEIGHT_TASKBAR);
        this.setBackground(Color.decode(ClientPanel.BACKGROUND));
        this.setLayout(null);

        this.initComponents();

        this.receiveThread = new Thread(this);
        this.receiveThread.setDaemon(true);
        this.receiveThread.start();
    }

    private void initComponents() {
        this.contentPanel  = new JPanel();
        this.scrollPanel   = new JScrollPane();
        this.messageTextArea = new JTextArea();
        this.scrollText    = new JScrollPane();
        this.sendLabel     = new CommonLabel();
        this.fileLabel     = new CommonLabel();
        this.loaderLabel   = new JLabel();

        // Message history scroll area.
        this.contentLayout   = new GroupLayout(this.contentPanel);
        this.horizontalGroup = this.contentLayout.createParallelGroup();
        this.verticalGroup   = this.contentLayout.createSequentialGroup();
        this.contentLayout.setHorizontalGroup(this.horizontalGroup);
        this.contentLayout.setVerticalGroup(this.verticalGroup);

        this.contentPanel.setBackground(Color.decode(CONTENT_BACKGROUND));
        this.contentPanel.setLayout(this.contentLayout);

        this.scrollPanel.setViewportView(this.contentPanel);
        this.scrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.scrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.scrollPanel.setBounds(10, 15, MainFrame.WIDTH_FRAME - 20, MainFrame.HEIGHT_FRAME - 150);
        this.add(this.scrollPanel);

        // Text input.
        this.messageTextArea.setFont(new Font(FONT_FAMILY_MONO, Font.PLAIN, 14));
        this.messageTextArea.setLineWrap(true);
        this.messageTextArea.getDocument().putProperty("filterNewlines", Boolean.TRUE);
        this.messageTextArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) sendMessage();
            }
        });

        this.scrollText = new JScrollPane(this.messageTextArea);
        this.scrollText.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.scrollText.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.scrollText.setBounds(
            30,
            this.scrollPanel.getHeight() + 20,
            MainFrame.WIDTH_FRAME - 150,
            MainFrame.HEIGHT_FRAME - MainFrame.HEIGHT_TASKBAR - this.scrollPanel.getHeight() - 30
        );
        this.add(this.scrollText);

        // "Send" button.
        this.sendLabel.setText("Send");
        this.sendLabel.setIcon(new ImageIcon(getClass().getClassLoader().getResource("send_icon.png")));
        this.sendLabel.setForeground(Color.decode(ClientPanel.FOREGROUND));
        this.sendLabel.setFont(new Font(FONT_FAMILY_UI, Font.PLAIN, 13));
        this.sendLabel.setBounds(this.scrollText.getWidth() + 50, this.scrollText.getY(), 80, 30);
        this.sendLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && sendLabel.isEnabled()) sendMessage();
            }
        });
        this.add(this.sendLabel);

        // "File" button.
        this.fileLabel.setText("File");
        this.fileLabel.setIcon(new ImageIcon(getClass().getClassLoader().getResource("file_icon.png")));
        this.fileLabel.setForeground(Color.decode(ClientPanel.FOREGROUND));
        this.fileLabel.setFont(new Font(FONT_FAMILY_UI, Font.PLAIN, 13));
        this.fileLabel.setBounds(this.scrollText.getWidth() + 50, this.scrollText.getY() + 40, 60, 30);
        this.fileLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    onFileLabelClicked(e);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Could not send file:\n" + ex.getMessage());
                }
            }
        });
        this.add(this.fileLabel);

        // Loading spinner.
        this.loaderLabel.setIcon(
            new ImageIcon(getClass().getClassLoader().getResource("loader_icon2.gif"))
        );
        this.loaderLabel.setBounds(this.fileLabel.getX() + 60, this.fileLabel.getY() + 7, 16, 16);
        this.loaderLabel.setVisible(false);
        this.add(this.loaderLabel);
    }

    /**
     * Returns the remote host's display name, used as the label for the connection menu item.
     *
     * @return the remote hostname
     */
    public String getRemoteHostName() {
        return this.chatBus.getSocket().getInetAddress().getHostName();
    }

    /**
     * Stores the {@link ConnectionItem} associated with this panel so it can be removed
     * from the pop-up menu when the connection closes.
     *
     * @param item the menu item for this session
     */
    public void setConnectionItem(ConnectionItem item) {
        this.connectionItem = item;
    }

    /**
     * Enables or disables the send and file action controls.
     *
     * @param enabled {@code true} to enable, {@code false} to disable
     */
    @Override
    public void setEnabled(boolean enabled) {
        this.messageTextArea.setEnabled(enabled);
        this.sendLabel.setEnabled(enabled);
        this.fileLabel.setEnabled(enabled);
    }

    /**
     * Scrolls the history to the bottom when this panel becomes visible.
     *
     * @param visible {@code true} to show the panel
     */
    @Override
    public void setVisible(boolean visible) {
        scrollToBottom();
        super.setVisible(visible);
    }

    // -------------------------------------------------------------------------
    // Message sending
    // -------------------------------------------------------------------------

    /**
     * Reads the text area content and sends it as a {@link StringMessage} to the peer.
     * The text area is cleared on success. Empty/blank inputs are ignored.
     */
    private void sendMessage() {
        try {
            String content = this.messageTextArea.getText();
            if (content.trim().isEmpty()) return;

            StringMessage message = new StringMessage(this.localHostName, content);
            this.chatBus.sendMessage(message);

            int rightGap = this.scrollPanel.getWidth() - 180;
            JLabel label = new JLabel("You sent a message:" + content);
            label.setFont(new Font(FONT_FAMILY_MONO, Font.PLAIN, 14));
            label.setForeground(Color.BLACK);
            addMessageToPanel(label, rightGap, "green");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not send message:\n" + ex.getMessage());
        } finally {
            this.messageTextArea.setText("");
        }
    }

    /**
     * Opens a file chooser, validates the selected file's size, then sends it on a
     * background thread as a {@link FileMessage}. Uses try-with-resources to prevent leaks.
     *
     * @throws IOException if the file is too large
     */
    private void onFileLabelClicked(MouseEvent e) throws IOException {
        if (e.getButton() != MouseEvent.BUTTON1 || !this.fileLabel.isEnabled()) return;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.showDialog(this, "Send");

        File selectedFile = fileChooser.getSelectedFile();
        if (selectedFile == null) return;
        if (selectedFile.length() > MAX_FILE_SIZE_BYTES) {
            throw new IOException("File too large — maximum allowed size is 30 MB");
        }

        this.loaderLabel.setVisible(true);
        this.setEnabled(false);

        Thread sendThread = new Thread(() -> {
            try (FileInputStream fis = new FileInputStream(selectedFile)) {
                FileMessage fileMessage = new FileMessage(
                    this.localHostName,
                    selectedFile.getName(),
                    selectedFile.length(),
                    fis.readAllBytes()
                );
                this.chatBus.sendMessage(fileMessage);

                int rightGap = this.scrollPanel.getWidth() - 180;
                fileMessage.setSender("You");
                FileLabel fileLabel = new FileLabel(fileMessage);
                addMessageToPanel(fileLabel, rightGap, "green");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Could not send file:\n" + ex.getMessage());
            } finally {
                this.setEnabled(true);
                this.loaderLabel.setVisible(false);
            }
        });
        sendThread.setDaemon(true);
        sendThread.start();
    }

    // -------------------------------------------------------------------------
    // Message history rendering
    // -------------------------------------------------------------------------

    /**
     * Adds a message label to the scrollable history panel on the EDT.
     *
     * <p>The label text is formatted with a coloured header line and line-wrapped body.
     * After adding, the scroll position is set to the bottom so the newest message is
     * always visible.</p>
     *
     * @param label       the label to add (may be a plain {@link JLabel} or a {@link FileLabel})
     * @param rightGap    horizontal gap from the right (larger for sent, 0 for received)
     * @param headerColor HTML colour string for the sender header (e.g., {@code "green"} or {@code "blue"})
     */
    public void addMessageToPanel(JLabel label, int rightGap, String headerColor) {
        EventQueue.invokeLater(() -> {
            label.setText(formatMessage(label.getText(), headerColor));

            this.horizontalGroup.addGroup(
                this.contentLayout.createSequentialGroup()
                    .addContainerGap(rightGap, rightGap)
                    .addComponent(label)
            );
            this.verticalGroup.addComponent(label).addGap(10, 10, 10);
            this.scrollPanel.revalidate();

            scrollToBottom();
        });
    }

    /**
     * Wraps a raw message string in HTML with a coloured sender header and line-broken body.
     *
     * @param rawText     the original label text
     * @param headerColor the HTML colour name for the header
     * @return an HTML string suitable for use in a {@link JLabel}
     */
    private String formatMessage(String rawText, String headerColor) {
        return "<html>" + buildHeader(rawText, headerColor) + buildWrappedBody(rawText) + "</html>";
    }

    /**
     * Extracts the portion of {@code text} up to and including the first colon, wraps it
     * in a coloured HTML {@code <font>} tag, and appends a line break.
     * Returns an empty string if no colon is present.
     */
    private String buildHeader(String text, String color) {
        if (!text.contains(":")) return "";
        String header = text.substring(0, text.indexOf(':') + 1);
        return "<font color='" + color + "'>" + header + "</font><br>";
    }

    /**
     * Extracts the body portion of {@code text} (after the first colon) and inserts
     * {@code <br>} tags every {@link #MAX_LINE_LENGTH} characters for readability.
     */
    private String buildWrappedBody(String text) {
        String body = text.substring(text.indexOf(':') + 1);
        if (body.length() <= MAX_LINE_LENGTH) return body;

        StringBuilder wrapped = new StringBuilder();
        int index = 0;
        while (index + MAX_LINE_LENGTH < body.length()) {
            wrapped.append(body, index, index + MAX_LINE_LENGTH).append("<br>");
            index += MAX_LINE_LENGTH;
        }
        wrapped.append(body.substring(index));
        return wrapped.toString();
    }

    /**
     * Scrolls the message history to the bottom. Called twice in succession because Swing's
     * layout may not have completed the first time, and the second call ensures the scroll
     * bar maximum is up-to-date.
     */
    private void scrollToBottom() {
        this.scrollPanel.getViewport().setViewPosition(
            new Point(0, this.scrollPanel.getVerticalScrollBar().getMaximum())
        );
        this.scrollPanel.getViewport().setViewPosition(
            new Point(0, this.scrollPanel.getVerticalScrollBar().getMaximum())
        );
    }

    // -------------------------------------------------------------------------
    // Receive loop
    // -------------------------------------------------------------------------

    /**
     * Background receive loop — blocks on {@link ChatBus#receiveMessage()} and dispatches
     * each received message to the history panel.
     *
     * <p>If neither server nor client is active, the thread sleeps 1 second between checks
     * to avoid busy-waiting. When an exception is thrown (connection dropped), the session
     * is cleaned up and the loop exits.</p>
     */
    @Override
    public void run() {
        while (true) {
            try {
                if (this.commonBus.hasConnectedClient() || this.commonBus.isClientConnected()) {
                    Message message = this.chatBus.receiveMessage();
                    if (message instanceof StringMessage) {
                        StringMessage stringMessage = (StringMessage) message;
                        JLabel label = new JLabel(
                            stringMessage.getSender() + " sent a message:" + stringMessage.getContent()
                        );
                        label.setFont(new Font(FONT_FAMILY_MONO, Font.PLAIN, 14));
                        label.setForeground(Color.BLACK);
                        addMessageToPanel(label, 0, "blue");
                    } else if (message instanceof FileMessage) {
                        FileMessage fileMessage = (FileMessage) message;
                        addMessageToPanel(new FileLabel(fileMessage), 0, "blue");
                    }
                } else {
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                handleDisconnect();
                break;
            }
        }
    }

    /**
     * Cleans up after the connection drops: resets transport state, removes this panel and
     * its menu item from the parent, and closes the underlying socket.
     */
    private void handleDisconnect() {
        this.commonBus.getTcpServer().setHasConnectedClient(false);
        this.commonBus.getTcpClient().setConnected(false);

        this.parentPanel.remove(this);
        this.parentPanel.getPopupMenu().remove(this.connectionItem);
        this.parentPanel.adjustConnectionCount(-1);
        this.parentPanel.getChatPanels().remove(this);
        this.parentPanel.validate();
        this.parentPanel.revalidate();
        this.parentPanel.repaint();

        try {
            this.chatBus.getSocket().close();
        } catch (IOException ex) {
            System.err.println("Error closing chat socket: " + ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /**
     * Resolves the local machine's hostname once at startup. Falls back to
     * {@code "localhost"} if the lookup fails.
     *
     * @return the local hostname string
     */
    private static String resolveLocalHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            System.err.println("Could not resolve local hostname, using 'localhost': " + e.getMessage());
            return "localhost";
        }
    }
}
