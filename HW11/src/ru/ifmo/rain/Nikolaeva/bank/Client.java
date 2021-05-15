package ru.ifmo.rain.Nikolaeva.bank;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    private synchronized static String addArg(String[] args, int arg, int minLength, String noName) throws RemoteException {
        if (args.length >= minLength) {
            return args[arg];
        } else {
            return noName;
        }
    }

    public static void main(final String... args) throws RemoteException {
        if (args == null) {
            System.err.println("Args == null");
            return;
        }
        for (String arg : args) {
            if (arg == null) {
                System.err.println("Arg == null");
                return;
            }
        }
        String name = addArg(args, 0, 1, "Smth");
        String surname = addArg(args, 1, 2, "Smth");
        String passport = addArg(args, 2, 3, "000000");
        String subId = addArg(args, 3, 4, "111111");
        int amount = 0;
        if (args.length >= 5) {
            try {
                amount = Integer.parseInt(args[4]);
            } catch (Exception ignored) {
            }
        }
        final Bank bank;
        try {
            Registry registry = LocateRegistry.getRegistry(null, 16665);
            bank = (Bank) registry.lookup("Bank");
        } catch (final NotBoundException e) {
            System.err.println("Bank not bound");
            return;
        }
        Person person = bank.getRemotePerson(passport);
        if (person == null) {
            person = bank.createPerson(name, surname, passport);
        }
        if (!person.getName().equals(name) || !person.getSurname().equals(surname)) {
            System.err.println("Can't find person");
            return;
        }
        Account account = bank.getAccount(person.getPassport() + ":" + subId);
        if (account == null) {
            account = bank.createAccount(subId, person);
        }
        account.setAmount(account.getAmount() + amount);
    }

}
