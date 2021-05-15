package ru.ifmo.rain.Nikolaeva.bank;

import java.rmi.*;
import java.util.*;
import java.util.concurrent.*;

public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Person> persons = new ConcurrentHashMap<>();

    /**
     * Create new remote bank with port
     *
     * @param port number of port
     */
    RemoteBank(final int port) {
        this.port = port;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account createAccount(String subid, Person person) throws RemoteException {
        if (subid == null) {
            return null;
        }
        final Account account = new RemoteAccount(subid, port);
        if (accounts.putIfAbsent(person.getPassport() + ":" + subid, account) != null) {
            return getAccount(person.getPassport() + ":" + subid);
        } else {
            person = persons.get(person.getPassport());
            person.putAccount(account);
            return account;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Person createPerson(String name, String surname, String passport) throws RemoteException {
        if (name == null || surname == null || passport == null) {
            return null;
        }
        final Person person = new RemotePerson(name, surname, passport, port);
        if (persons.putIfAbsent(passport, person) != null) {
            return getRemotePerson(passport);
        } else {
            return person;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account getAccount(String id) {
        if (id == null) {
            return null;
        }
        return accounts.get(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Person getLocalPerson(String passport) throws RemoteException {
        if (passport == null || persons.get(passport) == null) {
            return null;
        }
        Person person = persons.get(passport);
        List<Account> localAccounts = new ArrayList<>();
        for (Account account : person.getAccounts()) {
            localAccounts.add(new LocalAccount(account));
        }
        return new LocalPerson(person.getName(), person.getSurname(), person.getPassport(), localAccounts);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Person getRemotePerson(String passport) throws RemoteException {
        if (passport == null) {
            return null;
        }
        return persons.get(passport);
    }


}
