package com.bus.chat;

import java.io.Serial;
import java.util.Arrays;
import java.util.Objects;

/**
 * A {@link Message} that carries a binary file as its payload.
 *
 * <p>The entire file is read into a byte array before sending, so this message type is
 * limited to files that fit comfortably in memory. The caller should enforce a reasonable
 * size limit before constructing this object.</p>
 */
public record FileMessage(String sender, String fileName, long fileLength, byte[] fileData) implements Message {
    @Serial
    private static final long serialVersionUID = 1L;

    public FileMessage {
        Objects.requireNonNull(sender, "sender");
        Objects.requireNonNull(fileName, "fileName");
        fileData = Arrays.copyOf(Objects.requireNonNull(fileData, "fileData"), fileData.length);
    }

    @Override
    public byte[] fileData() {
        return Arrays.copyOf(this.fileData, this.fileData.length);
    }
}
