package com.bus.chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Wraps a connected {@link Socket} to provide typed, object-level message exchange.
 *
 * <p>The underlying {@link ObjectOutputStream} and {@link ObjectInputStream} are created
 * <em>once</em> per connection and reused for all subsequent send/receive calls. Recreating
 * these streams on each call would write duplicate serialization headers and corrupt the
 * stream state, so sharing them is required for correct operation.</p>
 *
 * <p><strong>Construction ordering:</strong> Both endpoints must create their
 * {@code ObjectOutputStream} (and flush it) before creating their {@code ObjectInputStream}.
 * The constructor enforces this order: it flushes the output stream header immediately so
 * the remote side's {@code ObjectInputStream} can unblock.</p>
 *
 * <p>This class is not thread-safe. Only one thread should call {@link #sendMessage} and
 * one thread should call {@link #receiveMessage} at a time.</p>
 */
public class ChatBus {

    private final Socket socket;
    private final ObjectOutputStream outputStream;
    private final ObjectInputStream inputStream;

    /**
     * Creates a {@code ChatBus} over the given already-connected socket.
     *
     * <p>The constructor establishes the serialization streams in the required order:
     * output stream first (flushing the protocol header), then input stream. Both
     * endpoints must follow the same order to avoid a deadlock.</p>
     *
     * @param socket a connected, open socket
     * @throws IOException if stream creation fails
     */
    public ChatBus(Socket socket) throws IOException {
        this.socket = socket;
        // ObjectOutputStream must be created and flushed before ObjectInputStream
        // on both sides to exchange stream headers without deadlocking.
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
        this.outputStream.flush();
        this.inputStream = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * Sends a {@link Message} to the remote peer.
     *
     * <p>{@link ObjectOutputStream#reset()} is called after each write to prevent the
     * object stream from caching previously serialized references, which would otherwise
     * cause stale data to be sent on repeated messages of the same object.</p>
     *
     * @param message the message to send; must not be {@code null}
     * @throws IOException if the write fails or the connection is closed
     */
    public void sendMessage(Message message) throws IOException {
        outputStream.writeObject(message);
        outputStream.flush();
        outputStream.reset();
    }

    /**
     * Blocks until a {@link Message} is received from the remote peer.
     *
     * @return the next message from the remote peer
     * @throws IOException            if the connection is closed or a read error occurs
     * @throws ClassNotFoundException if the received class is not on the classpath
     */
    public Message receiveMessage() throws IOException, ClassNotFoundException {
        return (Message) inputStream.readObject();
    }

    /**
     * Returns the underlying socket for this bus (e.g., to check the remote address).
     *
     * @return the underlying socket
     */
    public Socket getSocket() {
        return socket;
    }
}
