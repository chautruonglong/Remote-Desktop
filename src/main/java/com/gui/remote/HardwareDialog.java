package com.gui.remote;

import com.bus.rmi.IRemoteDesktop;
import com.gui.Icons;
import java.awt.Dimension;
import java.rmi.RemoteException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

/**
 * Modal-less dialog that shows live CPU usage, RAM usage, and disk drive information
 * fetched from the remote server via RMI.
 *
 * <p>A {@link ScheduledExecutorService} polls the remote hardware metrics every
 * {@value #POLL_INTERVAL_MS} ms and pushes the values into the two {@link HardwareGraph}
 * panels and the {@link DrivesInfoPanel}. The scheduler is shut down when the dialog
 * is disposed.</p>
 *
 * <p>RAM and processor counts are fetched once at construction time (they rarely change)
 * and used to label the graphs. Drive and CPU/RAM usage data is fetched on every poll.</p>
 */
public class HardwareDialog extends JDialog {

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

    private final IRemoteDesktop remoteDesktop;
    private final ScheduledExecutorService scheduler;

    private final long totalRamGb;
    private final long totalSwapGb;
    private final int  processorCount;

    /**
     * Creates and starts a hardware monitoring dialog.
     *
     * <p>Fetches the static hardware profile (processor count and RAM capacity) immediately
     * via RMI, then starts the polling scheduler.</p>
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

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        this.scheduler.scheduleAtFixedRate(this::pollHardware, 0, POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
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
        // Position CPU graph at the top.
        this.cpuGraph.setLocation(0, 20);
        this.add(this.cpuGraph);

        // Position RAM graph below CPU graph.
        int ramY = this.cpuGraph.getLocation().y + HEIGHT_PANEL + 20;
        this.ramGraph.setLocation(0, ramY);
        this.add(this.ramGraph);

        // Position drives scroll pane below RAM graph.
        int drivesY = ramY + HEIGHT_PANEL + 20;
        JScrollPane drivesScrollPane = new JScrollPane();
        drivesScrollPane.setViewportView(this.drivesInfoPanel);
        drivesScrollPane.setLocation(0, drivesY);
        drivesScrollPane.setSize(this.drivesInfoPanel.getSize());
        this.add(drivesScrollPane);
    }

    /**
     * Fetches CPU load, RAM usage, and drive info from the server and updates the graphs.
     * Called by the scheduler every {@link #POLL_INTERVAL_MS} milliseconds.
     * Shuts down the scheduler and hides the dialog if a remote error occurs.
     */
    private void pollHardware() {
        try {
            this.cpuGraph.addValue(this.remoteDesktop.getCpuLoad());
            this.ramGraph.addValue(this.remoteDesktop.getRamUsage());
            this.drivesInfoPanel.updateInfo(this.remoteDesktop.getComputerInfo());
        } catch (Exception e) {
            this.scheduler.shutdown();
            this.setVisible(false);
        }
    }

    /**
     * Stops the polling scheduler and hides and disposes the dialog.
     */
    @Override
    public void dispose() {
        this.scheduler.shutdownNow();
        super.setVisible(false);
        super.dispose();
    }
}
