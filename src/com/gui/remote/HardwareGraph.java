package com.gui.remote;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 * A real-time line graph that visualises a percentage-valued metric over time (e.g., CPU or RAM
 * usage).
 *
 * <p>Values between {@code 0.0} and {@code 1.0} are pushed via {@link #addValue(double)}.
 * Each call appends a new data point and triggers a repaint. Older data points that scroll
 * off the left edge of the graph are automatically removed to bound memory usage.</p>
 *
 * <p>The graph displays a labelled border, percentage grid lines, and the current reading
 * as a percentage annotation on the right edge.</p>
 *
 * <p>{@link #paintComponent(Graphics)} is overridden (rather than {@code paint}) so Swing's
 * double-buffering and border painting are preserved.</p>
 */
public class HardwareGraph extends JPanel {

    /** Pixel spacing between grid lines and between consecutive data points (quarter-spacing). */
    public static final int GRID_SPACING = 20;

    private String title;
    private final List<Integer> dataPoints;
    private final Rectangle graphBounds;
    private final int horizontalLines;
    private final int verticalLines;

    /**
     * Creates a titled graph panel.
     *
     * @param title the metric label displayed at the top-left of the graph
     */
    public HardwareGraph(String title) {
        this();
        this.title = title;
    }

    /**
     * Creates an untitled graph panel with default dimensions from {@link HardwareDialog}.
     */
    public HardwareGraph() {
        this.setSize(HardwareDialog.WIDTH_DIALOG, HardwareDialog.HEIGHT_PANEL);
        this.setBackground(Color.WHITE);

        this.dataPoints = new ArrayList<>();
        this.graphBounds = new Rectangle(
            50,
            30,
            HardwareDialog.WIDTH_DIALOG - 100,
            HardwareDialog.HEIGHT_PANEL - 45
        );
        this.horizontalLines = this.graphBounds.height / GRID_SPACING;
        this.verticalLines   = this.graphBounds.width  / GRID_SPACING;
    }

    /**
     * Appends a new data point to the graph and triggers a repaint.
     *
     * <p>Values outside {@code [0, 1]} are clamped to the valid range before storing.</p>
     *
     * @param value the metric value as a ratio ({@code 0.0} = 0%, {@code 1.0} = 100%)
     */
    public void addValue(double value) {
        double clamped = Math.max(0.0, Math.min(1.0, value));
        int pixelHeight = (int) Math.ceil(clamped * this.graphBounds.height);
        this.dataPoints.add(pixelHeight);
        this.repaint();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Renders the graph background, title, percentage labels, grid, border rectangle,
     * and the line connecting all stored data points. Also shows the current reading as a
     * percentage annotation on the right edge.</p>
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        var g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill white background.
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Draw title.
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("segoe ui", Font.BOLD, 12));
        g2d.drawString(this.title != null ? this.title : "", 10, g2d.getFontMetrics().getAscent());

        // Draw percentage axis labels.
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("segoe ui", Font.PLAIN, 10));
        g2d.drawString("100%", this.graphBounds.x, this.graphBounds.y - 2);
        g2d.drawString("0%",   this.graphBounds.x, this.graphBounds.y + this.graphBounds.height + g2d.getFontMetrics().getAscent());

        // Draw grid lines.
        g2d.setColor(Color.decode("0xD9EAF4"));
        g2d.setStroke(new BasicStroke(1f));
        for (int i = 1; i < verticalLines; i++) {
            int x = this.graphBounds.x + GRID_SPACING * i;
            g2d.drawLine(x, this.graphBounds.y, x, this.graphBounds.y + this.graphBounds.height);
            if (i <= horizontalLines) {
                int y = this.graphBounds.y + GRID_SPACING * i;
                g2d.drawLine(this.graphBounds.x, y, this.graphBounds.x + this.graphBounds.width, y);
            }
        }

        // Draw graph border rectangle.
        g2d.setColor(Color.decode("0x117DBB"));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRect(this.graphBounds.x, this.graphBounds.y, this.graphBounds.width, this.graphBounds.height);

        // Draw the data line.
        if (this.dataPoints.size() > 1) {
            // Remove oldest points that would render outside the left edge.
            int pointSpacing = GRID_SPACING / 4;
            int startX = this.graphBounds.x + this.graphBounds.width - (this.dataPoints.size() - 1) * pointSpacing;
            while (startX < this.graphBounds.x) {
                this.dataPoints.remove(0);
                startX = this.graphBounds.x + this.graphBounds.width - (this.dataPoints.size() - 1) * pointSpacing;
            }

            g2d.setColor(Color.GREEN);
            g2d.setStroke(new BasicStroke(1.5f));
            int currentX = startX;
            for (int i = 0; i < this.dataPoints.size() - 1; i++) {
                int y1 = this.graphBounds.y + this.graphBounds.height - this.dataPoints.get(i);
                int y2 = this.graphBounds.y + this.graphBounds.height - this.dataPoints.get(i + 1);
                g2d.drawLine(currentX, y1, currentX + pointSpacing, y2);
                currentX += pointSpacing;
            }

            // Annotate the latest reading on the right edge.
            int latestPixel = this.dataPoints.get(this.dataPoints.size() - 1);
            int latestY     = this.graphBounds.y + this.graphBounds.height - latestPixel;
            int latestPct   = latestPixel * 100 / this.graphBounds.height;
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("segoe ui", Font.PLAIN, 11));
            g2d.drawString(latestPct + "%", this.graphBounds.x + this.graphBounds.width + 10, latestY);
        }
    }
}
