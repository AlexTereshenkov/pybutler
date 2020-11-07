package com.scaffold.writer;

/**
 * Representation of a Python function.
 */
public class PyFunction {
    public final String parent;
    public final String name;
    public final String docstring;

    public PyFunction(String parent, String name, String docstring) {
        this.parent = parent;
        this.name = name;
        this.docstring = docstring;
    }

    public String toString() {
        return this.parent + "/" + this.name;
    }
}
