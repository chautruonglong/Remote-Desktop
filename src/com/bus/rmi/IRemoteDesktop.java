package com.bus.rmi;

import com.gui.remote.ComputerInfo;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemoteDesktop extends Remote {
    // TODO: for capture screen to share
    byte[] takeScreenshotServer(String quality) throws Exception;

    // TODO: for remote mouse
    void mouseMovedServer(int x, int y) throws RemoteException;
    void mousePressedServer(int buttons) throws RemoteException;
    void mouseReleasedServer(int buttons) throws RemoteException;
    void mouseWheelServer(int wheel_amt) throws RemoteException;

    // TODO: for remote keyboard
    void keyPressedServer(int keycode) throws RemoteException;
    void keyReleasedServer(int keycode) throws RemoteException;

    // TODO: for get hardware info of server
    double getCpuLoadServer() throws RemoteException;
    double getRamUsageServer() throws RemoteException;
    long[] getRamMemories() throws RemoteException;
    int getCpus() throws RemoteException;
    ComputerInfo getComputerInformation() throws RemoteException;
}
