package com.bus;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemoteDesktop extends Remote {
    // TODO: for capture screen to share
    byte[] takeScreenshotServer() throws Exception;

    // TODO: for remote mouse
    void mouseMovedServer(int x, int y) throws RemoteException; // return type of cursor
    void mousePressedServer(int buttons) throws RemoteException;
    void mouseReleasedServer(int buttons) throws RemoteException;
    void mouseWheelServer(int wheel_amt) throws RemoteException;

    //TODO: for remote keyboard
    void keyPressedServer(int keycode) throws RemoteException;
    void keyReleasedServer(int keycode) throws RemoteException;
}
