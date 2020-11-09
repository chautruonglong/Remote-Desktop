package com.bus;

import java.awt.AWTException;
import java.io.IOException;
import java.rmi.NotBoundException;

public class CommonBus {
    // TODO: for chat
    private ChatBus chat_bus;

    // TODO: for server
    private TcpServer tcp_server;
    private RmiServer rmi_server;

    // TODO: for client
    private TcpClient tcp_client;
    private RmiClient rmi_client;

    public CommonBus() {
        this.chat_bus = new ChatBus();
        this.tcp_server = new TcpServer(this.chat_bus);
        this.rmi_server = new RmiServer();
        this.tcp_client = new TcpClient(this.chat_bus);
        this.rmi_client = new RmiClient();
    }

    public TcpServer getTcpServer() {
        return this.tcp_server;
    }

    public RmiServer getRmiServer() {
        return this.rmi_server;
    }

    public TcpClient getTcpClient() {
        return this.tcp_client;
    }

    public RmiClient getRmiClient() {
        return this.rmi_client;
    }

    public ChatBus getChatBus() {
        return this.chat_bus;
    }

    // TODO: handle events of server
    public void startListeningOnServer(String host, int port, String password) throws IOException, AWTException {
        if(this.tcp_client.isConnectedServer()) throw new IOException("Please, stop remote server first!");
        if(!this.tcp_server.isListening() && !this.rmi_server.isBinding()) {
            // Port rmi = port tcp + 1
            this.tcp_server.startListeningOnTcpServer(host, port, password);
            this.rmi_server.startBindingOnRmiServer(host, port + 1);
        }
    }

    public void stopListeningOnServer() throws IOException, NotBoundException {
        if(this.tcp_server.isListening() && this.rmi_server.isBinding()) {
            this.tcp_server.stopListeningOnTcpServer();
            this.rmi_server.stopBindingOnRmiServer();
            this.chat_bus.setSocket(null);
        }
    }

    public void startConnectingToServer(String host, int port, String password) throws Exception {
        // TODO: check server is listening?
        if(this.tcp_server.isListening()) throw new Exception("Please, stop listening server first!");
        if(this.tcp_client.isConnectedServer()) throw new Exception("You are remoting!");
        this.tcp_client.startConnectingToTcpServer(host, port, password);
        this.rmi_client.startConnectingToRmiServer(host, port + 1);
    }
}