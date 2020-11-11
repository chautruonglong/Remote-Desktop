package test;

import com.gui.ComputerInfo;
import com.gui.DriveInfo;
import com.gui.DrivesInfoPanel;
import com.gui.HardwareDialog;
import java.awt.Color;
import java.awt.EventQueue;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileSystemView;

public class TestDrivesInfo extends JFrame {
    public TestDrivesInfo() {
        this.setSize(600, 500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.getContentPane().setBackground(Color.BLACK);
        this.setLayout(null);
        this.setVisible(true);

        ComputerInfo pc_info = new ComputerInfo("Window 10");
        for(File file : File.listRoots()) {
            pc_info.getDrives().add(new DriveInfo(FileSystemView.getFileSystemView().getSystemDisplayName(file), file.getFreeSpace(), file.getTotalSpace()));
        }
        DrivesInfoPanel drivesInfoPanel = new DrivesInfoPanel(pc_info);


        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(drivesInfoPanel);
        scrollPane.setBounds(0, 20, HardwareDialog.WIDTH_DIALOG, HardwareDialog.HEIGHT_PANEL);
        this.add(scrollPane);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            new TestDrivesInfo();
        });
    }
}
