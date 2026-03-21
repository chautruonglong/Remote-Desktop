package com.bus.rmi;

import com.model.ComputerInfo;
import com.model.ImageQuality;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * RMI remote interface exposing all operations the client can invoke on the server.
 *
 * <p>All methods may throw {@link RemoteException} due to the nature of remote method
 * invocation over the network. Screen capture additionally throws checked exceptions
 * from the imaging pipeline.</p>
 *
 * <p>Method naming convention: these are operations the <em>client</em> requests the
 * <em>server</em> to perform — the "Server" suffix has been dropped in favor of concise,
 * verb-first names (e.g., {@link #captureScreen}, {@link #moveMouse}).</p>
 */
public interface IRemoteDesktop extends Remote {

    // -------------------------------------------------------------------------
    // Screen capture
    // -------------------------------------------------------------------------

    /**
     * Captures the server's screen, optionally scales it down server-side, and returns it
     * as a compressed image byte array.
     *
     * @param quality the desired image format ({@link ImageQuality#LOW} for JPEG,
     *                {@link ImageQuality#HIGH} for higher-fidelity JPEG)
     * @param targetWidth the preferred maximum frame width on the client, or {@code 0} for native width
     * @param targetHeight the preferred maximum frame height on the client, or {@code 0} for native height
     * @return the captured screenshot encoded in the requested format
     * @throws Exception if capture or encoding fails
     */
    byte[] captureScreen(ImageQuality quality, int targetWidth, int targetHeight) throws Exception;

    /**
     * Returns the server's primary screen dimensions in pixels.
     *
     * @return a two-element array {@code [width, height]}
     * @throws RemoteException on network error
     */
    int[] getScreenDimensions() throws RemoteException;

    // -------------------------------------------------------------------------
    // Mouse control
    // -------------------------------------------------------------------------

    /**
     * Moves the server's mouse cursor to the given absolute screen coordinates.
     *
     * @param x the target x coordinate in server screen pixels
     * @param y the target y coordinate in server screen pixels
     * @throws RemoteException on network error
     */
    void moveMouse(int x, int y) throws RemoteException;

    /**
     * Simulates pressing one or more mouse buttons on the server.
     *
     * @param buttonMask an {@link java.awt.event.InputEvent} button mask constant
     * @throws RemoteException on network error
     */
    void pressMouse(int buttonMask) throws RemoteException;

    /**
     * Simulates releasing one or more mouse buttons on the server.
     *
     * @param buttonMask an {@link java.awt.event.InputEvent} button mask constant
     * @throws RemoteException on network error
     */
    void releaseMouse(int buttonMask) throws RemoteException;

    /**
     * Simulates scrolling the mouse wheel on the server.
     *
     * @param wheelAmount the number of "notches" to scroll (negative = up, positive = down)
     * @throws RemoteException on network error
     */
    void scrollMouseWheel(int wheelAmount) throws RemoteException;

    // -------------------------------------------------------------------------
    // Keyboard control
    // -------------------------------------------------------------------------

    /**
     * Simulates pressing a keyboard key on the server.
     *
     * @param keyCode a {@link java.awt.event.KeyEvent} VK_ constant
     * @throws RemoteException on network error
     */
    void pressKey(int keyCode) throws RemoteException;

    /**
     * Simulates releasing a keyboard key on the server.
     *
     * @param keyCode a {@link java.awt.event.KeyEvent} VK_ constant
     * @throws RemoteException on network error
     */
    void releaseKey(int keyCode) throws RemoteException;

    // -------------------------------------------------------------------------
    // Hardware monitoring
    // -------------------------------------------------------------------------

    /**
     * Returns the server's current CPU utilization as a value between {@code 0.0} and {@code 1.0}.
     *
     * @return CPU load ratio
     * @throws RemoteException on network error
     */
    double getCpuLoad() throws RemoteException;

    /**
     * Returns the server's current RAM utilization as a value between {@code 0.0} and {@code 1.0}.
     *
     * @return RAM usage ratio (used / total)
     * @throws RemoteException on network error
     */
    double getRamUsage() throws RemoteException;

    /**
     * Returns the server's total RAM and swap capacities in gigabytes.
     *
     * @return a two-element array {@code [totalRamGb, totalSwapGb]}
     * @throws RemoteException on network error
     */
    long[] getRamCapacities() throws RemoteException;

    /**
     * Returns the number of logical processors available on the server.
     *
     * @return the processor count (threads, including hyper-threading)
     * @throws RemoteException on network error
     */
    int getProcessorCount() throws RemoteException;

    /**
     * Returns detailed system information about the server (OS name and disk drives).
     *
     * @return a {@link ComputerInfo} snapshot of the server's system
     * @throws RemoteException on network error
     */
    ComputerInfo getComputerInfo() throws RemoteException;
}
