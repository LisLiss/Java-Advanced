package ru.ifmo.rain.Nikolaeva.walk;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

class HashSum {
    private static final int BUFFER_SIZE = 1024;
    private static final int FNV_PRIME = 0x01000193;
    private static final int HVAL = 0x811c9dc5;

    static int FNVHash(Path path) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        InputStream inputStream = Files.newInputStream(path);
        int i, hashSum = HVAL;
        while ((i = inputStream.read(buffer)) >= 0) {
            for (int j = 0; j < i; j++) {
                hashSum = (buffer[j] & 0xff) ^ (hashSum * FNV_PRIME);
            }
        }
        return hashSum;
    }
}
