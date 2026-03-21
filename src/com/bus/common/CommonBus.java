package com.bus.common;

import com.bus.rmi.RmiClient;
import com.bus.rmi.RmiServer;
import com.bus.tcp.TcpClient;
import com.bus.tcp.TcpServer;
import com.gui.chat.MainChatPanel;
import java.awt.AWTException;
import java.io.IOException;
import java.rmi.NotBoundException;

/**
 * Facade that coordinates the four underlying transport objects ({@link TcpServer},
 * {@link TcpClient}, {@link RmiServer}, {@link RmiClient}) required by the remote desktop
 * application.
 *
 * <p>Callers should prefer the high-level state-query methods
 * ({@link #isServerListening()}, {@link #isClientConnected()}, etc.) over calling into
 * the individual transport getters. This keeps the GUI panels decoupled from the internal
 * transport structure.</p>
 *
 * <p>The TCP and RMI ports are always paired: the RMI registry runs on
 * {@code tcpPort + 1}.</p>
 */
public class CommonBus {

    private TcpServer tcpServer;
    private RmiServer rmiServer;

    private TcpClient tcpClient;
    private RmiClient rmiClient;

    /**
     * Creates a new {@code CommonBus}.
     *
     * <p>{@link #setMainChatPanel(MainChatPanel)} must be called before any start/stop
     * operations to supply the TCP transports with the panel they register connections into.</p>
     */
    public CommonBus() {
        this.rmiServer = new RmiServer();
        this.rmiClient = new RmiClient();
    }

    /**
     * Initialises the TCP transports with the given chat panel.
     *
     * <p>Must be called once after construction and before any start/stop operations.</p>
     *
     * @param mainChatPanel the panel that receives new chat sessions
     */
    public void setMainChatPanel(MainChatPanel mainChatPanel) {
        this.tcpServer = new TcpServer(mainChatPanel);
        this.tcpClient = new TcpClient(mainChatPanel);
    }

    // -------------------------------------------------------------------------
    // High-level state queries (prefer these over calling individual getters)
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if the local TCP server socket is bound and accepting connections.
     *
     * @return {@code true} when the server is listening
     */
    public boolean isServerListening() {
        return tcpServer.isListening();
    }

    /**
     * Returns {@code true} if the local client is connected to a remote server.
     *
     * @return {@code true} when the client is connected
     */
    public boolean isClientConnected() {
        return tcpClient.isConnected();
    }

    /**
     * Returns {@code true} if at least one client has authenticated and connected to
     * this machine's server.
     *
     * @return {@code true} when a remote client is present
     */
    public boolean hasConnectedClient() {
        return tcpServer.hasConnectedClient();
    }

    // -------------------------------------------------------------------------
    // Server lifecycle
    // -------------------------------------------------------------------------

    /**
     * Starts the TCP server and RMI service so that remote clients can connect.
     *
     * <p>The RMI registry is bound on {@code port + 1}. Does nothing if already listening.</p>
     *
     * @param host     the local address to bind to
     * @param port     the TCP port; RMI uses {@code port + 1}
     * @param password the session password clients must supply
     * @throws IOException  if the TCP server fails to bind
     * @throws AWTException if the {@link java.awt.Robot} for screen capture cannot be created
     */
    public void startServer(String host, int port, String password) throws IOException, AWTException {
        if (!tcpServer.isListening() && !rmiServer.isBinding()) {
            tcpServer.startListening(host, port, password);
            rmiServer.bind(host, port + 1);
        }
    }

    /**
     * Stops the TCP server and unbinds the RMI service. Does nothing if not listening.
     *
     * @throws IOException       if closing the server socket fails
     * @throws NotBoundException if the RMI service was already unbound
     */
    public void stopServer() throws IOException, NotBoundException {
        if (tcpServer.isListening() && rmiServer.isBinding()) {
            tcpServer.stopListening();
            rmiServer.unbind();
        }
    }

    // -------------------------------------------------------------------------
    // Client lifecycle
    // -------------------------------------------------------------------------

    /**
     * Connects the client to the server at {@code host:port} and sets up the RMI stub.
     *
     * <p>Validates that the user is not trying to remote their own machine. Throws if
     * a connection is already active.</p>
     *
     * @param host     the server hostname or IP address
     * @param port     the server TCP port; RMI uses {@code port + 1}
     * @param password the session password
     * @throws Exception if connection or authentication fails, or self-remote is attempted
     */
    public void connectToServer(String host, int port, String password) throws Exception {
        if (tcpServer.isListening()) {
            String serverIp = tcpServer.getServerSocket().getInetAddress().getHostAddress();
            if (host.equals(serverIp)) {
                throw new Exception("Cannot remote your own machine!");
            }
        }
        if (tcpClient.isConnected()) {
            throw new Exception("Already connected to a server!");
        }
        tcpClient.connect(host, port, password);
        rmiClient.connect(host, port + 1);
    }

    // -------------------------------------------------------------------------
    // Individual transport accessors (use sparingly; prefer high-level methods above)
    // -------------------------------------------------------------------------

    /**
     * Returns the {@link TcpServer} transport. Prefer {@link #isServerListening()} for state checks.
     *
     * @return the TCP server
     */
    public TcpServer getTcpServer() {
        return tcpServer;
    }

    /**
     * Returns the {@link RmiServer} transport.
     *
     * @return the RMI server
     */
    public RmiServer getRmiServer() {
        return rmiServer;
    }

    /**
     * Returns the {@link TcpClient} transport. Prefer {@link #isClientConnected()} for state checks.
     *
     * @return the TCP client
     */
    public TcpClient getTcpClient() {
        return tcpClient;
    }

    /**
     * Returns the {@link RmiClient} transport.
     *
     * @return the RMI client
     */
    public RmiClient getRmiClient() {
        return rmiClient;
    }
}
