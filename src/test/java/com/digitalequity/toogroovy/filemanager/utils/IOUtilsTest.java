package com.digitalequity.toogroovy.filemanager.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class IOUtilsTest {
    private static final String testFileDirectory = "files/test/";
    private static final String A_FILE = testFileDirectory + "a.txt";
    private static final String B_FILE = testFileDirectory + "b.txt";
    private static final String A_COPY = testFileDirectory + "a-copy.txt";
    private static final String B_COPY = testFileDirectory + "b-copy.txt";

    @BeforeEach
    void setUp() {

    }

    @Test
    void deleteFile() {
    }

    @Test
    void copyFile() {
    }

    @Test
    void find() {
    }
}