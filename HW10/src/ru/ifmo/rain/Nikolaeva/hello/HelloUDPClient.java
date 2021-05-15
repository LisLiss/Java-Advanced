package ru.ifmo.rain.Nikolaeva.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloUDPClient implements HelloClient {

    private boolean rightResponse(final String request, final String response) {
        return response.contains(request) && response.length() != request.length();
    }

    @Override
    public void run(String host, int port, final String prefix, final int threads, final int requests) {
        ExecutorService threadsPool = Executors.newFixedThreadPool(threads);
        final InetSocketAddress socketAddress = new InetSocketAddress(host, port);
        for (int i = 0; i < threads; i++) {
            final int finalI = i;
            threadsPool.submit(() -> {
                try (DatagramSocket datagramSocket = new DatagramSocket()) {
                    for (int j = 0; j < requests; j++) {
                        byte[] requestBytes = (prefix + finalI + "_" + j).getBytes(StandardCharsets.UTF_8);
                        DatagramPacket request = new DatagramPacket(requestBytes, requestBytes.length, socketAddress);
                        datagramSocket.setSoTimeout(100);

                        String responseString;

                        while (!datagramSocket.isClosed()) {
                            try {
                                byte[] responseBytes = new byte[datagramSocket.getReceiveBufferSize()];
                                DatagramPacket response = new DatagramPacket(responseBytes, responseBytes.length);
                                datagramSocket.send(request);
                                System.out.println("Request sent:" + prefix + finalI + "_" + j);
                                datagramSocket.receive(response);
                                responseString = new String(response.getData(), response.getOffset(), response.getLength(), StandardCharsets.UTF_8);
                                if (rightResponse(prefix + finalI + "_" + j, responseString)) {
                                    System.out.println("Response received: " + responseString);
                                    break;
                                }
                            } catch (SocketTimeoutException e) {
                                System.err.println("Timeout for request " + prefix + finalI + "_" + j + "is out");
                            } catch (PortUnreachableException e) {
                                System.err.println("Socket is connected to a currently unreachable destination");
                            } catch (IOException e) {
                                System.err.println("InputOutput exception during connection");
                            }
                        }
                    }
                } catch (SocketException e) {
                    System.err.println("Socket could not be opened or bind to the local port" + e.getMessage());
                }
            });
        }
        threadsPool.shutdown();
        try {
            threadsPool.awaitTermination(20000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
        }
    }

    public static void main(String[] args) {
        if (args.length != 5) {
            System.err.println("Number of args != five");
            return;
        }
        for (int i = 0; i < 5; i++) {
            if (args[i] == null) {
                System.err.println("Arg[" + i + "] is null");
                return;
            }
        }
        try {
            int port = Integer.parseInt(args[1]);
            int threads = Integer.parseInt(args[3]);
            int requests = Integer.parseInt(args[4]);
            new HelloUDPClient().run(args[0], port, args[2], threads, requests);
        } catch (NumberFormatException e){
            System.err.println("Args 2, 4, 5 must be integer");
        }
    }
}
