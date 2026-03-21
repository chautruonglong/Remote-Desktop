package com.bus.chat;

/**
 * A {@link Message} that carries a binary file as its payload.
 *
 * <p>The entire file is read into a byte array before sending, so this message type is
 * limited to files that fit comfortably in memory. The caller should enforce a reasonable
 * size limit before constructing this object.</p>
 */
public class FileMessage extends Message {

    private static final long serialVersionUID = 1L;

    private final String fileName;
    private final long fileLength;
    private final byte[] fileData;

    /**
     * Creates a new file-transfer message.
     *
     * @param sender     the hostname or display name of the sender
     * @param fileName   the original name of the file (without path)
     * @param fileLength the size of the file in bytes
     * @param fileData   the raw binary content of the file
     */
    public FileMessage(String sender, String fileName, long fileLength, byte[] fileData) {
        super(Message.FILE_MESSAGE);
        this.setSender(sender);
        this.fileName = fileName;
        this.fileLength = fileLength;
        this.fileData = fileData;
    }

    /**
     * Returns the original file name (without directory path).
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns the size of the file in bytes.
     *
     * @return the file length
     */
    public long getFileLength() {
        return fileLength;
    }

    /**
     * Returns the raw binary content of the file.
     *
     * @return the file data byte array
     */
    public byte[] getFileData() {
        return fileData;
    }
}
