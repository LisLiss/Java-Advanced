package ru.ifmo.rain.Nikolaeva.bank;

import java.io.Serializable;

public abstract class AccountType implements Account, Serializable{
    private final String Subid;
    private int amount;

    /**
     * Create new account with Subid
     * @param Subid account's id
     */
    AccountType(String Subid) {
        this.Subid = Subid;
    }

    /**
     * Create new account with Subid and amount
     * @param Subid account's id
     * @param amount amount of money
     */
    AccountType(String Subid, int amount) {
        this.Subid = Subid;
        this.amount = amount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSubid() {
        return Subid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized int getAmount() {
        return amount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setAmount(final int amount) {
        this.amount = amount;
    }
}
