package ru.ifmo.rain.Nikolaeva.arrayset;

import java.util.*;

public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {

    private final List<T> data;
    private final Comparator<? super T> comparator;


    public ArraySet(Collection<? extends T> data, Comparator<? super T> comparator) {
        this.comparator = comparator;
        Set<T> tmp = new TreeSet<>(comparator);
        tmp.addAll(data);
        this.data = new ArrayList<>(tmp);
    }

    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    public ArraySet(Collection<? extends T> data) {
        this(data, null);
    }

    public ArraySet(Comparator<? super T> comparator) {
        this(Collections.emptyList(), comparator);
    }

    private ArraySet(List<T> data, Comparator<? super T> comparator) {
        this.data = data;
        this.comparator = comparator;
    }

    private T getByIndex(int index) {
        try {
            return data.get(index);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private int lowerIndex(T t) {
        int index = Collections.binarySearch(data, t, comparator);
        if (index < 0) {
            index = -(index + 1);
        }
        index--;
        return index;
    }

    @Override
    //the greatest element in this set strictly less than the given element
    public T lower(T t) {
        return getByIndex(lowerIndex(t));
    }

    private int floorIndex(T t) {
        int index = Collections.binarySearch(data, t, comparator);
        if (index < 0) {
            index = -(index + 1) - 1;
        }
        return index;
    }

    @Override
    //the greatest element in this set less than or equal to the given element
    public T floor(T t) {
        return getByIndex(floorIndex(t));
    }

    private int ceilingIndex(T t) {
        int index = Collections.binarySearch(data, t, comparator);
        if (index < 0) {
            index = -(index + 1);
        }
        return index;
    }

    @Override
    //the least element in this set greater than or equal to the given element
    public T ceiling(T t) {
        return getByIndex(ceilingIndex(t));
    }

    private int higherIndex(T t) {
        int index = Collections.binarySearch(data, t, comparator);
        if (index < 0) {
            index = -(index + 1);
        } else {
            index++;
        }
        return index;
    }

    @Override
    //the least element in this set strictly greater than the given element
    public T higher(T t) {
        return getByIndex(higherIndex(t));
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(data).iterator();
    }

    @Override
    public NavigableSet<T> descendingSet() {
        return new ArraySet<>(new ReversedList<>(data), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    private ArraySet<T> getSubSet(int fromIndex, int toIndex) {
        if (fromIndex < toIndex) {
            return new ArraySet<>(data.subList(fromIndex, toIndex), comparator);
        } else {
            return new ArraySet<>(comparator);
        }
    }

    @SuppressWarnings("unchecked")
    private int compare(T firstElement, T secondElement) {
        if (comparator == null) {
            return ((Comparable<T>) firstElement).compareTo(secondElement);
        } else {
            return comparator.compare(firstElement, secondElement);
        }
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        int fromIndex = fromInclusive ? ceilingIndex(fromElement) : higherIndex(fromElement);
        int toIndex = toInclusive ? floorIndex(toElement) : lowerIndex(toElement);
        return getSubSet(fromIndex, toIndex + 1);
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        if (data.isEmpty()) {
            return new ArraySet<>(comparator);
        } else {
            return subSet(first(), true, toElement, inclusive);
        }
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        if (data.isEmpty()) {
            return new ArraySet<>(comparator);
        } else {
            return subSet(fromElement, inclusive, last(), true);
        }
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        if (compare(fromElement, toElement) > 0) {
                throw new IllegalArgumentException();
        }
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public T first() {
        if (data.isEmpty()) {
            throw new NoSuchElementException();
        } else {
            return data.get(0);
        }
    }

    @Override
    public T last() {
        if (data.isEmpty()) {
            throw new NoSuchElementException();
        } else {
            return data.get(size() - 1);
        }
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return Collections.binarySearch(data, (T) o, comparator) >= 0;
    }
}
