package test;

import com.gui.HardwareDialog;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.rmi.RemoteException;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

public class TestRemoteFrame extends JFrame {
    private JMenuBar menu;

    public TestRemoteFrame() {
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setLayout(null);
        this.setVisible(true);

        this.menu = new JMenuBar();
        this.menu.add(new JMenu("Show"));
        this.menu.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    m(e);
                }
                catch(RemoteException remoteException) {
                    remoteException.printStackTrace();
                }
            }
        });
        this.setJMenuBar(menu);

        JButton button = new JButton("Click");
        button.setBounds(0, 0, 100, 30);
        this.add(button);

        File[] files = File.listRoots();
        int y = 100;
        for(File file : files) {
            JProgressBar progress = new JProgressBar();
            progress.setBounds(100, y, 70, 10);

            progress.setMaximum((int)(file.getTotalSpace() / 1024 / 1024 / 1024));
            progress.setValue((int) (file.getFreeSpace() / 1024 / 1024 / 1024 ));
            progress.setForeground(Color.decode("0x26A0DA"));
            this.add(progress);
            System.out.println(FileSystemView.getFileSystemView().getSystemDisplayName(new File("D:\\University")));
            y += 200;
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            new TestRemoteFrame();
        });
    }

    public void m(MouseEvent e) throws RemoteException {
        new HardwareDialog(this, null).setVisible(true);
    }
}
