package ru.ifmo.rain.Nikolaeva.arrayset;

import java.util.AbstractList;
import java.util.List;

public class ReversedList<T> extends AbstractList<T> {
    private List<T> data;

    public ReversedList(List<T> data) {
        this.data = data;
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public T get(int index) {
        return data.get(size() - index - 1);
    }

}
