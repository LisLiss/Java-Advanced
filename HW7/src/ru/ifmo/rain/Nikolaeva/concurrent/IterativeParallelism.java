package ru.ifmo.rain.Nikolaeva.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterativeParallelism implements ListIP {

    private <T> List<List<? extends T>> partitionValues(final int threads, final List<? extends T> values) {
        int actualThreads = 1;
        if (threads > 0) {
            actualThreads = Math.min(threads, values.size());
        }
        List<List<? extends T>> actualPartitionValues = new ArrayList<>();
        int lengthValue = values.size() / actualThreads;
        int from = 0;
        int to = lengthValue;
        int mod = values.size() - lengthValue * actualThreads;
        if (mod > 0) {
            to++;
            mod--;
        }
        for (int i = 0; i < actualThreads; i++) {
            actualPartitionValues.add(values.subList(from, to));
            from = to;
            to = to + lengthValue;
            if (mod > 0) {
                to++;
                mod--;
            }
        }
        return actualPartitionValues;
    }

    private <T, R> R makeIP(int threads, final List<? extends T> values, Function<Stream<? extends T>, R> function,
                            Function<? super Stream<R>, R> mergeFunction) throws InterruptedException {
        List<List<? extends T>> partitionValuesList = partitionValues(threads, values);
        List<ThreadIP<R>> threadIPs = new ArrayList<>();
        List<Thread> threadList = new ArrayList<>();
        for (List<? extends T> list : partitionValuesList) {
            threadIPs.add(new ThreadIP<R>() {
                @Override
                public void run() {
                    setResult(function.apply(list.stream()));
                }
            });
            Thread thread = new Thread(threadIPs.get(threadIPs.size() - 1));
            threadList.add(thread);
            thread.start();
        }
        for (Thread thread : threadList) {
            thread.join();
        }
        return mergeFunction.apply(threadIPs.stream().map(ThreadIP::getResult));
    }


    private static abstract class ThreadIP<R> implements Runnable {
        private R result;

        void setResult(R result) {
            this.result = result;
        }

        R getResult() {
            return result;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return makeIP(threads, values, i -> i.map(Object::toString).collect(Collectors.joining()),
                i -> i.collect(Collectors.joining()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return makeIP(threads, values, i -> i.filter(predicate).collect(Collectors.toList()),
                i -> i.reduce(new ArrayList<>(), (j, k) -> {
                    j.addAll(k);
                    return j;
                }));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> function) throws InterruptedException {
        return makeIP(threads, values, i -> i.map(function).collect(Collectors.toList()),
                i -> i.reduce(new ArrayList<>(), (j, k) -> {
                    j.addAll(k);
                    return j;
                }));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        Function<Stream<? extends T>, T> max = i -> i.max(comparator).orElse(null);
        return makeIP(threads, values, max, max);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        Function<Stream<? extends T>, T> min = i -> i.min(comparator).orElse(null);
        return makeIP(threads, values, min, min);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return makeIP(threads, values, i -> i.allMatch(predicate), i -> i.allMatch(Predicate.isEqual(true)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return makeIP(threads, values, i -> i.anyMatch(predicate), i -> i.anyMatch(Predicate.isEqual(true)));
    }
}
