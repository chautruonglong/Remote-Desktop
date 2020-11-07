package com.gui;

import com.bus.CommonBus;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class MainFrame extends JFrame {
    public final static int WIDTH_FRAME = 400;
    public final static int HEIGHT_FRAME = 400;
    public final static int HEIGHT_TASKBAR = 50;
    public final static String TASKBAR_BACKGROUND = "0x000942";

    // TODO: class for handle events
    private CommonBus common_bus;

    private JPanel taskbar_panel;
    private CommonLabel client_label;
    private CommonLabel server_label;
    private CommonLabel chat_label;
    private ClientPanel client_panel;
    private ServerPanel server_panel;
    private ChatPanel chat_panel;
    private int focus_key;

    public MainFrame() throws IOException {
        ImageIO.setUseCache(false); // TODO: not using disk cache
        // TODO: style main frame
        this.getContentPane().setPreferredSize(new Dimension(MainFrame.WIDTH_FRAME, MainFrame.HEIGHT_FRAME));
        this.pack();
        this.setLayout(null);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setTitle("Remote Desktop Software");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setIconImage(new ImageIcon(".\\images\\window_icon.png").getImage());
        this.setVisible(true);

        // TODO: add components
        this.initComponents();
    }

    private void initComponents() throws IOException {
        // TODO: constructor
        this.common_bus = new CommonBus();
        this.taskbar_panel = new JPanel();
        this.client_label = new CommonLabel();
        this.server_label = new CommonLabel();
        this.chat_label = new CommonLabel();
        this.client_panel = new ClientPanel(this.common_bus);
        this.server_panel = new ServerPanel(this.common_bus);
        this.chat_panel = new ChatPanel(this.common_bus);

        // TODO: set focus_key = 1 for client_panel
        this.focus_key = 1;

        // TODO: layout of taskbar_panel
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {100, 100, 100};

        // TODO: style taskbar_panel
        this.taskbar_panel.setLayout(gridBagLayout);
        this.taskbar_panel.setBackground(Color.decode(MainFrame.TASKBAR_BACKGROUND));
        this.taskbar_panel.setBounds(0, 0, MainFrame.WIDTH_FRAME, MainFrame.HEIGHT_TASKBAR);
        this.add(this.taskbar_panel);

        // TODO: style client_label
        this.client_label.setText("Client");
        this.client_label.setBigFont();
        this.client_label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tabLabelMouseClicked(e, client_label, 1);
            }
        });
        this.taskbar_panel.add(this.client_label);

        // TODO: style server_label
        this.server_label.setText("Server");
        this.server_label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tabLabelMouseClicked(e, server_label, 2);
            }
        });
        this.taskbar_panel.add(this.server_label);

        // TODO: style chat_label
        this.chat_label.setText("Chat");
        this.chat_label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tabLabelMouseClicked(e, chat_label, 3);
            }
        });
        this.taskbar_panel.add(this.chat_label);

        // TODO: default
        this.client_panel.setVisible(true);
        this.server_panel.setVisible(false);
        this.chat_panel.setVisible(false);
        this.add(this.client_panel);
        this.add(this.server_panel);
        this.add(this.chat_panel);
    }

    private void tabLabelMouseClicked(MouseEvent e, CommonLabel commonLabel, int key) {
        if(e.getButton() == MouseEvent.BUTTON1) {
            if(key == focus_key) return;
            JPanel show_panel = (key == 1) ? this.client_panel : (key == 2) ? this.server_panel : this.chat_panel;
            JPanel hide_panel = (focus_key == 1) ? this.client_panel : (focus_key == 2) ? this.server_panel : this.chat_panel ;
            if(key > focus_key) {
                this.showPanelsSlider(show_panel, hide_panel, true);
            }
            else {
                this.showPanelsSlider(show_panel, hide_panel, false);
            }

            // TODO: update status
            this.focus_key = key;
            this.client_label.setSmallFont();
            this.server_label.setSmallFont();
            this.chat_label.setSmallFont();
            commonLabel.setBigFont();
        }
    }

    private void showPanelsSlider(JPanel show_panel, JPanel hide_panel, boolean isLeft) {
        show_panel.setVisible(true);

        // TODO: atomic integer for lambda expression
        AtomicInteger x_hide_location = new AtomicInteger(0);
        AtomicInteger x_show_location = new AtomicInteger(0);
        AtomicInteger value = new AtomicInteger(0);

        if(isLeft) {
            x_show_location.set(MainFrame.WIDTH_FRAME);
            value.set(-50);
        }
        else {
            x_show_location.set(-MainFrame.WIDTH_FRAME);
            value.set(50);
        }

        Timer timer = new Timer(30, (e) -> {
            hide_panel.setLocation(x_hide_location.get(), MainFrame.HEIGHT_TASKBAR);
            show_panel.setLocation(x_show_location.get(), MainFrame.HEIGHT_TASKBAR);
            if(x_show_location.get() == 0) {
                ((Timer)e.getSource()).stop();
                hide_panel.setVisible(false);
            }
            x_show_location.addAndGet(value.get());
            x_hide_location.addAndGet(value.get());
        });
        timer.start();
    }
}