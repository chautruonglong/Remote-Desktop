package com.gui.remote;

import com.gui.AppStyles;
import com.gui.Icons;
import com.model.ComputerInfo;
import com.model.DriveInfo;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import javax.swing.*;

/**
 * Scrollable panel that displays the remote computer's operating system name and disk drives.
 *
 * <p>Updated periodically via {@link #updateInfo(ComputerInfo)} by the
 * {@link HardwareDialog} background thread. Each update rebuilds all child components
 * on the EDT to reflect the latest drive usage data.</p>
 *
 * <p>Uses absolute positioning ({@code null} layout) with explicit {@link java.awt.Rectangle}
 * bounds on each child component. The panel's preferred size is recalculated on every update
 * so the parent {@link JScrollPane} can scroll correctly.</p>
 *
 * <p>Drive usage bars turn red when a drive is more than 90% full.</p>
 */
public class DrivesInfoPanel extends JPanel {


    /** Usage ratio above which the progress bar switches to red. */
    private static final double WARNING_THRESHOLD = 0.9;

    private static final int PADDING          = 30;
    private static final int OS_LABEL_HEIGHT  = 30;
    private static final int ROW_HEIGHT       = 60;
    private static final int PROGRESS_Y_OFFSET = 10;
    private static final int PROGRESS_WIDTH   = 170;
    private static final int PROGRESS_HEIGHT  = 15;
    private static final int DRIVE_LABEL_WIDTH  = 200;
    private static final int DRIVE_LABEL_HEIGHT = 50;

    /**
     * Creates an empty drives-info panel with a white background.
     */
    public DrivesInfoPanel() {
        this.setBackground(Color.WHITE);
        this.setLayout(null);
    }

    /**
     * Rebuilds the panel contents to reflect the given computer information snapshot.
     *
     * <p>This method posts all component work to the EDT via {@link EventQueue#invokeLater}
     * so it is safe to call from any thread.</p>
     *
     * @param pcInfo the latest snapshot of the remote machine's OS and drives
     */
    public void updateInfo(ComputerInfo pcInfo) {
        EventQueue.invokeLater(() -> {
            this.removeAll();

            int y = 5;

            // OS name label.
            JLabel osLabel = new JLabel("Operating System: " + pcInfo.getOsName());
            osLabel.setFont(new Font(AppStyles.FONT_UI, Font.BOLD | Font.ITALIC, 17));
            osLabel.setForeground(Color.BLUE);
            osLabel.setBounds(PADDING, y, HardwareDialog.WIDTH_DIALOG - 100, OS_LABEL_HEIGHT);
            this.add(osLabel);
            y += OS_LABEL_HEIGHT + 10;

            // One row per drive.
            for (DriveInfo drive : pcInfo.getDrives()) {
                long totalGb = drive.getTotalSpaceGb();
                long usedGb  = totalGb - drive.getFreeSpaceGb();

                // Drive name + usage text label with HDD icon.
                String driveText =
                    "<html>" +
                    "<font color=\"blue\">" + drive.getName() + "<br></font>" +
                    " (Used " + usedGb + " GB of " + totalGb + " GB)" +
                    "</html>";
                JLabel driveLabel = new JLabel(driveText);
                driveLabel.setIcon(Icons.HDD);
                driveLabel.setFont(new Font(AppStyles.FONT_UI, Font.PLAIN, 12));
                driveLabel.setIconTextGap(20);
                driveLabel.setBounds(PADDING + 30, y, DRIVE_LABEL_WIDTH, DRIVE_LABEL_HEIGHT);
                this.add(driveLabel);

                // Usage progress bar — red if > 90% full.
                JProgressBar progressBar = new JProgressBar();
                progressBar.setMaximum((int) totalGb);
                progressBar.setValue((int) usedGb);
                progressBar.setForeground(
                    (totalGb > 0 && (double) usedGb / totalGb >= WARNING_THRESHOLD)
                        ? Color.RED
                        : Color.decode(AppStyles.COLOR_DRIVE_PROGRESS)
                );
                progressBar.setBounds(
                    PADDING + 30 + DRIVE_LABEL_WIDTH + 20,
                    y + PROGRESS_Y_OFFSET,
                    PROGRESS_WIDTH,
                    PROGRESS_HEIGHT
                );
                this.add(progressBar);

                y += DRIVE_LABEL_HEIGHT + 10;
            }

            // Update the preferred size so the parent scroll pane knows the full content height.
            this.setPreferredSize(new Dimension(HardwareDialog.WIDTH_DIALOG, y + 10));

            this.validate();
            this.revalidate();
            this.repaint();
        });
    }
}
