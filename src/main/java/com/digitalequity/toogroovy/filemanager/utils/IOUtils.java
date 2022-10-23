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

    public static boolean deleteFile(String fileName) throws SecurityException {
        boolean deleted = new File(fileName).delete();
        if (deleted) {
            logger.info("Successfully deleted {}.", fileName);
        }
        return deleted;
    }

    public static void copyContents(InputStream src, OutputStream dest) throws IOException {
        int value;
        while ((value = src.read()) > 0) {
            dest.write((char) value);
        }
    }

    public static boolean copyFile(File src, File dest) throws IOException {
        logger.info("Copying contents from {} to {}", src.getName(), dest.getName());
        final FileInputStream input = new FileInputStream(src);
        final FileOutputStream output = new FileOutputStream(dest);
        // copy using utility method
        copyContents(input, output);

        input.close();
        output.close();
        logger.info("Successfully copied contents from {} to {}", src.getName(), dest.getName());
        return true;
    }

    public static List<Integer> find(File file, String pattern) throws IOException {
        final List<Integer> lineNumbers = new ArrayList<>();
        final BufferedReader br = new BufferedReader(new FileReader(file));
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
        return lineNumbers;
    }
}
