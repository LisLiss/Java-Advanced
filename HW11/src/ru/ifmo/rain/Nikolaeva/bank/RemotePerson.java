package ru.ifmo.rain.Nikolaeva.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RemotePerson extends PersonType implements Person {

    /**
     * Create new remote person
     *
     * @param name     person's name
     * @param surname  person's surname
     * @param passport person's passport
     * @param port     number of port
     * @throws RemoteException
     */
    RemotePerson(String name, String surname, String passport, int port) throws RemoteException {
        super(name, surname, passport, new ArrayList<>());
        UnicastRemoteObject.exportObject(this, port);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account putAccount(Account account) {
        if (account == null) {
            return null;
        }
        if (!(account instanceof RemoteAccount)) {
            return null;
        } else {
            accounts.add(account);
            return account;
        }
    }
}
