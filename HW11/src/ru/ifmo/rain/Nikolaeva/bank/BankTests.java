package ru.ifmo.rain.Nikolaeva.bank;

import org.junit.*;

import java.rmi.*;

import org.junit.runner.JUnitCore;

import java.rmi.registry.*;
import java.util.concurrent.*;

public class BankTests {
    private static Bank bank;
    private String name = "Nikita";
    private String surname = "Kravtsov";
    private String subId = "12345";
    private String passport = "666666";
    private static final int sizeOfPerson = 8;

    @BeforeClass
    public static void beforeClass() throws Exception {
        Server.main();
        Registry registry = LocateRegistry.getRegistry(null, 16665);
        bank = (Bank) registry.lookup("Bank");
    }

    //Not existing:
    @Test
    public void testNotExistingLocalPerson() throws RemoteException {
        Assert.assertNull(bank.getLocalPerson("qwertyui"));
    }

    @Test
    public void testNotExistingRemotePerson() throws RemoteException {
        Assert.assertNull(bank.getRemotePerson("qwertyui"));
    }

    private void createPersonWIthNewName() throws RemoteException {
        for (int i = 0; i < sizeOfPerson; i++) {
            char suffix = (char) (i + 'A');
            bank.createPerson(name + suffix, surname + suffix, passport + Integer.toString(i));
        }
    }

    private void checkData(Person person, char suffix, String charI) throws RemoteException {
        Assert.assertEquals(name + suffix, person.getName());
        Assert.assertEquals(surname + suffix, person.getSurname());
        Assert.assertEquals(passport + charI, person.getPassport());
    }

    @Test
    public void testPerson() throws RemoteException {
        createPersonWIthNewName();
        for (int i = 0; i < sizeOfPerson; i++) {
            char suffix = (char) (i + 'A');
            String charI = Integer.toString(i);
            //local
            checkData(bank.getLocalPerson(passport + charI), suffix, charI);
            //remote
            //checkData(bank.getRemotePerson(passport + charI), suffix, charI);
        }
    }


    @Test
    public void testAccountCreation() throws RemoteException {
        //local
        Person remotePerson = bank.createPerson(name, surname, passport);
        bank.createAccount(subId, remotePerson);
        Person localPerson = bank.getLocalPerson(passport);
        Account localAccount = localPerson.getAccountBySubId(subId);
        Assert.assertNotNull(localAccount);
        Assert.assertEquals(localAccount.getSubid(), subId);
        //remote
        Account remoteAccount = remotePerson.getAccountBySubId(subId);
        Assert.assertNotNull(remoteAccount);
        Assert.assertEquals(remoteAccount.getSubid(), subId);
    }

    @Test
    public void testAmountSet() throws RemoteException {
        //local
        Person localPerson = bank.getLocalPerson(passport);
        Account localAccount = localPerson.getAccountBySubId(subId);
        localAccount.setAmount(1);
        Assert.assertEquals(1, localAccount.getAmount());
        //remote
        Person remotePerson = bank.createPerson(name, surname, passport);
        bank.createAccount(subId, remotePerson);
        Account remoteAccount = remotePerson.getAccountBySubId(subId);
        remoteAccount.setAmount(1);
        Assert.assertEquals(1, remoteAccount.getAmount());
    }

    @Test
    public void testAmountUpdate() throws RemoteException {
        //local
        Person localPerson = bank.getLocalPerson(passport);
        Account localAccount = localPerson.getAccountBySubId(subId);
        int oldAmount = localAccount.getAmount();
        localAccount.setAmount(oldAmount + 1);
        Assert.assertEquals(oldAmount + 1, localAccount.getAmount());
        //remote
        Person remotePerson = bank.createPerson(name, surname, passport);
        bank.createAccount(subId, remotePerson);
        Account remoteAccount = remotePerson.getAccountBySubId(subId);
        oldAmount = remoteAccount.getAmount();
        remoteAccount.setAmount(oldAmount + 1);
        Assert.assertEquals(oldAmount + 1, remoteAccount.getAmount());
    }

    @Test
    public void testLocalAccounts() throws RemoteException {
        //changing from first person doesn't affect second person
        Person remotePerson = bank.createPerson(name, surname, passport);
        bank.createAccount(subId, remotePerson);
        Person firstPerson = bank.getLocalPerson(passport);
        Person secondPerson = bank.getLocalPerson(passport);
        Account firstAccount = firstPerson.getAccountBySubId(subId);
        Account secondAccount = secondPerson.getAccountBySubId(subId);
        firstAccount.setAmount(firstAccount.getAmount() + 1);
        Assert.assertNotEquals(firstAccount.getAmount(), secondAccount.getAmount());
    }

    @Test
    public void testRemoteAccounts() throws RemoteException {
        //in remote account by link; changing from first person affects second person
        Person firstPerson = bank.createPerson(name, surname, passport);
        bank.createAccount(subId, firstPerson);
        Person secondPerson = bank.getRemotePerson(passport);
        Account firstAccount = firstPerson.getAccountBySubId(subId);
        Account secondAccount = secondPerson.getAccountBySubId(subId);
        firstAccount.setAmount(firstAccount.getAmount() + 1);
        Assert.assertEquals(firstAccount.getAmount(), secondAccount.getAmount());
    }

    @Test
    public void testClient() throws RemoteException {
        int amount = 0;
        for (int i = 0; i < sizeOfPerson; i++) {
            Person person = bank.getLocalPerson(passport + Integer.toString(i));
            if (person != null) {
                amount = person.getAccountBySubId(subId + Integer.toString(i)).getAmount();
            }
            char suffix = (char) (i + 'A');
            Client.main(name + suffix, surname + suffix, passport + Integer.toString(i), subId + Integer.toString(i), "1");
            Person localPerson = bank.getLocalPerson(passport + Integer.toString(i));
            Assert.assertNotNull(localPerson);
            Assert.assertEquals(name + suffix, localPerson.getName());
            Assert.assertEquals(surname + suffix, localPerson.getSurname());
            Account localAccount = localPerson.getAccountBySubId(subId + Integer.toString(i));
            Assert.assertNotNull(localAccount);
            Assert.assertEquals(amount + 1, localAccount.getAmount());
        }
    }

    @Test
    public void testMultiThreading() throws RemoteException {
        Person remotePerson = bank.createPerson(name, surname, passport);
        ExecutorService pool = Executors.newFixedThreadPool(sizeOfPerson);
        CountDownLatch checker = new CountDownLatch(sizeOfPerson);
        for (int i = 0; i < sizeOfPerson; i++) {
            pool.submit(() -> {
                try {
                    bank.getAccount(passport + ":" + subId);
                    bank.createAccount(subId, remotePerson);
                    checker.countDown();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            });
        }
        pool.shutdown();
        try {
            pool.awaitTermination(3, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            checker.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Assert.fail();
        }
        if (checker.getCount() > 0) {
            Assert.fail();
        }

    }

    public static void main(String[] args) throws Exception {
        System.exit(new JUnitCore().run(BankTests.class).wasSuccessful() ? 0 : 1);
    }

}
