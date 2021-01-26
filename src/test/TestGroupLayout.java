package test;

import java.awt.EventQueue;
import javax.swing.*;

public class TestGroupLayout extends JFrame {
    public TestGroupLayout() {
        this.setSize(500, 500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        JScrollPane scrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        this.add(scrollPane);

        JButton button = new JButton("RECV");
        JButton button1 = new JButton("SEND");
        JButton button2 = new JButton("RECV");
        JButton button3 = new JButton("RECV");

        GroupLayout.ParallelGroup h_parallelGroup = layout.createParallelGroup();
        GroupLayout.SequentialGroup v_sequentialGroup = layout.createSequentialGroup();
        layout.setHorizontalGroup(h_parallelGroup);
        layout.setVerticalGroup(v_sequentialGroup);


        h_parallelGroup.addComponent(button);
        v_sequentialGroup.addComponent(button);

        h_parallelGroup.addGroup(layout.createSequentialGroup().addContainerGap(300, 300).addComponent(button1));
        v_sequentialGroup.addComponent(button1);

        h_parallelGroup.addGroup(layout.createSequentialGroup().addContainerGap(300, 300).addComponent(button2));
        v_sequentialGroup.addComponent(button2);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            new TestGroupLayout();
        });
    }
}