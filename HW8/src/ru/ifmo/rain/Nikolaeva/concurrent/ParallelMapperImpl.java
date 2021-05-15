package ru.ifmo.rain.Nikolaeva.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {
    private final List<Thread> threadsList;
    private final Queue<Runnable> threadQueue;


    private void addStartThread(final Thread thread) {
        threadsList.add(thread);
        thread.start();
    }

    /**
     * Makes given number of threads
     * @param threads number of threads
     */
    public ParallelMapperImpl(int threads) {
        if (threads <= 0) {
            throw new IllegalArgumentException("Number of threads is less than one");
        }
        this.threadsList = new ArrayList<>();
        this.threadQueue = new ArrayDeque<>();
        for (int i = 0; i < threads; i++) {
            addStartThread(new Thread(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        Runnable taskRun;
                        synchronized (threadQueue) {
                            while (threadQueue.isEmpty()) {
                                threadQueue.wait();
                            }
                            taskRun = threadQueue.poll();
                            threadQueue.notifyAll();
                        }
                        taskRun.run();
                    }
                } catch (InterruptedException ignored) {
                } finally {
                    Thread.currentThread().interrupt();
                }
            }));
        }
    }

    private class ResultCollection<R> {
        private final List<R> result;
        private int counter;

        ResultCollection(final int counterSize) {
            counter = 0;
            result = new ArrayList<>(Collections.nCopies(counterSize, null));
        }

        void set(final int index, R value) {
            result.set(index, value);
            synchronized (this) {
                counter++;
                if (counter == result.size()) {
                    notify();
                }
            }
        }

        synchronized List<R> get() throws InterruptedException {
            while (counter < result.size()) {
                wait();
            }
            return result;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        ResultCollection<R> resultCollection = new ResultCollection<>(args.size());
        for (int i = 0; i < args.size(); i++) {
            final int nowI = i;
            synchronized (threadQueue){
                threadQueue.add(() -> resultCollection.set(nowI, f.apply((args.get(nowI)))));
                threadQueue.notifyAll();
            }
        }
        return resultCollection.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        threadsList.forEach(Thread::interrupt);
        for (Thread thread : threadsList){
            try{
                thread.join();
            } catch (InterruptedException ignored){
            }
        }
    }
}
