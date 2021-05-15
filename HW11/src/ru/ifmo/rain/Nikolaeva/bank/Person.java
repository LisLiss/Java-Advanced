package ru.ifmo.rain.Nikolaeva.bank;

import java.rmi.*;
import java.util.*;

public interface Person extends Remote {

    /** Returns person's name. */
    String getName() throws RemoteException;

    /** Returns person's surname. */
    String getSurname() throws RemoteException;

    /** Returns person's passport. */
    String getPassport() throws RemoteException;

    /** Put new account to person. */
    Account putAccount (Account account) throws RemoteException;

    /** Returns all person's accounts. */
    List<Account> getAccounts() throws RemoteException;

    /** Returns account by SubId. */
    Account getAccountBySubId (String subId) throws RemoteException;


}
