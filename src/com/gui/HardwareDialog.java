package com.gui;

import com.bus.IRemoteDesktop;
import java.awt.Dimension;
import java.rmi.RemoteException;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

public class HardwareDialog extends JDialog implements Runnable {
    public final static int WIDTH_DIALOG = 500;
    public final static int HEIGHT_DIALOG = 540;
    public final static int HEIGHT_PANEL = 160;

    private HardwareGraph cpu_graphics;
    private HardwareGraph ram_graphics;
    private DrivesInfoPanel drives_info_panel;
    private JScrollPane drives_scroll;

    private IRemoteDesktop remote_obj;

    public HardwareDialog(JFrame owner, IRemoteDesktop remote_obj) throws RemoteException {
        super(owner);
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.setTitle("Hardware information");
        this.setResizable(false);
        this.setIconImage(new ImageIcon(this.getClass().getClassLoader().getResource("dialog_icon.png")).getImage());
        this.getContentPane().setPreferredSize(new Dimension(HardwareDialog.WIDTH_DIALOG, HardwareDialog.HEIGHT_DIALOG));
        this.setLayout(null);
        this.pack();

        this.remote_obj = remote_obj;

        // TODO: add components
        this.initComponents();
    }

    private void initComponents() {
        // TODO: constructor
        this.cpu_graphics = new HardwareGraph("CPU");
        this.ram_graphics = new HardwareGraph("RAM");
        this.drives_info_panel = new DrivesInfoPanel();
        this.drives_scroll = new JScrollPane();

        // TODO: style cpu_graphics
        this.cpu_graphics.setLocation(0, 20);
        this.add(this.cpu_graphics);

        // TODO: style ram_graphics
        this.ram_graphics.setLocation(0, this.cpu_graphics.getLocation().y + HardwareDialog.HEIGHT_PANEL + 20);
        this.add(this.ram_graphics);

        // TODO: style drives_info
        this.drives_scroll.setViewportView(this.drives_info_panel);

        // TODO: style drivers_scroll
        this.drives_scroll.setLocation(0, this.ram_graphics.getLocation().y + HardwareDialog.HEIGHT_PANEL + 20);
        this.drives_scroll.setSize(this.drives_info_panel.getSize());
        this.add(this.drives_scroll);
    }

    @Override
    public void run() {
        while(true) {
            try {
                this.cpu_graphics.addValue(this.remote_obj.getCpuLoadServer());
                this.ram_graphics.addValue(this.remote_obj.getRamUsageServer());
                this.drives_info_panel.updateInfo(this.remote_obj.getComputerInformation());
                Thread.sleep(500);
            }
            catch(Exception e){
                this.dispose();
            }
        }
    }
}