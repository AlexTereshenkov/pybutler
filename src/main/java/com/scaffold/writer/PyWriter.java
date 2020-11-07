package com.scaffold.writer;

import com.scaffold.console.Definitions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Collections;

import static com.scaffold.console.Definitions.PYTEST_NAME;
import static com.scaffold.console.Definitions.UNITTEST_NAME;

public class PyWriter {

    final StringBuilder builder;
    final PyWriterConfig config;
    final PyModule pyModule;
    final String indent;
    final Path outFilePath;

    public PyWriter(PyWriterConfig config, PyModule pyModule, Path outFilePath) {
        this.builder = new StringBuilder();
        this.config = config;
        this.pyModule = pyModule;
        this.indent = String.join("", Collections.nCopies(config.indentSize, " "));
        this.outFilePath = outFilePath;
    }

    /**
     * Add import statements, if any.
     */
    void addImportPackages() {
        if (this.config.importPackages.toArray().length > 0) {
            StringBuilder importString = new StringBuilder();
            for (String packageName : config.importPackages) {
                importString.append(
                        MessageFormat.format("import {0}{1}", packageName, System.lineSeparator()));
            }
            this.builder.append(importString);
        }
        this.builder.append(this.getImportTestFrameWork());
    }

    /**
     * Get import test framework string e.g. `import pytest`.
     */
    String getImportTestFrameWork() {
        String importString = "";
        if (!config.importPackages.contains(UNITTEST_NAME)
                && config.testFramework == Definitions.TestFramework.UNITTEST) {
            importString += MessageFormat.format("import {1}{0}{0}",
                    System.lineSeparator(), UNITTEST_NAME);
        } else if (!config.importPackages.contains(PYTEST_NAME)
                && config.testFramework == Definitions.TestFramework.PYTEST) {
            importString += MessageFormat.format("import {1}{0}{0}",
                    System.lineSeparator(), PYTEST_NAME);
        } else {
            importString += MessageFormat.format("{0}", System.lineSeparator());
        }
        return importString;
    }

    /**
     * Add class definition, if configuration requires.
     */
    void addTestClass(String testSuiteClassName) {
        String startTestString = "";
        if (config.testFramework == Definitions.TestFramework.UNITTEST) {
            startTestString += MessageFormat.format("class {0}(unittest.TestCase):{1}{1}",
                    testSuiteClassName, System.lineSeparator());
        } else if (config.testFramework == Definitions.TestFramework.PYTEST) {
            startTestString += MessageFormat.format("class {0}:{1}{1}", testSuiteClassName,
                    System.lineSeparator());
        }
        this.builder.append(startTestString);
    }

    /**
     * Add test as a class method.
     */
    void addTestClassMethods() {
        int index = 1;
        String functionSeparator;
        for (PyFunction function : this.pyModule.functions) {
            if (index != pyModule.functions.size()) {
                functionSeparator = MessageFormat.format("{0}{0}", System.lineSeparator());
            } else {
                functionSeparator = MessageFormat.format("{0}", System.lineSeparator());
            }

            String docstring = constructDocString(function);
            //@formatter:off
            String moduleFunction = MessageFormat.format(
                    "{1}def {4}{0}(self):{2}"
                            + "{5}"
                            + "{1}{1}{3}{2}"
                            + "{1}{1}return{6}",
                    function.name, //0
                    this.indent, //1
                    System.lineSeparator(), //2
                    this.config.testStateStatement, //3
                    this.config.testFunctionNamePrefix, //4
                    docstring, //5
                    functionSeparator); //6

            this.builder.append(moduleFunction);
            //@formatter:on
            index++;
        }
    }

    /**
     * Add standalone test functions to the module scope.
     */
    void addTestFunctions() {
        int index = 1;
        String functionSeparator;
        for (PyFunction function : pyModule.functions) {
            if (index != pyModule.functions.size()) {
                functionSeparator = MessageFormat.format("{0}{0}", System.lineSeparator());
            } else {
                functionSeparator = MessageFormat.format("{0}", System.lineSeparator());
            }

            String docstring = constructDocString(function);
            //@formatter:off
            String moduleFunction = MessageFormat.format(
                    "def {4}{0}():{2}"
                            + "{5}"
                            + "{1}{3}{2}"
                            + "{1}return{6}",
                    function.name, // 0
                    this.indent, // 1
                    System.lineSeparator(), // 2
                    this.config.testStateStatement, // 3
                    this.config.testFunctionNamePrefix, //4
                    docstring, // 5
                    functionSeparator); //6

            this.builder.append(moduleFunction);
            //@formatter:on
            index++;
        }
    }

    /**
     * Add setUp method to the test class.
     */
    void addTestClassSetUp() {
        String setUpString = "";
        setUpString += MessageFormat.format("{0}def setUp(self):{1}{0}{0}return{1}{1}",
                this.indent, System.lineSeparator());
        this.builder.append(setUpString);
    }

    /**
     * Add tearDown method to the test class.
     */
    void addTestClassTearDown() {
        this.builder
                .append(MessageFormat.format("{1}{0}def tearDown(self):{1}{0}{0}return{1}",
                        this.indent, System.lineSeparator()));
    }

    /**
     * Construct a docstring to be written for a test function.
     */
    String constructDocString(PyFunction function) {
        String docstring = "";
        switch (this.config.testDocstring) {
            case SOURCE:
                if (this.config.testsInsideClass) {
                    docstring = MessageFormat.format("{0}{0}\"\"\"{1}\"\"\"{2}", this.indent,
                            function.docstring, System.lineSeparator());
                } else {
                    docstring = MessageFormat.format("{0}\"\"\"{1}\"\"\"{2}", this.indent,
                            function.docstring, System.lineSeparator());
                }
                break;
            case EMPTY:
                if (this.config.testsInsideClass) {
                    docstring = MessageFormat.format("{0}{0}\"\"\"\"\"\"{1}", this.indent,
                            System.lineSeparator());
                } else {
                    docstring = MessageFormat.format("{0}\"\"\"\"\"\"{1}", this.indent,
                            System.lineSeparator());
                }
                break;
            case NONE:
                docstring = "";
                break;
        }
        return docstring;
    }

    /**
     * Write test module on disk with all the tests.
     */
    public void writeTestsModule() throws IOException {

        this.addImportPackages();

        if (this.config.testsInsideClass) {
            this.addTestClass(this.config.testSuiteClassName);
            if (this.config.testFramework == Definitions.TestFramework.UNITTEST
                    && this.config.testSuiteHaveSetUp) {
                this.addTestClassSetUp();
            }
        }

        if (this.config.testsInsideClass) {
            this.addTestClassMethods();
        } else {
            this.addTestFunctions();
        }

        if (this.config.testFramework == Definitions.TestFramework.UNITTEST
                && this.config.testsInsideClass && this.config.testSuiteHaveTearDown) {
            this.addTestClassTearDown();
        }

        Files.write(this.outFilePath,
                this.builder.toString().getBytes(StandardCharsets.UTF_8));
    }
}
