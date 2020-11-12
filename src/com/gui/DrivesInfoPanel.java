package com.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class DrivesInfoPanel extends JPanel {
    public final static String FOREGROUND = "0x26A0DA";

    private GroupLayout group_layout;
    private GroupLayout.ParallelGroup h_parallel;
    private GroupLayout.ParallelGroup v_parallel;

    public DrivesInfoPanel() {
        this.setSize(HardwareDialog.WIDTH_DIALOG, HardwareDialog.HEIGHT_PANEL);
        this.setBackground(Color.WHITE);

        // TODO: add components
        this.initComponents();
    }

    private void initComponents() {
        // TODO: constructor
        this.group_layout = new GroupLayout(this);
        this.v_parallel = this.group_layout.createParallelGroup();
        this.h_parallel = this.group_layout.createParallelGroup();
        this.group_layout.setHorizontalGroup(this.h_parallel);
        this.group_layout.setVerticalGroup(this.v_parallel);
        this.setLayout(this.group_layout);
    }
    int check = 0;
    public void updateInfo(ComputerInfo pc_info) {
        EventQueue.invokeLater(() -> {
            this.removeAll();

            // TODO: style os_name_label
            JLabel os_name_label = new JLabel();
            os_name_label.setText("Operating System: " + pc_info.getOsName());
            os_name_label.setFont(new Font("segoe ui", Font.BOLD | Font.ITALIC, 17));
            os_name_label.setForeground(Color.BLUE);
            os_name_label.setBounds(30, 0, HardwareDialog.WIDTH_DIALOG - 100, 30);
            this.add(os_name_label);

            // TODO: style drives
            int y = os_name_label.getY() + os_name_label.getHeight() + 10;
            for(DriveInfo drive : pc_info.getDrives()) {
                int total_space = drive.getTotalSpace();
                int usage_space = drive.getTotalSpace() - drive.getFreeSpace();

                // TODO: add name
                String text =
                    "<html>" +
                        "<font color=\"blue\">" + drive.getName() + "<br></font>" +
                        " (Used " + usage_space + " GB of " + total_space + " GB)" +
                        "</html>";
                JLabel drive_label = new JLabel(text);
                drive_label.setFont(new Font("segoe ui", Font.PLAIN, 12));
                drive_label.setBounds(80, y, 150, 30);
                this.add(drive_label);

                // TODO: add usage
                JProgressBar progress = new JProgressBar();
                progress.setMaximum(total_space);
                progress.setValue(usage_space);
                progress.setBounds(drive_label.getX() + drive_label.getWidth() + 50, y + 10, 170, 15);
                progress.setForeground(Color.decode(DrivesInfoPanel.FOREGROUND));
                if(total_space - usage_space < 20) progress.setForeground(Color.RED);
                this.add(progress);

                y += drive_label.getHeight() + 10;
            }

            this.validate();
            this.revalidate();
        });
    }

    @Override
    public Component add(Component comp) {
        super.add(comp);
        this.h_parallel.addGroup(this.group_layout.createSequentialGroup()
            .addContainerGap(comp.getX(), comp.getX())
            .addComponent(comp, comp.getWidth(), comp.getWidth(), comp.getWidth())
        );
        this.v_parallel.addGroup(this.group_layout.createSequentialGroup()
            .addContainerGap(comp.getY(), comp.getY())
            .addComponent(comp, comp.getHeight(), comp.getHeight(), comp.getHeight())
        );
        return comp;
    }
}