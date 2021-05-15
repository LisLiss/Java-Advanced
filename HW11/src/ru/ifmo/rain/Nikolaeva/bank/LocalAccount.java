package ru.ifmo.rain.Nikolaeva.bank;

import java.io.Serializable;
import java.rmi.*;

public class LocalAccount extends AccountType implements Serializable {

    /**
     * Create new local account with Subid
     *
     * @param Subid account's subid
     */
    LocalAccount(String Subid) {
        super(Subid);
    }

    /**
     * Create new local account with this account
     *
     * @param account given account
     */
    LocalAccount(Account account) throws RemoteException {
        super(account.getSubid(), account.getAmount());
    }
}
