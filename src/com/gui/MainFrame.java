package com.gui;

import com.bus.common.CommonBus;
import com.gui.chat.MainChatPanel;
import com.gui.client.ClientPanel;
import com.gui.common.CommonLabel;
import com.gui.server.ServerPanel;
import com.model.Tab;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * The application's main window.
 *
 * <p>Contains a tab bar with three {@link Tab} entries (Client, Server, Chat), each
 * backed by a dedicated panel. Switching tabs triggers a horizontal slide animation.
 * The window cleans up server resources on close.</p>
 *
 * <p>Image I/O disk caching is disabled at startup to ensure all screen captures and
 * icon loads use memory-only buffers.</p>
 */
public class MainFrame extends JFrame {

    /** Fixed window width in pixels. */
    public static final int WIDTH_FRAME = 400;

    /** Fixed window height in pixels. */
    public static final int HEIGHT_FRAME = 420;

    /** Height of the top tab bar in pixels. */
    public static final int HEIGHT_TASKBAR = 50;

    /** Hex colour string for the tab bar background. */
    public static final String TASKBAR_BACKGROUND = "0x000942";

    private final CommonBus commonBus;

    private JPanel taskbarPanel;
    private CommonLabel clientLabel;
    private CommonLabel serverLabel;
    private CommonLabel chatLabel;
    private ClientPanel clientPanel;
    private ServerPanel serverPanel;
    private MainChatPanel mainChatPanel;

    /** Maps each Tab constant to its corresponding content panel. */
    private final Map<Tab, JPanel> tabPanels = new EnumMap<>(Tab.class);

    /** The currently active (selected) tab. */
    private Tab activeTab;

    /**
     * Constructs and displays the main application window.
     *
     * @throws IOException if any icon resource cannot be loaded
     */
    public MainFrame() throws IOException {
        // Disable ImageIO disk caching for the whole application lifetime.
        ImageIO.setUseCache(false);

        // Update UIManager defaults for disabled label/radio button colours.
        UIManager.put("Label.disabledForeground", Color.decode("0xD3D3D3"));
        UIManager.put("RadioButton.disabledText", Color.decode("0xD3D3D3"));

        this.getContentPane().setPreferredSize(new Dimension(WIDTH_FRAME, HEIGHT_FRAME));
        this.pack();
        this.setLayout(null);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setTitle("Remote Desktop Software");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setIconImage(Icons.WINDOW.getImage());

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onWindowClosing(e);
            }
        });

        this.commonBus = new CommonBus();
        this.initComponents();
        this.setVisible(true);
    }

    private void initComponents() {
        // Construct child panels.
        this.taskbarPanel   = new JPanel();
        this.clientLabel    = new CommonLabel();
        this.serverLabel    = new CommonLabel();
        this.chatLabel      = new CommonLabel();
        this.clientPanel    = new ClientPanel(this.commonBus);
        this.serverPanel    = new ServerPanel(this.commonBus);
        this.mainChatPanel  = new MainChatPanel(this.commonBus);

        this.commonBus.setMainChatPanel(this.mainChatPanel);

        // Register tab–panel mappings.
        this.tabPanels.put(Tab.CLIENT, this.clientPanel);
        this.tabPanels.put(Tab.SERVER, this.serverPanel);
        this.tabPanels.put(Tab.CHAT, this.mainChatPanel);
        this.activeTab = Tab.CLIENT;

        // Tab bar layout — three equal columns.
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{ 100, 100, 100 };
        this.taskbarPanel.setLayout(gridBagLayout);
        this.taskbarPanel.setBackground(Color.decode(TASKBAR_BACKGROUND));
        this.taskbarPanel.setBounds(0, 0, WIDTH_FRAME, HEIGHT_TASKBAR);
        this.add(this.taskbarPanel);

        // Client tab label.
        this.clientLabel.setText("Client");
        this.clientLabel.setBigFont();
        this.clientLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onTabClicked(e, clientLabel, Tab.CLIENT);
            }
        });
        this.taskbarPanel.add(this.clientLabel);

        // Server tab label.
        this.serverLabel.setText("Server");
        this.serverLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onTabClicked(e, serverLabel, Tab.SERVER);
            }
        });
        this.taskbarPanel.add(this.serverLabel);

        // Chat tab label.
        this.chatLabel.setText("Chat");
        this.chatLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onTabClicked(e, chatLabel, Tab.CHAT);
            }
        });
        this.taskbarPanel.add(this.chatLabel);

        // Show only the client panel initially.
        this.clientPanel.setVisible(true);
        this.serverPanel.setVisible(false);
        this.mainChatPanel.setVisible(false);
        this.add(this.clientPanel);
        this.add(this.serverPanel);
        this.add(this.mainChatPanel);
    }

    /**
     * Handles the window-closing event by stopping the server gracefully.
     * Errors are logged but not shown to the user since the application is already closing.
     *
     * @param e the window event
     */
    private void onWindowClosing(WindowEvent e) {
        try {
            this.commonBus.stopServer();
        } catch (IOException | NotBoundException ex) {
            System.err.println("Error while stopping server on window close: " + ex.getMessage());
        }
    }

    /**
     * Handles a click on one of the three tab labels.
     *
     * <p>If the clicked tab is already active, nothing happens. Otherwise the panel
     * transitions with a horizontal slide animation and the label font sizes are updated.</p>
     *
     * @param e     the mouse event
     * @param label the label that was clicked
     * @param tab   the {@link Tab} constant associated with that label
     */
    private void onTabClicked(MouseEvent e, CommonLabel label, Tab tab) {
        if (e.getButton() != MouseEvent.BUTTON1) return;
        if (tab == activeTab) return;

        JPanel showPanel = tabPanels.get(tab);
        JPanel hidePanel = tabPanels.get(activeTab);
        boolean slideFromRight = tab.getIndex() > activeTab.getIndex();
        slidePanel(showPanel, hidePanel, slideFromRight);

        // Update tab label styles.
        activeTab = tab;
        clientLabel.setSmallFont();
        serverLabel.setSmallFont();
        chatLabel.setSmallFont();
        label.setBigFont();
    }

    /**
     * Animates a horizontal panel switch.
     *
     * <p>The incoming panel slides in from the right (if {@code fromRight} is {@code true})
     * or from the left. Both panels move simultaneously at 50-pixel steps every 10 ms.</p>
     *
     * @param showPanel the panel to slide into view
     * @param hidePanel the panel to slide out of view
     * @param fromRight {@code true} to slide the new panel in from the right
     */
    private void slidePanel(JPanel showPanel, JPanel hidePanel, boolean fromRight) {
        showPanel.setVisible(true);

        AtomicInteger hideX = new AtomicInteger(0);
        AtomicInteger showX = new AtomicInteger(fromRight ? WIDTH_FRAME : -WIDTH_FRAME);
        AtomicInteger step  = new AtomicInteger(fromRight ? -50 : 50);

        Timer timer = new Timer(10, e -> {
            hidePanel.setLocation(hideX.get(), HEIGHT_TASKBAR);
            showPanel.setLocation(showX.get(), HEIGHT_TASKBAR);

            if (showX.get() == 0) {
                ((Timer) e.getSource()).stop();
                hidePanel.setVisible(false);
                return;
            }

            showX.addAndGet(step.get());
            hideX.addAndGet(step.get());
        });
        timer.start();
    }
}
