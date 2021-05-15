package ru.ifmo.rain.Nikolaeva.bank;

import java.rmi.*;

public interface Bank extends Remote {

    /** Create person's account with Subid and return it. */
    Account createAccount(String subid, Person person) throws RemoteException;

    /** Create person with name, surname and passport and return them. */
    Person createPerson(String name, String surname, String passport) throws RemoteException;

    /** Find account by id. */
    Account getAccount(String id) throws RemoteException;

    /** Find local person by passport. */
    Person getLocalPerson(String passport) throws RemoteException;

    /** Find local person by passport. */
    Person getRemotePerson(String passport) throws RemoteException;

    }
