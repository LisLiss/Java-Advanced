package ru.ifmo.rain.Nikolaeva.bank;

import java.io.Serializable;
import java.rmi.*;
import java.util.List;

public class LocalPerson extends PersonType implements Serializable {

    /**
     * Create new LocalPerson
     *
     * @param name     person's name
     * @param surname  person's surname
     * @param passport person's passport
     * @param accounts person's accounts
     */
    public LocalPerson(String name, String surname, String passport, List<Account> accounts) throws RemoteException {
        super(name, surname, passport, accounts);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account putAccount(Account newAccount) throws RemoteException {
        if (newAccount == null) {
            return null;
        }
        if (!(newAccount instanceof LocalAccount)) {
            return null;
        } else {
            accounts.add(newAccount);
            return newAccount;
        }

    }
}
