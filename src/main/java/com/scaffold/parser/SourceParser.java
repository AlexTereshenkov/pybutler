/*
The parsing approach has been adopted from
https://github.com/bkiers/python3-parser/blob/master/
src/main/java/nl/bigo/pythonparser/Main.java
*/

package com.scaffold.parser;

import com.scaffold.writer.PyFunction;
import com.scaffold.writer.PyModule;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Python source parser class that provides methods to extract information
 * about standalone module functions and class methods.
 */
public class SourceParser {

    public final PyModule pyModule;
    private final Python3Parser parser;

    public SourceParser(String filePath) throws Exception {
        this.pyModule = new PyModule(filePath);
        File moduleFile = new File(filePath);
        String sourceCode = Files.readString(Paths.get(moduleFile.getAbsolutePath()));
        this.parser = new Builder.Parser(sourceCode).build();
    }

    /**
     * Load functions metadata into the Python module object.
     */
    public void loadFunctions() {
        ParseTreeWalker.DEFAULT.walk(new Python3BaseListener() {

            @Override
            public void enterFuncdef(Python3Parser.FuncdefContext ctx) {
                String functionName = ctx.NAME().getText();
                // function parameters are not of interest, but can be obtained with
                // String functionParams = ctx.parameters().getText();

                // get context of function definition
                ArrayList<ParserRuleContext> parents = new ArrayList<>();
                parents.add(ctx.getParent());
                int maxNestLevel = 100;
                for (int lastParentIndex = 0; lastParentIndex < maxNestLevel; lastParentIndex++) {
                    ParserRuleContext parent = getParent(parents.get(lastParentIndex));
                    // module level function
                    if (parent == null) {
                        if (isTestableFunction(functionName)) {
                            pyModule.functions
                                    .add(new PyFunction(".", functionName, getFunctionDocstring()));
                        }
                        break;

                    }
                    // nested functions are not of interest but can be obtained with
                    // if (parent instanceof Python3Parser.FuncdefContext)

                    // class method
                    if (parent instanceof Python3Parser.ClassdefContext) {
                        if (isTestableFunction(functionName)) {
                            PyFunction func = new PyFunction(
                                    ((Python3Parser.ClassdefContext) parent).NAME().getText(), functionName,
                                    getFunctionDocstring());
                            pyModule.functions.add(func);
                        }
                        break;
                    }
                    parents.add(parent);
                }
            }
        }, this.parser.file_input());
    }

    /**
     * Get parent of an object in the AST tree.
     */
    private static ParserRuleContext getParent(ParserRuleContext parent) {
        return parent.getParent();
    }

    /**
     * Get source function docstring.
     */
    private String getFunctionDocstring() {
        // implement this method if the source function docstring will ever be required
        return "function docstring";
    }

    /**
     * Get whether a function name should be tested
     * (to avoid getting dunder, or magic, methods into tests).
     */
    private boolean isTestableFunction(String functionName) {
        return !functionName.startsWith("__") || !functionName.endsWith("__");
    }
}
