package com.bus.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Manages the RMI client lifecycle: looking up the remote object in the server's registry
 * and releasing it on disconnect.
 *
 * <p>The service URL is constructed using the same helper as {@link RmiServer} to ensure
 * the path always matches.</p>
 */
public class RmiClient {

    private IRemoteDesktop remoteDesktop;
    private boolean connected;

    /**
     * Creates a new {@code RmiClient} in the disconnected state.
     */
    public RmiClient() {
        this.remoteDesktop = null;
        this.connected = false;
    }

    /**
     * Looks up the {@link IRemoteDesktop} stub in the server's RMI registry.
     *
     * <p>Does nothing if already connected.</p>
     *
     * @param host the server hostname or IP address
     * @param port the RMI registry port (must be {@code tcpPort + 1})
     * @throws RemoteException       if the lookup fails due to a network error
     * @throws NotBoundException     if no object is bound under the expected name
     * @throws MalformedURLException if the URL is malformed
     */
    public void connect(String host, int port) throws RemoteException, NotBoundException, MalformedURLException {
        if (connected) return;

        String url = RmiServer.buildServiceUrl(host, port);
        this.remoteDesktop = (IRemoteDesktop) Naming.lookup(url);
        this.connected = true;
    }

    /**
     * Releases the remote object reference and marks this client as disconnected.
     *
     * <p>Does nothing if not currently connected.</p>
     */
    public void disconnect() {
        if (connected) {
            this.remoteDesktop = null;
            this.connected = false;
        }
    }

    /**
     * Returns the {@link IRemoteDesktop} stub used to invoke methods on the server,
     * or {@code null} if not connected.
     *
     * @return the remote desktop stub
     */
    public IRemoteDesktop getRemoteDesktop() {
        return remoteDesktop;
    }

    /**
     * Returns {@code true} if the client holds a valid reference to the remote object.
     *
     * @return {@code true} when connected
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Overrides the connection state flag. Use this when the remote connection is known
     * to have dropped externally.
     *
     * @param connected the new connection state
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
