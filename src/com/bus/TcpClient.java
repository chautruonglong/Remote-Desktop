package com.bus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TcpClient extends Thread {
    private Socket client;
    private ChatBus chat_bus;

    private boolean is_connected_server;

    public TcpClient(ChatBus chat_bus) {
        this.client = null;
        this.is_connected_server = false;
        this.chat_bus = chat_bus;
    }

    public void startConnectingToTcpServer(String host, int port, String password) throws IOException {
        if(this.is_connected_server == false) {
            this.client = new Socket(host, port);
            DataOutputStream dos = new DataOutputStream(this.client.getOutputStream());
            DataInputStream dis = new DataInputStream(this.client.getInputStream());

            dos.writeUTF(password);
            String result = dis.readUTF();

            if(result.equals("true")) {
                this.chat_bus.setSocket(this.client);
                this.is_connected_server = true;
            }
            else if(result.equals("false")) {
                this.client.close();
                throw new IOException("Wrong password of server");
            }
            else if(result.equals("busy")) {
                this.client.close();
                throw new IOException("Server is busy now");
            }
        }
    }

    public void stopConnectingToTcpServer() throws IOException {
        if(this.is_connected_server = true) {
            this.client.close();
            this.chat_bus.setSocket(null);
            this.is_connected_server = false;
        }
    }

    public boolean isConnectedServer() {
        return this.is_connected_server;
    }

    public void setConnectedServer(boolean b) {
        this.is_connected_server = b;
    }

    public Socket getClient() {
        return this.client;
    }
}
