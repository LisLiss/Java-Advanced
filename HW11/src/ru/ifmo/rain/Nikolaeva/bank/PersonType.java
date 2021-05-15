package ru.ifmo.rain.Nikolaeva.bank;

import java.io.*;
import java.rmi.*;
import java.util.*;

public abstract class PersonType implements Person, Serializable {
    private final String name, surname, passport;
    protected List<Account> accounts;

    /**
     * Create new person
     * @param name person's name
     * @param surname person's surname
     * @param passport person's passport
     * @param accounts person's accounts
     */
    PersonType(String name, String surname, String passport, List<Account> accounts) {
        this.name = name;
        this.surname = surname;
        this.passport = passport;
        this.accounts = accounts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSurname() {
        return surname;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPassport() {
        return passport;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Account> getAccounts() {
        return accounts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account getAccountBySubId(String Subid) throws RemoteException {
        for (Account account : accounts){
            if (account.getSubid().equals(Subid)){
                return account;
            }
        }
        return null;
    }
}
