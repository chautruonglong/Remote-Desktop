package com.gui.remote;

import com.bus.common.CommonBus;
import com.bus.rmi.IRemoteDesktop;
import com.gui.AppStyles;
import com.gui.Icons;
import com.gui.client.ClientPanel;
import com.gui.common.UiSupport;
import com.model.ImageQuality;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Full-screen window that streams the remote server's desktop and forwards local mouse
 * and keyboard events to the server via RMI.
 *
 * <p>A background daemon thread continuously captures screenshots from the server and
 * updates the display label. The thread runs as long as the TCP client connection is active.</p>
 *
 * <p>Scale factors ({@link #scaleX}, {@link #scaleY}) compensate for size differences
 * between the client's screen and the server's screen when translating mouse coordinates.</p>
 *
 * <p>{@link #dispose()} stops the screen thread, resets transport state, and re-enables
 * the {@link ClientPanel} so the user can start a new session.</p>
 */
public class RemoteFrame extends JFrame implements Runnable {
    private static final long MOUSE_EVENT_INTERVAL_NANOS = TimeUnit.MILLISECONDS.toNanos(8);

    private final ClientPanel clientPanel;
    private final CommonBus commonBus;
    private final IRemoteDesktop remoteDesktop;
    private final ImageQuality imageQuality;

    private JLabel screenLabel;
    private JMenuBar menuBar;

    /**
     * Ratio of server screen width to displayed label width.
     * Used to translate client mouse X coordinates into server screen coordinates.
     */
    private float scaleX;

    /**
     * Ratio of server screen height to displayed label height.
     * Used to translate client mouse Y coordinates into server screen coordinates.
     */
    private float scaleY;

    private final Thread screenThread;
    private final ImageIcon screenIcon = new ImageIcon();

    private volatile long lastMouseEventAt;
    private volatile int lastMouseX = Integer.MIN_VALUE;
    private volatile int lastMouseY = Integer.MIN_VALUE;

    /**
     * Creates and opens the remote desktop viewing window.
     *
     * <p>Fetches one screenshot immediately to determine display geometry, then starts
     * the background streaming thread. The {@code clientPanel} is disabled while the
     * session is active.</p>
     *
     * @param clientPanel  the panel to re-enable when the session ends
     * @param commonBus    the shared transport facade
     * @param imageQuality the image format to request from the server
     * @throws Exception if the initial screenshot or RMI setup fails
     */
    public RemoteFrame(ClientPanel clientPanel, CommonBus commonBus, ImageQuality imageQuality)
            throws Exception {
        this.clientPanel   = clientPanel;
        this.commonBus     = commonBus;
        this.imageQuality  = imageQuality;
        this.remoteDesktop = this.commonBus.getRmiClient().getRemoteDesktop();

        this.setTitle("Remoting: " + commonBus.getTcpClient().getSocket().getLocalAddress().getHostName());
        this.setIconImage(Icons.WINDOW.getImage());
        this.getContentPane().setBackground(Color.BLACK);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setLocationRelativeTo(null);
        this.setLayout(null);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }

            @Override
            public void windowOpened(WindowEvent e) {
                clientPanel.setEnabled(false);
            }
        });

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                try {
                    remoteDesktop.pressKey(e.getKeyCode());
                } catch (RemoteException ex) {
                    dispose();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                try {
                    remoteDesktop.releaseKey(e.getKeyCode());
                } catch (RemoteException ex) {
                    dispose();
                }
            }
        });

        this.setVisible(true);

        this.initComponents();
        this.setupDisplayGeometry();

        this.screenThread = UiSupport.startDaemon("remote-screen-stream", this);
    }

    private void initComponents() throws RemoteException {
        this.screenLabel            = new JLabel();
        this.menuBar                = new JMenuBar();
        JMenu monitorMenu           = new JMenu();
        HardwareDialog hardwareDialog = new HardwareDialog(this, this.remoteDesktop);

        this.screenLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.screenLabel.setVerticalAlignment(SwingConstants.CENTER);
        this.screenLabel.setIcon(this.screenIcon);

        this.setMinimumSize(hardwareDialog.getPreferredSize());

        // Screen label — forwards mouse interaction to the server.
        this.screenLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    remoteDesktop.pressMouse(InputEvent.getMaskForButton(e.getButton()));
                } catch (RemoteException ex) {
                    dispose();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    remoteDesktop.releaseMouse(InputEvent.getMaskForButton(e.getButton()));
                } catch (RemoteException ex) {
                    dispose();
                }
            }
        });

        this.addMouseWheelListener(e -> {
            try {
                remoteDesktop.scrollMouseWheel(e.getWheelRotation());
            } catch (RemoteException ex) {
                dispose();
            }
        });

        this.screenLabel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                sendMousePosition(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                sendMousePosition(e);
            }
        });

        this.add(this.screenLabel);

        // Menu bar with the "Show monitor" option.
        this.menuBar.setLayout(new GridBagLayout());
        this.menuBar.setBackground(Color.decode(AppStyles.COLOR_MENU_BAR));
        this.menuBar.setPreferredSize(new Dimension(0, 25));
        this.setJMenuBar(this.menuBar);

        monitorMenu.setText("Show monitor");
        this.menuBar.setFont(new Font(AppStyles.FONT_UI, Font.PLAIN, 14));
        monitorMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) hardwareDialog.setVisible(true);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                monitorMenu.setFont(new Font(AppStyles.FONT_UI, Font.BOLD, 16));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                monitorMenu.setFont(new Font(AppStyles.FONT_UI, Font.PLAIN, 14));
                setCursor(Cursor.getDefaultCursor());
            }
        });
        this.menuBar.add(monitorMenu);
    }

    /**
     * Fetches one screenshot from the server, computes the scale factors needed to map
     * client mouse coordinates to server screen coordinates, and positions the screen label.
     *
     * @throws Exception if the screenshot or geometry computation fails
     */
    private void setupDisplayGeometry() throws Exception {
        int[] remoteScreen = this.remoteDesktop.getScreenDimensions();
        int remoteWidth = remoteScreen[0];
        int remoteHeight = remoteScreen[1];

        Dimension localScreen   = Toolkit.getDefaultToolkit().getScreenSize();
        Insets    frameInsets   = this.getInsets();
        Insets    taskbarInsets = Toolkit.getDefaultToolkit().getScreenInsets(this.getGraphicsConfiguration());

        int availWidth  = localScreen.width  - (taskbarInsets.left + taskbarInsets.right);
        int availHeight = localScreen.height - (taskbarInsets.top  + taskbarInsets.bottom + frameInsets.top + this.menuBar.getPreferredSize().height);

        if (availWidth > remoteWidth && availHeight > remoteHeight) {
            // The server screen is smaller — center it with no scaling.
            int hGap = (availWidth  - remoteWidth)  / 2;
            int vGap = (availHeight - remoteHeight) / 2;
            this.scaleX = 1f;
            this.scaleY = 1f;
            this.screenLabel.setBounds(hGap, vGap, remoteWidth, remoteHeight);
        } else {
            // The server screen is larger — scale to fit, preserving an aspect ratio.
            float ratio      = (float) remoteWidth / remoteHeight;
            int scaledWidth  = Math.min(availWidth, Math.max(1, (int) (ratio * availHeight)));
            int scaledHeight = Math.max(1, Math.min(availHeight, (int) (scaledWidth / ratio)));
            int hGap         = (availWidth - scaledWidth) / 2;
            int vGap         = (availHeight - scaledHeight) / 2;
            this.scaleX = (float) remoteWidth  / scaledWidth;
            this.scaleY = (float) remoteHeight / scaledHeight;
            this.screenLabel.setBounds(hGap, vGap, scaledWidth, scaledHeight);
        }
    }

    /**
     * Screen streaming loop — repeatedly fetches screenshots and updates the display label
     * while the TCP client connection is active. Disposes the frame on disconnection or error.
     */
    @Override
    public void run() {
        int width  = this.screenLabel.getWidth();
        int height = this.screenLabel.getHeight();
        long frameIntervalNanos = TimeUnit.SECONDS.toNanos(1) / this.imageQuality.getTargetFps();
        try {
            while (this.commonBus.isClientConnected()) {
                long frameStartedAt = System.nanoTime();
                byte[] screenshotBytes = this.remoteDesktop.captureScreen(this.imageQuality, width, height);
                BufferedImage frame = ImageIO.read(new ByteArrayInputStream(screenshotBytes));
                if (frame != null) {
                    UiSupport.runOnEdt(() -> {
                        this.screenIcon.setImage(frame);
                        this.screenLabel.repaint();
                    });
                }
                sleepForFrameBudget(frameStartedAt, frameIntervalNanos);
            }
        } catch (Exception e) {
            // Connection dropped, or session ended — fall through to dispose.
        } finally {
            this.dispose();
        }
    }

    /**
     * Closes the session: stops the screen thread, resets transport state, re-enables the
     * client panel, and releases all window resources.
     */
    @Override
    public void dispose() {
        if (this.screenThread != null && !this.screenThread.isInterrupted()) {
            this.screenThread.interrupt();
        }
        try {
            this.commonBus.getRmiClient().setConnected(false);
            this.commonBus.getTcpClient().setConnected(false);
            if (this.commonBus.getTcpClient().getSocket() != null) {
                this.commonBus.getTcpClient().getSocket().close();
            }
        } catch (IOException ex) {
            UiSupport.logError("Error closing client socket on dispose", ex);
        }
        this.clientPanel.setEnabled(true);
        super.setVisible(false);
        super.dispose();
    }

    // -------------------------------------------------------------------------
    // Mouse coordinate translation helper
    // -------------------------------------------------------------------------

    /**
     * Translates a client-side mouse event position to server screen coordinates and
     * forwards a mouse-move RMI call.
     *
     * @param e the mouse event from the screen label
     */
    private void sendMousePosition(MouseEvent e) {
        long now = System.nanoTime();
        int serverX = (int) (e.getX() * scaleX);
        int serverY = (int) (e.getY() * scaleY);
        if (serverX == this.lastMouseX && serverY == this.lastMouseY) {
            return;
        }
        if (now - this.lastMouseEventAt < MOUSE_EVENT_INTERVAL_NANOS) {
            return;
        }

        try {
            this.remoteDesktop.moveMouse(serverX, serverY);
            this.lastMouseX = serverX;
            this.lastMouseY = serverY;
            this.lastMouseEventAt = now;
        } catch (RemoteException ex) {
            dispose();
        }
    }

    private static void sleepForFrameBudget(long frameStartedAt, long frameIntervalNanos) throws InterruptedException {
        long elapsedNanos = System.nanoTime() - frameStartedAt;
        long remainingNanos = frameIntervalNanos - elapsedNanos;
        if (remainingNanos > 0) {
            Thread.sleep(Duration.ofNanos(remainingNanos));
        }
    }
}
