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
import javax.swing.*;

public class RemoteFrame extends JFrame implements Runnable {
    private ClientPanel client_panel;
    private CommonBus common_bus;
    private IRemoteDesktop remote_obj;

    private JLabel screen_label;
    private JMenuBar menu_bar;
    private JMenu menu_monitor;
    private HardwareDialog hardware_dialog;

    private Dimension screen_size;
    private Insets frame_insets;
    private Insets taskbar_insets;
    private String quality;

    // TODO: properties for remote larger screen
    private float dx;
    private float dy;

    private volatile Thread screen_thread;

    public RemoteFrame(ClientPanel client_panel, CommonBus common_bus, String quality) throws Exception {
        this.setTitle("You are remoting " + common_bus.getTcpClient().getClient().getLocalAddress().getHostName());
        this.setIconImage(new ImageIcon(this.getClass().getClassLoader().getResource("window_icon.png")).getImage());
        this.getContentPane().setBackground(Color.BLACK);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setLocationRelativeTo(null);
        this.setLayout(null);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    remoteFrameWindowClosing(e);
                }
                catch(Exception exception) {
                    dispose();
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

        this.quality = quality;
        this.client_panel = client_panel;
        this.common_bus = common_bus;
        this.remote_obj = this.common_bus.getRmiClient().getRemoteObject();
        this.screen_size = Toolkit.getDefaultToolkit().getScreenSize();
        this.frame_insets = this.getInsets();
        this.taskbar_insets = Toolkit.getDefaultToolkit().getScreenInsets(this.getGraphicsConfiguration());

        // TODO: add components
        this.initComponents();

        // TODO: setup window
        this.setupWindow();

        // TODO: start thread to share partner's screen
        this.screen_thread = new Thread(this);
        this.screen_thread.setDaemon(true);
        this.screen_thread.start();
    }

    private void initComponents() throws RemoteException {
        // TODO: constructor
        this.screen_label = new JLabel();
        this.menu_bar = new JMenuBar();
        this.menu_monitor = new JMenu();
        this.hardware_dialog = new HardwareDialog(this, this.remote_obj);

        // TODO: set minimum size of remote frame
        this.setMinimumSize(this.hardware_dialog.getPreferredSize());

        // TODO: style screen_label
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

        // TODO: style menu_bar
        this.menu_bar.setLayout(new GridBagLayout());
        this.menu_bar.setBackground(Color.decode("0x9A9A9A"));
        this.menu_bar.setPreferredSize(new Dimension(0, 25));
        this.setJMenuBar(this.menu_bar);

        // TODO: style menu
        this.menu_monitor.setText("Show monitor");
        this.menu_bar.setFont(new Font("segoe ui", Font.PLAIN, 14));
        this.menu_monitor.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                menuMonitorMousePressed(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                menuMonitorMouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                menuMonitorMouseExited(e);
            }
        });
        this.menu_bar.add(menu_monitor);
    }

    private void setupWindow() throws Exception {
        ImageIO.setUseCache(false);
        byte[] dgram = this.remote_obj.takeScreenshotServer(quality);
        ByteArrayInputStream bis = new ByteArrayInputStream(dgram);
        BufferedImage screenshot = ImageIO.read(bis);

        this.screen_size.width -= (this.taskbar_insets.left + this.taskbar_insets.right);
        this.screen_size.height -= (this.taskbar_insets.top + this.taskbar_insets.bottom + this.frame_insets.top + this.menu_bar.getPreferredSize().height);

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
            float ratio = (float) screenshot.getWidth() / screenshot.getHeight();
            int tmp_width = this.screen_size.width;
            this.screen_size.width = (int) (ratio * this.screen_size.height);

            int h_gap = (tmp_width - this.screen_size.width) / 2;

            this.dx = (float) screenshot.getWidth() / this.screen_size.width;
            this.dy = (float) screenshot.getHeight() / this.screen_size.height;
            this.screen_label.setBounds(h_gap, 0, this.screen_size.width, this.screen_size.height);
        }
    }

    @Override
    public void run() {
        int width = this.screen_label.getWidth();
        int height = this.screen_label.getHeight();
        try {
            while(this.common_bus.getTcpClient().isConnectedServer()) {
                byte[] dgram = this.remote_obj.takeScreenshotServer(quality);
                ByteArrayInputStream bis = new ByteArrayInputStream(dgram);
                Image screenshot = ImageIO.read(bis).getScaledInstance(width, height, Image.SCALE_SMOOTH);
                this.screen_label.setIcon(new ImageIcon(screenshot));
            }
            this.dispose();
        }
        catch(Exception e) {
            this.dispose();
        }
    }

    @Override
    public void dispose() {
        try {
            super.setVisible(false);
            super.dispose();
            this.client_panel.setEnabled(true);
            this.common_bus.getRmiClient().setRemoteServer(false);
            this.common_bus.getTcpClient().setConnectedServer(false);
            this.common_bus.getTcpClient().getClient().close();
            if(!this.screen_thread.isInterrupted())
                this.screen_thread.isInterrupted();
        }
        catch(Exception exception) {
            JOptionPane.showMessageDialog(null, "Can't close connection");
        }
    }

    private void remoteFrameWindowClosing(WindowEvent e) {
        this.dispose();
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

    // TODO: get hardware info of server
    private void menuMonitorMousePressed(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1) {
            this.hardware_dialog.setVisible(true);
        }
    }

    private void menuMonitorMouseEntered(MouseEvent e) {
        this.menu_monitor.setFont(new Font("segoe ui", Font.BOLD, 16));
        this.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void menuMonitorMouseExited(MouseEvent e) {
        this.menu_monitor.setFont(new Font("segoe ui", Font.PLAIN, 14));
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
}