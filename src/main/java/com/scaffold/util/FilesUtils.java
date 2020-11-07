package com.scaffold.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FilesUtils {

    /**
     * Copy contents of source folder to the destination folder.
     */
    public static void copyFolder(Path src, Path dest) throws IOException {
        Files.walk(src).forEach(source -> copy(source, dest.resolve(src.relativize(source))));
    }

    /**
     * Copy individual files.
     */
    private static void copy(Path source, Path dest) {
        try {
            if (Files.exists(dest)) {
                deleteFolder(new File(dest.toString()));
            }
            Files.copy(source, dest);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Delete folder contents recursively (equivalent to `rm -rf`).
     */
    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) { // some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    /**
     * Get file name of the module file.
     * "module.py" -> "module"
     * "module"    -> "module"
     */
    public static String getFilenameWithoutExt(String fileName) {
        int pos = fileName.lastIndexOf(".");
        String nameWithoutExt;
        if (pos > 0) {
            nameWithoutExt = fileName.substring(0, pos);
            return nameWithoutExt;
        }
        return fileName;
    }

    /**
     * Check whether a module represents the business source code module vs dunder
     * modules e.g. __init__.py
     */
    public static boolean isBusinessModule(String fileName) {
        return !fileName.startsWith("__") || !fileName.endsWith("__");
    }
}
