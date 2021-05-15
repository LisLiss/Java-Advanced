package ru.ifmo.rain.Nikolaeva.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RecursiveWalk {
    private final Path inputPath;
    private final Path outputPath;

    private RecursiveWalk(final String input, final String output) throws RecursiveWalkException {
        try {
            inputPath = Paths.get(input);
        } catch (InvalidPathException e) {
            throw new RecursiveWalkException("Invalid input path");
        }
        try {
            outputPath = Paths.get(output);
        } catch (InvalidPathException e) {
            throw new RecursiveWalkException("Invalid output path");
        }
        if (outputPath.getParent() != null) {
            try {
                Files.createDirectories(outputPath.getParent());
            } catch (IOException e) {
                throw new RecursiveWalkException("Can't create directory output file");
            }
        }
    }

    public static void main(String[] args) {
        try {
            if (args == null || args.length != 2) {
                throw new RecursiveWalkException("Wrong number of arguments");
            }
            if (args[0] == null || args[1] == null) {
                throw new RecursiveWalkException("Name of file is null");
            }
            RecursiveWalk recursiveWalk = new RecursiveWalk(args[0], args[1]);
            try (BufferedReader reader = Files.newBufferedReader(recursiveWalk.inputPath, StandardCharsets.UTF_8)) {
                try (BufferedWriter writer = Files.newBufferedWriter(recursiveWalk.outputPath, StandardCharsets.UTF_8)) {
                    Visitor visitor = new Visitor(writer);
                    String path;
                    while ((path = reader.readLine()) != null) {
                        try {
                            Files.walkFileTree(Paths.get(path), visitor);
                        } catch (InvalidPathException e) {
                            writer.write(String.format("%08x", 0) + " " + path);
                            writer.newLine();
                        }
                    }
                }
                catch (SecurityException e){
                    throw new RecursiveWalkException("Don't have access for output file");
                }catch (IOException e){
                    throw new RecursiveWalkException(e.getMessage());
                }
            } catch (FileNotFoundException e){
                throw new RecursiveWalkException("Don't have input file");
            } catch (SecurityException e){
                throw new RecursiveWalkException("Don't have access for input file");
            } catch (UnsupportedEncodingException e){
                throw new RecursiveWalkException("Wrong encoding in input file");
            } catch (IOException e){
                throw new RecursiveWalkException(e.getMessage());
            }
        } catch (
                RecursiveWalkException e) {
            System.err.println(e.getMessage());
        }
    }
}
