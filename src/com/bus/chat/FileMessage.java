package com.bus.chat;

public class FileMessage extends Message {
    private String name;
    private long length;
    private byte[] data;

    public FileMessage(String sender, String name, long length, byte[] data) {
        this.setCurrentType(Message.FILE_MESSAGE);
        this.setSender(sender);
        this.name = name;
        this.length = length;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public long getLength() {
        return length;
    }

    public byte[] getData() {
        return data;
    }
}
