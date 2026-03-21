package com.bus.tcp;

import com.bus.chat.ChatBus;
import com.gui.chat.MainChatPanel;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Manages the server side of a TCP connection for accepting remote desktop clients.
 *
 * <p>Binds a {@link ServerSocket} to a given host/port and waits for incoming client
 * connections. Each accepted client must authenticate with the configured password before
 * a {@link ChatBus} session is opened and registered with the shared {@link MainChatPanel}.</p>
 *
 * <p>Only one client connection is tracked at a time ({@link #hasConnectedClient()}).
 * Threading (the accept loop) is the responsibility of the calling UI panel.</p>
 */
public class TcpServer {

    private final MainChatPanel mainChatPanel;

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private String password;

    private boolean listening;
    private boolean hasConnectedClient;

    /**
     * Creates a new {@code TcpServer} that registers accepted clients with the given panel.
     *
     * @param mainChatPanel the panel that receives new chat sessions
     */
    public TcpServer(MainChatPanel mainChatPanel) {
        this.mainChatPanel = mainChatPanel;
        this.serverSocket = null;
        this.clientSocket = null;
        this.password = null;
        this.listening = false;
        this.hasConnectedClient = false;
    }

    /**
     * Binds the server to the given address and port and begins accepting connections.
     *
     * <p>Does nothing if the server is already listening.</p>
     *
     * @param host     the local address to bind to (e.g., {@code "0.0.0.0"})
     * @param port     the TCP port to listen on
     * @param password the password clients must supply to authenticate
     * @throws IOException if binding fails
     */
    public void startListening(String host, int port, String password) throws IOException {
        if (listening) return;

        InetSocketAddress endpoint = new InetSocketAddress(host, port);
        this.password = password;
        this.serverSocket = new ServerSocket();
        this.serverSocket.bind(endpoint);
        this.listening = true;
    }

    /**
     * Stops listening and closes all open sockets.
     *
     * <p>Does nothing if the server is not currently listening.</p>
     *
     * @throws IOException if closing a socket fails
     */
    public void stopListening() throws IOException {
        if (!listening) return;

        this.serverSocket.close();
        if (this.clientSocket != null) this.clientSocket.close();
        this.listening = false;
        this.hasConnectedClient = false;
    }

    /**
     * Blocks until a client connects, then authenticates it.
     *
     * <p>On a correct password, opens a {@link ChatBus} and registers it with the panel.
     * On an incorrect password, sends {@code "false"} and does not open a session.
     * This method is intended to be called in a loop from a dedicated background thread.</p>
     *
     * @throws IOException if the accept or I/O operation fails
     */
    public void acceptNextClient() throws IOException {
        this.clientSocket = this.serverSocket.accept();

        DataOutputStream dataOut = new DataOutputStream(this.clientSocket.getOutputStream());
        DataInputStream dataIn = new DataInputStream(this.clientSocket.getInputStream());
        String receivedPassword = dataIn.readUTF();

        if (this.password.equals(receivedPassword)) {
            dataOut.writeUTF("true");
            ChatBus chatBus = new ChatBus(this.clientSocket);
            this.mainChatPanel.addNewConnection(chatBus);
            this.hasConnectedClient = true;
        } else {
            dataOut.writeUTF("false");
        }
    }

    /**
     * Returns all IPv4 addresses assigned to the local machine's network interfaces.
     *
     * <p>IPv6 addresses are silently skipped. Useful for populating the host combo box
     * in the server panel.</p>
     *
     * @return a list of IPv4 address strings (never {@code null})
     * @throws SocketException if the network interface list cannot be read
     */
    public List<String> getLocalIpv4Addresses() throws SocketException {
        List<String> addresses = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            Enumeration<InetAddress> ifaceAddresses = iface.getInetAddresses();
            while (ifaceAddresses.hasMoreElements()) {
                InetAddress addr = ifaceAddresses.nextElement();
                if (addr instanceof Inet4Address) {
                    addresses.add(addr.getHostAddress());
                }
            }
        }
        return addresses;
    }

    /**
     * Returns {@code true} if the server socket is currently bound and accepting connections.
     *
     * @return {@code true} when listening
     */
    public boolean isListening() {
        return listening;
    }

    /**
     * Returns {@code true} if at least one client has successfully authenticated and connected.
     *
     * @return {@code true} when a client is connected
     */
    public boolean hasConnectedClient() {
        return hasConnectedClient;
    }

    /**
     * Overrides the connected-client flag. Use this when the client is known to have
     * disconnected externally (e.g., detected by the receive thread).
     *
     * @param hasConnectedClient the new state
     */
    public void setHasConnectedClient(boolean hasConnectedClient) {
        this.hasConnectedClient = hasConnectedClient;
    }

    /**
     * Returns the server's {@link ServerSocket}, or {@code null} if not listening.
     *
     * @return the server socket
     */
    public ServerSocket getServerSocket() {
        return serverSocket;
    }
}
