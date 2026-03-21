package com.bus.tcp;

import com.bus.chat.ChatBus;
import com.gui.chat.MainChatPanel;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Manages the client side of a TCP connection to a remote desktop server.
 *
 * <p>Handles the authentication handshake with the server: sends the password and reads
 * back the server's accept/reject response. On success, creates a {@link ChatBus} and
 * registers it with the shared {@link MainChatPanel}.</p>
 *
 * <p>This class is a plain helper and does <em>not</em> extend {@link Thread}. Threading
 * is the responsibility of the calling UI panel.</p>
 */
public class TcpClient {

    private final MainChatPanel mainChatPanel;

    private Socket socket;
    private boolean connected;

    /**
     * Creates a new {@code TcpClient} that will register connections with the given panel.
     *
     * @param mainChatPanel the panel that receives new chat sessions
     */
    public TcpClient(MainChatPanel mainChatPanel) {
        this.mainChatPanel = mainChatPanel;
        this.socket = null;
        this.connected = false;
    }

    /**
     * Connects to the server at {@code host:port}, authenticates with {@code password},
     * and on success opens a {@link ChatBus} session.
     *
     * <p>If the server rejects the password, the socket is closed and an
     * {@link IOException} is thrown. Does nothing if already connected.</p>
     *
     * @param host     the server hostname or IP address
     * @param port     the server TCP port
     * @param password the session password to send
     * @throws IOException if the connection or authentication fails
     */
    public void connect(String host, int port, String password) throws IOException {
        if (connected) return;

        this.socket = new Socket(host, port);

        DataOutputStream dataOut = new DataOutputStream(this.socket.getOutputStream());
        DataInputStream dataIn = new DataInputStream(this.socket.getInputStream());

        dataOut.writeUTF(password);
        String authResult = dataIn.readUTF();

        if ("true".equals(authResult)) {
            ChatBus chatBus = new ChatBus(this.socket);
            this.mainChatPanel.addNewConnection(chatBus);
            this.connected = true;
        } else {
            this.socket.close();
            throw new IOException("Authentication failed: wrong password");
        }
    }

    /**
     * Disconnects from the server by closing the socket.
     *
     * <p>Does nothing if not currently connected.</p>
     *
     * @throws IOException if closing the socket fails
     */
    public void disconnect() throws IOException {
        if (connected) {
            this.socket.close();
            this.connected = false;
        }
    }

    /**
     * Returns {@code true} if the client is currently connected to a server.
     *
     * @return {@code true} when connected
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Overrides the connection state flag. Use this when the connection is known to have
     * dropped externally (e.g., detected by the receiver thread).
     *
     * @param connected the new connection state
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     * Returns the underlying socket, or {@code null} if not connected.
     *
     * @return the client socket
     */
    public Socket getSocket() {
        return socket;
    }
}
