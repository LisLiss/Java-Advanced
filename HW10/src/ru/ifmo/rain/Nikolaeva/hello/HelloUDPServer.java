package ru.ifmo.rain.Nikolaeva.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class HelloUDPServer implements HelloServer {
    private DatagramSocket datagramSocket;
    private ExecutorService handler, pool;


    private void process(final DatagramPacket request) {
        try {
            String responseAns = "Hello, " + new String(request.getData(), request.getOffset(), request.getLength(), StandardCharsets.UTF_8);
            byte[] responseBytes = responseAns.getBytes(StandardCharsets.UTF_8);
            datagramSocket.send(new DatagramPacket(responseBytes, responseBytes.length, request.getSocketAddress()));
        } catch (PortUnreachableException e) {
            System.out.println("The socket is connected to unreachable destination. " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error: InputOutput error: " + e.getMessage());
        }
    }

    @Override
    public void start(int port, int threads) {
        try {
            pool = Executors.newFixedThreadPool(threads);
            datagramSocket = new DatagramSocket(port);
            handler = Executors.newSingleThreadExecutor();
            handler.submit(() -> {
                try {
                    while (!Thread.interrupted() && !datagramSocket.isClosed()) {
                        DatagramPacket request = new DatagramPacket(new byte[datagramSocket.getReceiveBufferSize()], datagramSocket.getReceiveBufferSize());
                        datagramSocket.receive(request);
                        pool.submit(() -> process(request));
                    }
                } catch (RejectedExecutionException e) {
                    System.out.println("Server is too busy");
                } catch (PortUnreachableException e) {
                    System.out.println("The socket is connected to unreachable destination: " + e.getMessage());
                } catch (IOException e) {
                    if (!datagramSocket.isClosed())
                        System.err.println("InputOutput error occurred: " + e.getMessage());
                }
            });
        }catch (SocketException e) {
            System.err.println("The socket could not be opened or bind to local port: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        handler.shutdown();
        pool.shutdown();
        datagramSocket.close();
        try {
            pool.awaitTermination(20000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
        }
    }

    public static void main(String[] args) {
        if (args.length!=2){
            System.err.println("Number of args != 2");
            return;
        }
        for (int i=0; i<2; i++){
            if (args[i]==null){
                System.err.println("Arg[" + i +"] is null");
                return;
            }
        }
        try {
            int port = Integer.parseInt(args[0]);
            int threads = Integer.parseInt(args[1]);
            new HelloUDPServer().start(port, threads);
        } catch (NumberFormatException e){
            System.err.println("Args 2, 4, 5 must be integer");
        }
    }
}
