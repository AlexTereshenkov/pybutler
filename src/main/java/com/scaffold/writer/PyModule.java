package com.scaffold.writer;

import com.scaffold.util.FilesUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * Representation of a Python module file.
 */
public class PyModule {
    public final String name;
    public final String nameWithExt;
    public final String baseDir;
    public ArrayList<PyFunction> functions;
    public static final String pythonModuleExtension = ".py";

    public PyModule(String path) {
        this.name = FilesUtils.getFilenameWithoutExt(new File(path).getName());
        this.nameWithExt = this.name + pythonModuleExtension;
        this.baseDir = new File(path).getParent();
        this.functions = new ArrayList<>();
    }

}
