package com.gui;

import com.bus.CommonBus;
import com.bus.IRemoteDesktop;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class RemoteFrame extends JFrame implements Runnable {
    private ClientPanel client_panel;
    private JLabel screen_label;
    private CommonBus common_bus;
    private IRemoteDesktop remote_obj;

    private Dimension screen_size;
    private Insets frame_insets;
    private Insets taskbar_insets;
    private String quality;

    // TODO: properties for remote larger screen
    private float dx;
    private float dy;

    public RemoteFrame(ClientPanel client_panel, CommonBus common_bus, String quality) throws Exception {
        this.quality = quality;
        this.client_panel = client_panel;
        this.common_bus = common_bus;
        this.remote_obj = this.common_bus.getRmiClient().getRemoteObject();

        this.setTitle("You are remoting " + this.common_bus.getTcpClient().getClient().getLocalAddress().getHostName());
        this.setIconImage(new ImageIcon(this.getClass().getClassLoader().getResource("window_icon.png")).getImage());
        this.getContentPane().setBackground(Color.BLACK);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setLocationRelativeTo(null);
        this.setLayout(null);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    remoteFrameWindowClosing(e);
                }
                catch(Exception exception) {
                    JOptionPane.showMessageDialog(null, "Can't close connection");
                }
            }

            @Override
            public void windowOpened(WindowEvent e) {
                remoteFrameWindowOpened(e);
            }
        });
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                try {
                    remoteFrameKeyPressed(e);
                }
                catch(RemoteException remoteException) {
                    dispose();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                try {
                    remoteFrameKeyReleased(e);
                }
                catch(RemoteException remoteException) {
                    dispose();
                }
            }
        });
        this.setVisible(true);

        this.screen_size = Toolkit.getDefaultToolkit().getScreenSize();
        this.frame_insets = this.getInsets();
        this.taskbar_insets = Toolkit.getDefaultToolkit().getScreenInsets(this.getGraphicsConfiguration());

        // TODO: events
        this.screen_label = new JLabel();
        this.screen_label.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    screenLabelMousePressed(e);
                }
                catch(RemoteException remoteException) {
                    dispose();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    screenLabelMouseReleased(e);
                }
                catch(RemoteException remoteException) {
                    dispose();
                }
            }
        });
        this.addMouseWheelListener((e) -> {
            try {
                screenLabelMouseWheelMoved(e);
            }
            catch(RemoteException remoteException) {
                dispose();
            }
        });
        this.screen_label.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                try {
                    screenLabelMouseMoved(e);
                }
                catch(RemoteException remoteException) {
                    dispose();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                try {
                    screenLabelMouseDragged(e);
                }
                catch(RemoteException remoteException) {
                    dispose();
                }
            }
        });
        this.add(this.screen_label);

        // TODO: setup window
        this.setupWindow();

        // TODO: start thread to share partner's screen
        new Thread(this).start();
    }

    private void setupWindow() throws Exception {
        ImageIO.setUseCache(false);
        byte[] dgram = this.remote_obj.takeScreenshotServer(quality);
        ByteArrayInputStream bis = new ByteArrayInputStream(dgram);
        BufferedImage screenshot = ImageIO.read(bis);

        this.screen_size.width -= (this.taskbar_insets.left + this.taskbar_insets.right);
        this.screen_size.height -= (this.taskbar_insets.top + this.taskbar_insets.bottom + this.frame_insets.top);

        // TODO: your screen lager than partner's screen
        if(this.screen_size.width > screenshot.getWidth() && this.screen_size.height > screenshot.getHeight()) {
            int h_gap = (this.screen_size.width - screenshot.getWidth()) / 2;
            int v_gap = (this.screen_size.height - screenshot.getHeight()) / 2;

            this.dx = 1;
            this.dy = 1;
            this.screen_label.setBounds(h_gap, v_gap, screenshot.getWidth(), screenshot.getHeight());
        }
        // TODO: your screen smaller than partner's screen
        else {
            this.dx = (float) screenshot.getWidth() / this.screen_size.width;
            this.dy = (float) screenshot.getHeight() / this.screen_size.height;
            this.screen_label.setBounds(0, 0, this.screen_size.width, this.screen_size.height);
        }
    }

    @Override
    public void run() {
        int width = this.screen_label.getWidth();
        int height = this.screen_label.getHeight();
        while(this.common_bus.getTcpClient().isConnectedServer()) {
            try {
                byte[] dgram = this.remote_obj.takeScreenshotServer(quality);
                ByteArrayInputStream bis = new ByteArrayInputStream(dgram);
                Image screenshot = ImageIO.read(bis).getScaledInstance(width, height, Image.SCALE_SMOOTH);
                this.screen_label.setIcon(new ImageIcon(screenshot));
            }
            catch(Exception e) {
                this.dispose();
            }
        }
    }

    @Override
    public void dispose() {
        try {
            remoteFrameWindowClosing(null);
        }
        catch(IOException exception) {
            JOptionPane.showMessageDialog(null, "Can't close connection");
        }
        super.dispose();
    }

    private void remoteFrameWindowClosing(WindowEvent e) throws IOException {
        this.client_panel.setEnabled(true);
        this.common_bus.getRmiClient().setIsRemoteServer(false);
        this.common_bus.getTcpClient().setConnectedServer(false);
        this.common_bus.getTcpClient().getClient().close();
    }

    private void remoteFrameWindowOpened(WindowEvent e) {
        this.client_panel.setEnabled(false);
    }

    // TODO: remote keyboard of server
    private void remoteFrameKeyPressed(KeyEvent e) throws RemoteException {
        this.remote_obj.keyPressedServer(e.getKeyCode());
    }

    private void remoteFrameKeyReleased(KeyEvent e) throws RemoteException {
        this.remote_obj.keyReleasedServer(e.getKeyCode());
    }

    // TODO: remote mouse of server
    private void screenLabelMousePressed(MouseEvent e) throws RemoteException {
        this.remote_obj.mousePressedServer(InputEvent.getMaskForButton(e.getButton()));
    }

    private void screenLabelMouseReleased(MouseEvent e) throws RemoteException {
        this.remote_obj.mouseReleasedServer(InputEvent.getMaskForButton(e.getButton()));
    }

    private void screenLabelMouseMoved(MouseEvent e) throws RemoteException {
        float x = e.getX() * dx;
        float y = e.getY() * dy;
        this.remote_obj.mouseMovedServer((int) x, (int) y);
    }

    private void screenLabelMouseDragged(MouseEvent e) throws RemoteException {
        float x = e.getX() * dx;
        float y = e.getY() * dy;
        this.remote_obj.mouseMovedServer((int) x, (int) y);
    }

    private void screenLabelMouseWheelMoved(MouseWheelEvent e) throws RemoteException {
        this.remote_obj.mouseWheelServer(e.getWheelRotation());
    }
}