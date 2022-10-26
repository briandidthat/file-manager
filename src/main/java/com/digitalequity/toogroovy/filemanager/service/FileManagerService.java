package com.digitalequity.toogroovy.filemanager.service;

import com.digitalequity.toogroovy.filemanager.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class FileManagerService {
    private static final Logger logger = LoggerFactory.getLogger(FileManagerService.class);
    @Value("${files.test}")
    private String fileDirectory;

    public Boolean copyFile(String src, String dest) {
        boolean copied = false;
        try {
            copied = IOUtils.copyFile(new File(src), new File(dest));
        } catch (IOException e) {
            logger.error("Error: {}", e.getMessage());
        }
        return copied;
    }

    public Boolean deleteFile(String fileName) {
        boolean deleted = false;
        try {
            deleted = IOUtils.deleteFile(fileName);
        } catch (Exception e) {
            logger.error("Error: {}", e.getMessage());
        }
        return deleted;
    }

    public List<Integer> findPattern(String fileName, String pattern) {
        List<Integer> lineNumbers = null;
        try {
            lineNumbers = IOUtils.find(new File(fileName), pattern);
        } catch (IOException e) {
            logger.error("Error: {}", e.getMessage());
        }
        return lineNumbers;
    }

    // ASYNC method definitions

    @Async
    private CompletableFuture<Boolean> copyFileAsync(String src, String dest) {
        return CompletableFuture.supplyAsync(() -> copyFile(src, dest));
    }

    @Async
    private CompletableFuture<Boolean> deleteFileAsync(String fileName) {
        return CompletableFuture.supplyAsync(() -> deleteFile(fileName));
    }

    @Async
    private CompletableFuture<List<Integer>> findPatternAsync(String fileName, String pattern) {
        return CompletableFuture.supplyAsync(() -> findPattern(fileName, pattern));
    }

    /**
     * Will copy the contents of a file to the given target files
     * @param files Map containing the filename as key, and the target filenames as value
     * @return Map containing the filename as key, and the list containing status of copy as value
     * @throws Exception
     */
    public Map<String, List<Boolean>> copyFiles(Map<String, Set<String>> files) throws Exception {
        final List<CompletableFuture<Boolean>> tasks = new ArrayList<>();
        final Map<String, List<CompletableFuture<Boolean>>> completableFutures = new HashMap<>();
        final Map<String, List<Boolean>> results = new HashMap<>();

        final Set<String> keys = files.keySet();
        if (keys.size() == 0 || keys.size() > 20) throw new UnsupportedOperationException("Invalid input");

        final Instant start = Instant.now();
        keys.forEach((src) -> { // iterate through the source file array
            Set<String> targets = files.get(src); // get list of targets for this file to be copied to
            List<CompletableFuture<Boolean>> futures = new ArrayList<>();
            targets.forEach((target) -> { // iterate through the target file array for this source file
                CompletableFuture<Boolean> future = copyFileAsync(src, target);
                tasks.add(future);
                futures.add(future);
            });
            completableFutures.put(src, futures);
        });
        // wait for all completable futures to be done
        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
        final Instant end = Instant.now();
        logger.info("Completed async file copying in {}ms", end.minusMillis(start.toEpochMilli()).toEpochMilli());

        completableFutures.keySet().forEach(key -> { // iterate through the source file array
            List<CompletableFuture<Boolean>> futures = completableFutures.get(key);
            List<Boolean> completed = futures.stream().map(k -> { // iterate through the completable
                boolean result = false;
                try {
                    result = k.get();
                } catch (Exception e) {
                    logger.error("Error: {}", e.getMessage());
                }
                return result;
            }).collect(Collectors.toList());
            results.put(key, completed);
        });

        return results;
    }

    /**
     * Will delete several files asynchronously if they exist and return a list of booleans
     * @param fileNames Set of filenames to be deleted
     * @return List of booleans representing whether the file was successfully deleted
     */
    public List<Boolean> deleteFiles(Set<String> fileNames) {
        final List<Boolean> results;
        final List<CompletableFuture<Boolean>> tasks;

        final Instant start = Instant.now();
        tasks = fileNames.stream().map(this::deleteFileAsync).collect(Collectors.toList());
        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
        final Instant end = Instant.now();
        logger.info("Completed async file deletion in {}ms", end.minusMillis(start.toEpochMilli()).toEpochMilli());

        results = tasks.stream().map(k -> {
            boolean result = false;
            try {
                result = k.get();
            } catch (Exception e) {
                logger.error("Error: {}", e.getMessage());
            }
            return result;
        }).collect(Collectors.toList());

        return results;
    }

    /**
     * Will search through the provided files asynchronously for the provided pattern
     * @param fileNames Set of filenames to search through
     * @param pattern Pattern to look for in provided files
     * @return Map containing the filenames and list of line numbers in file it appeared in
     */
    public Map<String, List<Integer>> findPattern(Set<String> fileNames, String pattern) {
        final Map<String, List<Integer>> results = new HashMap<>();
        final Map<String, CompletableFuture<List<Integer>>> completableFutures = new HashMap<>();
        final List<CompletableFuture<List<Integer>>> tasks = new ArrayList<>();

        final Instant start = Instant.now();
        fileNames.forEach((fileName) -> {
            CompletableFuture<List<Integer>> task = findPatternAsync(fileName, pattern);
            completableFutures.put(fileName, task);
            tasks.add(task);
        });

        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
        final Instant end = Instant.now();
        logger.info("Completed async file deletion in {}ms", end.minusMillis(start.toEpochMilli()).toEpochMilli());

        fileNames.forEach((fileName) -> {
            CompletableFuture<List<Integer>> future = completableFutures.get(fileName);
            List<Integer> responses = null;
            try {
                responses = future.get();
            } catch (Exception e) {
                logger.info("Error: {}", e.getMessage());
            }
            results.put(fileName, responses);
        });

        return results;
    }

}
