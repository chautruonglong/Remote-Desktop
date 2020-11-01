package com.bus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatBus {
    Socket socket;

    public ChatBus() {
        this.socket = null;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void sendMessage(String message) throws IOException {
        if(this.socket != null && !this.socket.isClosed()) {
            DataOutputStream dos = new DataOutputStream(this.socket.getOutputStream());
            message = this.socket.getLocalAddress().getHostName() + ":" + message;
            dos.writeUTF(message);
        }
    }

    public String recvMessage() throws IOException {
        String message = null;
        if(this.socket != null && !this.socket.isClosed()) {
            DataInputStream dis = new DataInputStream(this.socket.getInputStream());
            message = dis.readUTF();
        }
        return message;
    }
}
