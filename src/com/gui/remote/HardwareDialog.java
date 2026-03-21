package com.gui.remote;

import com.bus.rmi.IRemoteDesktop;
import com.gui.Icons;
import java.awt.Dimension;
import java.rmi.RemoteException;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

/**
 * Modal-less dialog that shows live CPU usage, RAM usage, and disk drive information
 * fetched from the remote server via RMI.
 *
 * <p>A background daemon thread polls the remote hardware metrics every 500 ms and
 * pushes the values into the two {@link HardwareGraph} panels and the
 * {@link DrivesInfoPanel}. The thread is interrupted when the dialog is disposed.</p>
 *
 * <p>RAM and processor counts are fetched once at construction time (they rarely change)
 * and used to label the graphs. Drive and CPU/RAM usage data is fetched on every poll.</p>
 */
public class HardwareDialog extends JDialog implements Runnable {

    /** Fixed dialog width in pixels. */
    public static final int WIDTH_DIALOG  = 500;

    /** Fixed dialog height in pixels. */
    public static final int HEIGHT_DIALOG = 540;

    /** Height of each graph panel in pixels. */
    public static final int HEIGHT_PANEL  = 160;

    /** Polling interval for hardware metrics, in milliseconds. */
    private static final int POLL_INTERVAL_MS = 500;

    private HardwareGraph cpuGraph;
    private HardwareGraph ramGraph;
    private DrivesInfoPanel drivesInfoPanel;
    private JScrollPane drivesScrollPane;

    private final IRemoteDesktop remoteDesktop;
    private Thread updateThread;

    private final long totalRamGb;
    private final long totalSwapGb;
    private final int  processorCount;

    /**
     * Creates and starts a hardware monitoring dialog.
     *
     * <p>Fetches the static hardware profile (processor count and RAM capacity) immediately
     * via RMI, then starts the polling thread.</p>
     *
     * @param owner         the parent frame that owns this dialog
     * @param remoteDesktop the RMI stub used to query the server's hardware
     * @throws RemoteException if the initial hardware profile fetch fails
     */
    public HardwareDialog(JFrame owner, IRemoteDesktop remoteDesktop) throws RemoteException {
        super(owner);

        this.remoteDesktop  = remoteDesktop;
        this.processorCount = this.remoteDesktop.getProcessorCount();
        long[] capacities   = this.remoteDesktop.getRamCapacities();
        this.totalRamGb     = capacities[0];
        this.totalSwapGb    = capacities[1];

        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.setTitle("Hardware Information");
        this.setResizable(false);
        this.setIconImage(Icons.DIALOG.getImage());
        this.getContentPane().setPreferredSize(new Dimension(WIDTH_DIALOG, HEIGHT_DIALOG));
        this.setLayout(null);
        this.pack();

        this.initComponents();

        this.updateThread = new Thread(this);
        this.updateThread.setDaemon(true);
        this.updateThread.start();
    }

    private void initComponents() {
        int cpuCores   = this.processorCount / 2;
        this.cpuGraph = new HardwareGraph(
            "CPU (" + cpuCores + " cores / " + this.processorCount + " threads)"
        );
        this.ramGraph = new HardwareGraph(
            "RAM (" + this.totalRamGb + " GB)  SWAP (" + this.totalSwapGb + " GB)"
        );
        this.drivesInfoPanel = new DrivesInfoPanel();
        this.drivesScrollPane = new JScrollPane();

        // Position CPU graph at the top.
        this.cpuGraph.setLocation(0, 20);
        this.add(this.cpuGraph);

        // Position RAM graph below CPU graph.
        int ramY = this.cpuGraph.getLocation().y + HEIGHT_PANEL + 20;
        this.ramGraph.setLocation(0, ramY);
        this.add(this.ramGraph);

        // Position drives scroll pane below RAM graph.
        int drivesY = ramY + HEIGHT_PANEL + 20;
        this.drivesScrollPane.setViewportView(this.drivesInfoPanel);
        this.drivesScrollPane.setLocation(0, drivesY);
        this.drivesScrollPane.setSize(this.drivesInfoPanel.getSize());
        this.add(this.drivesScrollPane);
    }

    /**
     * Background polling loop — fetches CPU load, RAM usage, and drive info every
     * {@link #POLL_INTERVAL_MS} milliseconds. Stops if interrupted or a remote error occurs.
     */
    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                this.cpuGraph.addValue(this.remoteDesktop.getCpuLoad());
                this.ramGraph.addValue(this.remoteDesktop.getRamUsage());
                this.drivesInfoPanel.updateInfo(this.remoteDesktop.getComputerInfo());
                Thread.sleep(POLL_INTERVAL_MS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            this.setVisible(false);
        }
    }

    /**
     * Stops the polling thread and hides and disposes the dialog.
     */
    @Override
    public void dispose() {
        if (this.updateThread != null && !this.updateThread.isInterrupted()) {
            this.updateThread.interrupt();
        }
        super.setVisible(false);
        super.dispose();
    }
}
