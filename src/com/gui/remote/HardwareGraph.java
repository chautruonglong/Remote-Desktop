package com.gui.remote;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.JPanel;

public class HardwareGraph extends JPanel {
    public final static int SPACE = 20;

    private String title;
    private ArrayList<Integer> values;
    private Rectangle bounds;
    private int h_lines;
    private int v_lines;

    public HardwareGraph(String title) {
        this();
        this.title = title;
    }

    public HardwareGraph() {
        this.setSize(HardwareDialog.WIDTH_DIALOG, HardwareDialog.HEIGHT_PANEL);
        this.setBackground(Color.GREEN);

        this.values = new ArrayList<>();
        this.bounds = new Rectangle(50, 30, HardwareDialog.WIDTH_DIALOG - 100, HardwareDialog.HEIGHT_PANEL - 45);
        this.h_lines = this.bounds.height / HardwareGraph.SPACE;
        this.v_lines = this.bounds.width / HardwareGraph.SPACE;
    }

    public void addValue(double value) {
        if(value < 0) value = 0;
        if(value > 1) value = 1;
        int fixed_value = (int) Math.ceil(value * this.bounds.height);
        this.values.add(fixed_value);
        this.repaint();
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // TODO: clear panel
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, this.getSize().width, this.getSize().height);

        // TODO: draw title
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("segoe ui", Font.BOLD, 12));
        g2d.drawString(this.title, 10, g2d.getFontMetrics().getAscent());

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("segoe ui", Font.PLAIN, 10));
        g2d.drawString("100%", this.bounds.x, this.bounds.y - 2);
        g2d.drawString("0%", this.bounds.x, this.bounds.y + this.bounds.height + g2d.getFontMetrics().getAscent());

        // TODO: draw grid
        g2d.setColor(Color.decode("0xD9EAF4"));
        g2d.setStroke(new BasicStroke(1f));
        for(int i = 1; i < v_lines; ++i) {
            g2d.drawLine(this.bounds.x + SPACE * i, this.bounds.y, this.bounds.x + SPACE * i, this.bounds.y + this.bounds.height);
            if(i <= this.h_lines) {
                g2d.drawLine(this.bounds.x, this.bounds.y + SPACE * i, this.bounds.x + this.bounds.width, this.bounds.y + SPACE * i);
            }
        }

        // TODO: draw rectangle
        g2d.setColor(Color.decode("0x117DBB"));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRect(this.bounds.x, this.bounds.y, this.bounds.width, this.bounds.height);

        // TODO: draw all values
        g2d.setColor(Color.GREEN);
        g2d.setStroke(new BasicStroke(1.5f));
        if(this.values.size() > 1) {
            int start = this.bounds.x + this.bounds.width - (this.values.size() - 1) * (HardwareGraph.SPACE / 4);
            while(start < this.bounds.x) {
                this.values.remove(0);
                start = this.bounds.x + this.bounds.width - (this.values.size() - 1) * (HardwareGraph.SPACE / 4);
            }
            for(int i = 0; i < this.values.size() - 1; ++i) {
                int value_1 = this.bounds.y + this.bounds.height - this.values.get(i);
                int value_2 = this.bounds.y + this.bounds.height - this.values.get(i + 1);
                g2d.drawLine(start, value_1, start + HardwareGraph.SPACE / 4, value_2);
                start += HardwareGraph.SPACE / 4;
            }
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("segoeui", Font.PLAIN, 11));
            int last = this.values.get(this.values.size() - 1);
            int y_last = this.bounds.y + this.bounds.height - last;
            g2d.drawString(last * 100 / this.bounds.height + "%", this.bounds.x + this.bounds.width + 10, y_last);
        }
    }
}
