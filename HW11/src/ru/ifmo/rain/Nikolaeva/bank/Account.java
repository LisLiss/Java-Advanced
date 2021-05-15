package ru.ifmo.rain.Nikolaeva.bank;

import java.rmi.*;

public interface Account extends Remote {
    /** Returns account identifier. */
    String getSubid() throws RemoteException;

    /** Returns amount of money at the account. */
    int getAmount() throws RemoteException;

    /** Sets amount of money at the account. */
    void setAmount(int amount) throws RemoteException;
}