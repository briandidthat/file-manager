package com.digitalequity.toogroovy.filemanager.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class FileManagerServiceTest {
    private FileManagerService service;
    private static final String testFileDirectory = "files/test/";
    private static final String A_FILE = testFileDirectory + "a.txt";
    private static final String B_FILE = testFileDirectory + "b.txt";
    private static final String C_FILE = testFileDirectory + "c.txt";
    private static final String D_FILE = testFileDirectory + "d.txt";
    private static final String A_COPY = testFileDirectory + "a-copy.txt";
    private static final String B_COPY = testFileDirectory + "b-copy.txt";
    private static final String C_COPY = testFileDirectory + "c-copy.txt";
    private static final String D_COPY = testFileDirectory + "d-copy.txt";


    @BeforeEach
    void setUp() {
        service = new FileManagerService();
    }

    @Test
    void copyFile() throws Exception {
        boolean copied = service.copyFile(A_FILE, A_COPY);
        assertTrue(copied);
        service.deleteFile(A_COPY);
    }

    @AfterAll
    void resetFileDirectory() {
        service.deleteFiles(new HashSet<>(List.of(A_COPY, B_COPY, C_COPY, D_COPY)));
    }

    @Test
    void copyAndDeleteFiles() throws Exception {
        Map<String, Set<String>> tasks = new HashMap<>();
        tasks.put(A_FILE, new HashSet<>(List.of(A_COPY)));
        tasks.put(B_FILE, new HashSet<>(List.of(B_COPY)));
        tasks.put(C_FILE, new HashSet<>(List.of(C_COPY)));
        tasks.put(D_FILE, new HashSet<>(List.of(D_COPY)));
        Map<String, List<Boolean>> results = service.copyFiles(tasks);

        System.out.println(results.toString());

        results.values().forEach((list) -> list.forEach(Assertions::assertTrue));
        List<Boolean> deleted = service.deleteFiles(new HashSet<>(List.of(A_COPY, B_COPY, C_COPY, D_COPY)));

    }

    @Test
    void deleteFile() {
        service.copyFile(A_FILE, A_COPY);
        boolean deleted = service.deleteFile(A_COPY);

        assertTrue(deleted);
    }

    @Test
    void deleteFiles() {
    }

    @Test
    void findPattern() {
    }

    @Test
    void findPatternAsync() {
    }
}