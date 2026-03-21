package com.bus.chat;

import java.io.Serializable;

/**
 * Sealed message contract for all payloads exchanged between peers over a chat session.
 *
 * <p>Each permitted implementation represents a different kind of payload (e.g., text or file).
 * Messages are serialized over the network via Java object streams and therefore implement
 * {@link Serializable}.</p>
 *
 * <p>Use {@code instanceof} checks (rather than the type constants) to determine the
 * concrete subtype at the receiving end:</p>
 * <pre>
 *   if (message instanceof StringMessage stringMsg) { ... }
 *   else if (message instanceof FileMessage fileMsg) { ... }
 * </pre>
 */
public sealed interface Message extends Serializable permits StringMessage, FileMessage {
}
