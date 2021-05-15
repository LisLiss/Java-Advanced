package ru.ifmo.rain.Nikolaeva.bank;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class Server {
    private final static int PORT = 8888;

    public static void main(final String... args) {
        final Bank bank = new RemoteBank(PORT);
        try {
            UnicastRemoteObject.exportObject(bank, PORT);
            LocateRegistry.createRegistry(16665).bind("Bank", bank);
        } catch (final RemoteException e) {
            System.err.println("Cannot export object: " + e.getMessage());
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
        System.out.println("Server started");
    }
}
