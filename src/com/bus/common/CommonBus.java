package com.bus.common;

import com.bus.rmi.RmiClient;
import com.bus.rmi.RmiServer;
import com.bus.tcp.TcpClient;
import com.bus.tcp.TcpServer;
import com.gui.chat.MainChatPanel;
import java.awt.AWTException;
import java.io.IOException;
import java.rmi.NotBoundException;

public class CommonBus {
    // TODO: for server
    private TcpServer tcp_server;
    private RmiServer rmi_server;

    // TODO: for client
    private TcpClient tcp_client;
    private RmiClient rmi_client;

    public CommonBus() {
        this.rmi_server = new RmiServer();
        this.rmi_client = new RmiClient();
    }

    public void setMainChatPanel(MainChatPanel main_chat_panel) {
        this.tcp_server = new TcpServer(main_chat_panel);
        this.tcp_client = new TcpClient(main_chat_panel);
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

    // TODO: handle events of server
    public void startListeningOnServer(String host, int port, String password) throws IOException, AWTException {
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
        }
    }

    public void startConnectingToServer(String host, int port, String password) throws Exception {
        // TODO: check server is listening?
        if(this.tcp_server.isListening()) {
            String ip_server = this.tcp_server.getServer().getInetAddress().getHostAddress();
            if(host.equals(ip_server)) throw new Exception("Can't remote yourself!");
            System.out.println(ip_server);
            System.out.println(host);
        }
        if(this.tcp_client.isConnectedServer()) throw new Exception("You are remoting!");
        this.tcp_client.startConnectingToTcpServer(host, port, password);
        this.rmi_client.startConnectingToRmiServer(host, port + 1);
    }
}