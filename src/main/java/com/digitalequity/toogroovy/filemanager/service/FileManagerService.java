package com.digitalequity.toogroovy.filemanager.service;

import com.digitalequity.toogroovy.filemanager.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    // ASYNC method definitions

    @Async
    public CompletableFuture<Boolean> copyFile(String src, String dest) {
        return CompletableFuture.supplyAsync(() -> {
            boolean copied = false;
            try {
                copied = IOUtils.copyFile(new File(src), new File(dest));
            } catch (IOException e) {
                logger.error("Error: {}", e.getMessage());
            }
            return copied;
        });
    }

    @Async
    public CompletableFuture<Boolean> deleteFile(String fileName) {
        return CompletableFuture.supplyAsync(() -> {
            boolean deleted = false;
            try {
                deleted = IOUtils.deleteFile(fileName);
            } catch (Exception e) {
                logger.error("Error: {}", e.getMessage());
            }
            return deleted;
        });
    }

    /**
     * Will copy files asynchronously. Uses LinkedHashSets for the input filenames since we would
     * like to preserve the ordering of elements as they were inserted
     *
     * @param originals LinkedHashSet of filenames containing the filenames of the original files
     * @param copies    LinkedHashSet of filenames containing the filenames of target copy files
     * @return List of boolean values representing whether the file was copied or not
     * @throws Exception InputMisMatchException if the input sets are not of same length
     */
    public List<Boolean> copyFiles(LinkedHashSet<String> originals, LinkedHashSet<String> copies) throws Exception {
        if (originals.size() != copies.size()) throw new InputMismatchException("array length must match");

        final List<Boolean> results;
        final List<CompletableFuture<Boolean>> tasks = new ArrayList<>();

        final Instant start = Instant.now();
        final Iterator<String> originalItr = originals.iterator();
        final Iterator<String> copyItr = copies.iterator();

        while (originalItr.hasNext() && copyItr.hasNext()) {
            String src = originalItr.next();
            String dest = copyItr.next();

            CompletableFuture<Boolean> future = copyFile(src, dest);
            tasks.add(future);
        }

        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
        final Instant end = Instant.now();
        logger.info("Completed async file copying in {}ms", end.minusMillis(start.toEpochMilli()).toEpochMilli());

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
                CompletableFuture<Boolean> future = copyFile(src, target);
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
        tasks = fileNames.stream().map(this::deleteFile).collect(Collectors.toList());
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
}
