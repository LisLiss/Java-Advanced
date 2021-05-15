package ru.ifmo.rain.Nikolaeva.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final ExecutorService downloaderService;
    private final ExecutorService extractorsService;
    private final int perHost;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaderService = Executors.newFixedThreadPool(downloaders);
        this.extractorsService = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;
    }

    private void addToDownload(final String url, final int depth, final Set<String> was,
                               final Map<String, IOException> bad, final Map<String, Integer> hosts, AtomicInteger counter) {
        counter.incrementAndGet();
        downloaderService.execute(() -> {
            try {
                String nowHost = URLUtils.getHost(url);

                try {
                    if (!was.add(url)) {
                        return;
                    }
                    synchronized (hosts) {
                        if (hosts.containsKey(nowHost)) {
                            try {
                                while (hosts.get(nowHost) == perHost) {
                                    hosts.wait();
                                }
                            } catch (InterruptedException ignored) {
                            }
                            hosts.put(nowHost, hosts.get(nowHost) + 1);
                        } else {
                            hosts.put(nowHost, 1);
                        }
                    }
                    final Document document = downloader.download(url);
                    synchronized (hosts) {
                        hosts.put(nowHost, hosts.get(nowHost) - 1);
                        hosts.notifyAll();
                    }
                    if (depth > 1) {
                        counter.incrementAndGet();
                        extractorsService.execute(() -> {
                            try {
                                document.extractLinks().forEach(link -> addToDownload(link, depth - 1, was, bad, hosts, counter));
                            } catch (IOException e) {
                                bad.put(url, e);
                            } finally {
                                counter.decrementAndGet();
                            }
                        });
                    }
                } catch (IOException e) {
                    bad.put(url, e);
                    synchronized (hosts) {
                        hosts.put(nowHost, hosts.get(nowHost) - 1);
                        hosts.notifyAll();
                    }
                }
            } catch (MalformedURLException e) {
                was.add(url);
                bad.put(url, e);
            } finally {
                counter.decrementAndGet();
            }
        });
    }

    @Override
    public Result download(String url, int depth) {
        final Set<String> was = ConcurrentHashMap.newKeySet();
        final Map<String, IOException> bad = new ConcurrentHashMap<>();
        final Map<String, Integer> hosts = new ConcurrentHashMap<>();
        //final Phaser counter = new Phaser(1);
        final AtomicInteger counter = new AtomicInteger();
        addToDownload(url, depth, was, bad, hosts, counter);
        while (counter.get() != 0) {
        }
        //counter.arriveAndAwaitAdvance();
        was.removeAll(bad.keySet());
        return new Result(new ArrayList<>(was), bad);
    }

    @Override
    public void close() {
        downloaderService.shutdown();
        extractorsService.shutdown();
    }

    private static int getInt(String[] args, int i) {
        if (i >= args.length) {
            return 1;
        } else {
            return Integer.parseInt(args[i]);
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length < 1 || args.length > 5) {
            System.err.println("Incorrect number of args");
            return;
        }
        for (String arg : args) {
            if (arg == null) {
                System.err.println("argument is null");
                return;
            }
        }
        final String url = args[0];
        final int depth = getInt(args, 1);
        final int downloaders = getInt(args, 2);
        final int extractors = getInt(args, 3);
        final int perHost = getInt(args, 4);
        try (WebCrawler webCrawler = new WebCrawler(new CachingDownloader(), downloaders, extractors, perHost)) {
            Result result = webCrawler.download(url, depth);
            System.out.print("Finished ");
            int finishUrl = result.getDownloaded().size();
            System.out.print(finishUrl);
            if (finishUrl != 1) {
                System.out.println(" pages :");
            } else {
                System.out.println(" page :");
            }
            for (String page : result.getDownloaded()) {
                System.out.println(page);
            }
            System.out.println();
            System.out.print("Errors with ");
            int errorUrl = result.getErrors().size();
            System.out.print(errorUrl);
            if (errorUrl != 1) {
                System.out.println(" pages :");
            } else {
                System.out.println(" page :");
            }
            for (Map.Entry<String, IOException> page : result.getErrors().entrySet()) {
                System.out.println(page.getKey() + " with " + page.getValue());
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

}

