package com.digitalequity.toogroovy.filemanager.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class IOUtils {
    private static final Logger logger = LoggerFactory.getLogger(IOUtils.class);

    private IOUtils() {
    }

    public static boolean deleteFile(String fileName) throws Exception {
        File file = new File(fileName);
        return file.delete();
    }

    public static void copyContents(InputStream src, OutputStream dest) throws IOException {
        int value;
        while ((value = src.read()) > 0) {
            dest.write((char) value);
        }
    }

    public static void copyFile(File src, File dest) throws IOException {
        logger.info("Copying files from {} to {}", src.getName(), dest.getName());
        FileInputStream input = new FileInputStream(src);
        FileOutputStream output = new FileOutputStream(dest);
        // copy using utility method
        copyContents(input, output);

        input.close();
        output.close();
        logger.info("Successfully copied contents from {} to {}", src.getName(), dest.getName());
    }

    public static List<Integer> find(File file, String pattern) {
        List<Integer> lineNumbers = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            int lineNumber = 1;
            String line;

            // iterate through the lines of the file
            while ((line = br.readLine()) != null) {
                // add line number to lineNumbers if pattern is found
                if (line.contains(pattern)) {
                    lineNumbers.add(lineNumber);
                }
                // increment the line number to proceed to the next line
                lineNumber++;
            }
        } catch (IOException e) {
            logger.info("I/O Error occurred. Error: {}", e.getMessage());

        }
        return lineNumbers;
    }
}
