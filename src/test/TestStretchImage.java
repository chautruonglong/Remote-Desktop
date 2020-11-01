package test;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class TestStretchImage extends JFrame {
    JLabel label = new JLabel();

    public TestStretchImage() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setLocationRelativeTo(null);
        this.setLayout(null);
        this.setVisible(true);

        this.label = new JLabel();
        // THIS LINE
        this.label.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("D:\\Pictures\\background.jpg").getScaledInstance(this.getWidth() - 200, this.getHeight() - 200, Image.SCALE_DEFAULT)));
        this.label.setBounds(0, 0, this.getWidth(), this.getHeight());
        this.add(this.label);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            new TestStretchImage();
        });
    }
}
