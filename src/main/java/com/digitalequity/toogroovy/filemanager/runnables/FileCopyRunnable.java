package com.digitalequity.toogroovy.filemanager.runnables;

import com.digitalequity.toogroovy.filemanager.utils.IOUtils;

import java.io.File;
import java.io.IOException;

public class FileCopyRunnable implements Runnable {
    final File srcFile;
    final File destFile;

    public FileCopyRunnable(File src, File dest) {
        this.srcFile = src;
        this.destFile = dest;
    }

    @Override
    public void run() {
        try {
            IOUtils.copyFile(srcFile, destFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
