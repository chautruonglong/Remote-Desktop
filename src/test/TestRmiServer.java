package test;

import com.bus.IRemoteDesktop;
import com.bus.RmiServer;
import java.rmi.Naming;

public class TestRmiServer {
    public static void main(String[] args) throws Exception {
        RmiServer rmiServer = new RmiServer();
        System.out.println(rmiServer.isBinding());
        rmiServer.startBindingOnRmiServer("0.0.0.0", 9999);
        System.out.println(rmiServer.isBinding());

        IRemoteDesktop remote_obj = (IRemoteDesktop)Naming.lookup("rmi://0.0.0.0:9999/RemoteDesktop");
        System.out.println(remote_obj.takeScreenshotServer("jpg").length);

        rmiServer.stopBindingOnRmiServer();

    }
}