package ru.ifmo.rain.Nikolaeva.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RemoteAccount extends AccountType {

    /**
     * Create new remote account
     * @param Subid account's subid
     * @param port number of port
     */
    public RemoteAccount(final String Subid, int port) throws RemoteException {
        super(Subid);
        UnicastRemoteObject.exportObject(this, port);
    }
}
