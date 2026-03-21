package com.bus.rmi;

import java.awt.AWTException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * Manages the RMI server lifecycle: creating the registry, binding the remote object,
 * and unbinding it on shutdown.
 *
 * <p>The RMI service URL is constructed as {@code rmi://host:port/remote} and is shared
 * between bind and unbind operations via the {@link #buildServiceUrl} helper to avoid
 * duplication.</p>
 */
public class RmiServer {

    /** The RMI service path appended to the URL (must match {@link RmiClient}'s lookup path). */
    private static final String SERVICE_PATH = "/remote";

    private String serviceUrl;
    private boolean binding;

    /**
     * Creates a new {@code RmiServer} in the unbound state.
     */
    public RmiServer() {
        this.serviceUrl = null;
        this.binding = false;
    }

    /**
     * Creates the RMI registry on the given port, binds a {@link RemoteDesktopImpl} to it,
     * and marks this server as active.
     *
     * <p>If a registry already exists on the port (e.g., from a previous session),
     * the existing registry is reused and the object is rebound. Only one
     * {@link RemoteDesktopImpl} instance is created regardless.</p>
     *
     * <p>Does nothing if already binding.</p>
     *
     * @param host the hostname to advertise to clients (set as {@code java.rmi.server.hostname})
     * @param port the RMI registry port
     * @throws RemoteException       if RMI setup fails
     * @throws MalformedURLException if the service URL is malformed
     * @throws AWTException          if a {@link java.awt.Robot} cannot be created
     */
    public void bind(String host, int port) throws RemoteException, MalformedURLException, AWTException {
        if (binding) return;

        this.serviceUrl = buildServiceUrl(host, port);
        System.setProperty("java.rmi.server.hostname", host);

        try {
            LocateRegistry.createRegistry(port);
        } catch (RemoteException e) {
            // Registry already exists on this port — safe to reuse.
            System.err.println("RMI registry already exists on port " + port + ", reusing existing registry.");
        }

        // Bind once with a single RemoteDesktopImpl instance.
        Naming.rebind(this.serviceUrl, new RemoteDesktopImpl());
        this.binding = true;
    }

    /**
     * Unbinds the remote object from the registry and marks this server as inactive.
     *
     * <p>Does nothing if not currently binding.</p>
     *
     * @throws RemoteException       if the unbind operation fails
     * @throws MalformedURLException if the stored service URL is malformed
     * @throws NotBoundException     if the name was already unbound
     */
    public void unbind() throws RemoteException, MalformedURLException, NotBoundException {
        if (!binding) return;

        Naming.unbind(this.serviceUrl);
        this.serviceUrl = null;
        this.binding = false;
    }

    /**
     * Returns {@code true} if this server has a remote object bound in the RMI registry.
     *
     * @return {@code true} when bound
     */
    public boolean isBinding() {
        return binding;
    }

    /**
     * Builds the canonical RMI service URL for the given host and port.
     *
     * @param host the server hostname or IP address
     * @param port the RMI registry port
     * @return the fully qualified RMI URL (e.g., {@code rmi://192.168.0.1:9999/remote})
     */
    static String buildServiceUrl(String host, int port) {
        return "rmi://" + host + ":" + port + SERVICE_PATH;
    }
}
