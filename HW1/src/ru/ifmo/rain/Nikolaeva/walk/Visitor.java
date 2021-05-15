package ru.ifmo.rain.Nikolaeva.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class Visitor extends SimpleFileVisitor<Path> {
    private final BufferedWriter writer;

    Visitor(BufferedWriter writer) {
        this.writer = writer;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        try {
            write(path, HashSum.FNVHash(path));
        } catch (IOException e) {
            write(path, 0);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
        write(path, 0);
        return FileVisitResult.CONTINUE;
    }

    private void write(Path path, int hash) throws IOException {
        writer.write(String.format("%08x", hash) + " " + path);
        writer.newLine();
    }
}
