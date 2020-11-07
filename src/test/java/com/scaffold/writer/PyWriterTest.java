package com.scaffold.writer;

import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.scaffold.console.Definitions.*;
import static org.junit.Assert.*;


class MockPyWriterConstructDocString extends PyWriter {
    MockPyWriterConstructDocString(PyWriterConfig config, PyModule pyModule, Path outFilePath) {
        super(config, pyModule, outFilePath);
    }

    String constructDocString(PyFunction function) {
        return "docstring body";
    }
}

public class PyWriterTest {

    final String moduleFunction = MessageFormat.format(
            "def foo():{0}"
                    + "  \"\"\"Docstring body\"\"\"{0}"
                    + "  print(){0}"
                    + "  return{0}",
            System.lineSeparator());

    @Test
    public void testAddImportPackages() throws IOException {
        final TemporaryFolder testFolder = new TemporaryFolder();
        testFolder.create();
        final String module = testFolder.newFile("module.py").toString();
        Files.write(Paths.get(module), moduleFunction.getBytes(StandardCharsets.UTF_8));

        PyWriterConfig config = new PyWriterConfig();
        config.loadTestImportPackages("os;sys");
        PyModule pyModule = new PyModule(module);
        PyWriter writer = new PyWriter(config, pyModule, Paths.get(module));

        writer.addImportPackages();
        List<String> expectedPackages = Arrays.asList("import os", "import sys");
        assertTrue(expectedPackages.stream().allMatch(writer.builder.toString()::contains));
    }

    @Test
    public void testGetImportTestFrameWorkPyTest() throws IOException {
        final TemporaryFolder testFolder = new TemporaryFolder();
        testFolder.create();
        final String module = testFolder.newFile("module.py").toString();
        Files.write(Paths.get(module), moduleFunction.getBytes(StandardCharsets.UTF_8));
        PyModule pyModule = new PyModule(module);

        // pytest not in list of packages to import
        PyWriterConfig configPyTest = new PyWriterConfig();
        configPyTest.loadTestFrameworkName(ABBREV_PYTEST);
        PyWriter writerPyTest = new PyWriter(configPyTest, pyModule, Paths.get(module));
        String importString = writerPyTest.getImportTestFrameWork();
        assertEquals(importString.split(MessageFormat.format("import {0}", PYTEST_NAME)).length - 1, 1);


        // pytest in list of packages to import
        PyWriterConfig configPyTestExtraImport = new PyWriterConfig();
        configPyTestExtraImport.loadTestImportPackages(PYTEST_NAME);
        configPyTestExtraImport.loadTestFrameworkName(ABBREV_PYTEST);
        PyWriter writerPyTestExtraImport = new PyWriter(configPyTestExtraImport, pyModule, Paths.get(module));
        String importStringExtraImport = writerPyTestExtraImport.getImportTestFrameWork();
        assertFalse(importStringExtraImport.contains(PYTEST_NAME));
    }

    @Test
    public void testGetImportTestFrameWorkUnitTest() throws IOException {
        final TemporaryFolder testFolder = new TemporaryFolder();
        testFolder.create();
        final String module = testFolder.newFile("module.py").toString();
        Files.write(Paths.get(module), moduleFunction.getBytes(StandardCharsets.UTF_8));
        PyModule pyModule = new PyModule(module);

        // not in list of packages to import
        PyWriterConfig configUnitTest = new PyWriterConfig();
        configUnitTest.loadTestFrameworkName(ABBREV_UNITTEST);
        PyWriter writerUnitTest = new PyWriter(configUnitTest, pyModule, Paths.get(module));
        String importStringUnitTest = writerUnitTest.getImportTestFrameWork();
        assertEquals(importStringUnitTest.split(MessageFormat.format("import {0}", UNITTEST_NAME)).length - 1, 1);

        // in list of packages to import
        PyWriterConfig configUnitTestExtraImport = new PyWriterConfig();
        configUnitTestExtraImport.loadTestImportPackages(UNITTEST_NAME);
        configUnitTestExtraImport.loadTestFrameworkName(ABBREV_UNITTEST);
        PyWriter writerUnitTestExtraImport = new PyWriter(configUnitTestExtraImport, pyModule, Paths.get(module));
        String importStringUnitTestExtraImport = writerUnitTestExtraImport.getImportTestFrameWork();
        assertFalse(importStringUnitTestExtraImport.contains(UNITTEST_NAME));
    }

    @Test
    public void testAddTestClass() throws IOException {
        final TemporaryFolder testFolder = new TemporaryFolder();
        testFolder.create();
        final String module = testFolder.newFile("module.py").toString();
        Files.write(Paths.get(module), moduleFunction.getBytes(StandardCharsets.UTF_8));
        PyModule pyModule = new PyModule(module);

        PyWriterConfig configUnitTest = new PyWriterConfig();
        configUnitTest.loadTestFrameworkName(ABBREV_UNITTEST);
        PyWriter writerUnitTest = new PyWriter(configUnitTest, pyModule, Paths.get(module));
        writerUnitTest.addTestClass("MyTestBaseClass");
        assertTrue(writerUnitTest.builder.toString().contains("class MyTestBaseClass(unittest.TestCase)"));

        PyWriterConfig configPyTest = new PyWriterConfig();
        configPyTest.loadTestFrameworkName(ABBREV_PYTEST);
        PyWriter writerPyTest = new PyWriter(configPyTest, pyModule, Paths.get(module));
        writerPyTest.addTestClass("MyTestBaseClass");
        assertTrue(writerPyTest.builder.toString().contains("class MyTestBaseClass:"));
    }

    @Test
    public void testAddTestClassMethods() throws IOException {
        final TemporaryFolder testFolder = new TemporaryFolder();
        testFolder.create();
        final String module = testFolder.newFile("module.py").toString();
        Files.write(Paths.get(module), moduleFunction.getBytes(StandardCharsets.UTF_8));
        PyModule pyModule = new PyModule(module);
        pyModule.functions = new ArrayList<>(Arrays.asList(
                new PyFunction("", "func1", ""),
                new PyFunction("", "func2", "")
        ));
        PyWriterConfig configUnitTest = new PyWriterConfig();
        configUnitTest.loadTestFrameworkName(ABBREV_UNITTEST);

        PyWriter writer = new MockPyWriterConstructDocString(configUnitTest, pyModule, Paths.get(module));
        writer.addTestClassMethods();
        List<String> expectedFunctions = Arrays.asList("def test_func1(self)", "def test_func2(self)");
        assertTrue(expectedFunctions.stream().allMatch(writer.builder.toString()::contains));
    }

    @Test
    public void testAddTestFunctions() throws IOException {
        final TemporaryFolder testFolder = new TemporaryFolder();
        testFolder.create();
        final String module = testFolder.newFile("module.py").toString();
        Files.write(Paths.get(module), moduleFunction.getBytes(StandardCharsets.UTF_8));
        PyModule pyModule = new PyModule(module);
        pyModule.functions = new ArrayList<>(Arrays.asList(
                new PyFunction("", "func1", ""),
                new PyFunction("", "func2", "")
        ));
        PyWriterConfig configUnitTest = new PyWriterConfig();
        configUnitTest.loadTestFrameworkName(ABBREV_UNITTEST);

        PyWriter writer = new MockPyWriterConstructDocString(configUnitTest, pyModule, Paths.get(module));
        writer.addTestFunctions();
        List<String> expectedFunctions = Arrays.asList("def test_func1()", "def test_func2()");
        assertTrue(expectedFunctions.stream().allMatch(writer.builder.toString()::contains));
    }

    @Test
    public void testAddTestClassSetUp() throws IOException {
        final TemporaryFolder testFolder = new TemporaryFolder();
        testFolder.create();
        final String module = testFolder.newFile("module.py").toString();
        Files.write(Paths.get(module), moduleFunction.getBytes(StandardCharsets.UTF_8));
        PyModule pyModule = new PyModule(module);

        PyWriterConfig configUnitTest = new PyWriterConfig();
        configUnitTest.loadTestFrameworkName(ABBREV_UNITTEST);
        PyWriter writerUnitTest = new PyWriter(configUnitTest, pyModule, Paths.get(module));
        writerUnitTest.addTestClassSetUp();
        assertTrue(writerUnitTest.builder.toString().contains("setUp"));
    }

    @Test
    public void testAddTestClassTearDown() throws IOException {
        final TemporaryFolder testFolder = new TemporaryFolder();
        testFolder.create();
        final String module = testFolder.newFile("module.py").toString();
        Files.write(Paths.get(module), moduleFunction.getBytes(StandardCharsets.UTF_8));
        PyModule pyModule = new PyModule(module);

        PyWriterConfig configUnitTest = new PyWriterConfig();
        configUnitTest.loadTestFrameworkName(ABBREV_UNITTEST);
        PyWriter writerUnitTest = new PyWriter(configUnitTest, pyModule, Paths.get(module));
        writerUnitTest.addTestClassTearDown();
        assertTrue(writerUnitTest.builder.toString().contains("tearDown"));
    }

    @Test
    public void testConstructDocStringSourceType() throws IOException {
        final TemporaryFolder testFolder = new TemporaryFolder();
        testFolder.create();
        final String module = testFolder.newFile("module.py").toString();
        Files.write(Paths.get(module), moduleFunction.getBytes(StandardCharsets.UTF_8));
        PyModule pyModule = new PyModule(module);

        PyWriterConfig config = new PyWriterConfig();
        config.loadTestFrameworkName(ABBREV_UNITTEST);
        config.loadTestFunctionDocstringType(ABBREV_DOCSTRINGS_SOURCE);

        // function outside of a class
        config.loadTestsInsideClass(ABBREV_NO);
        PyWriter writer = new PyWriter(config, pyModule, Paths.get(module));
        PyFunction function = new PyFunction("", "func1", "source docstring body");
        String docStringOutsideClass = writer.constructDocString(function);
        assertEquals(docStringOutsideClass, MessageFormat.format("{0}\"\"\"{1}\"\"\"{2}", writer.indent,
                function.docstring, System.lineSeparator()));

        // function inside a class
        config.loadTestsInsideClass(ABBREV_YES);
        String docStringInsideClass = writer.constructDocString(function);
        assertEquals(docStringInsideClass, MessageFormat.format("{0}{0}\"\"\"{1}\"\"\"{2}", writer.indent,
                function.docstring, System.lineSeparator()));
    }

    @Test
    public void testConstructDocStringEmptyNoneType() throws IOException {
        final TemporaryFolder testFolder = new TemporaryFolder();
        testFolder.create();
        final String module = testFolder.newFile("module.py").toString();
        Files.write(Paths.get(module), moduleFunction.getBytes(StandardCharsets.UTF_8));
        PyModule pyModule = new PyModule(module);

        PyWriterConfig config = new PyWriterConfig();
        config.loadTestFrameworkName(ABBREV_UNITTEST);
        config.loadTestFunctionDocstringType(ABBREV_DOCSTRINGS_EMPTY);

        // function outside of a class
        config.loadTestsInsideClass(ABBREV_NO);
        PyWriter writer = new PyWriter(config, pyModule, Paths.get(module));
        PyFunction function = new PyFunction("", "func1", "source docstring body");
        String docStringOutsideClass = writer.constructDocString(function);
        assertEquals(docStringOutsideClass, MessageFormat.format("{0}\"\"\"\"\"\"{1}", writer.indent,
                System.lineSeparator()));

        // function inside a class
        config.loadTestsInsideClass(ABBREV_YES);
        String docStringInsideClass = writer.constructDocString(function);
        assertEquals(docStringInsideClass, MessageFormat.format("{0}{0}\"\"\"\"\"\"{1}", writer.indent,
                System.lineSeparator()));

        // no docstrings are required
        config.loadTestFunctionDocstringType(ABBREV_DOCSTRINGS_NONE);
        assertEquals(writer.constructDocString(function), "");
    }

    @Test
    public void testWriteTestsModule() throws IOException {
        final TemporaryFolder testFolder = new TemporaryFolder();
        testFolder.create();
        final String module = testFolder.newFile("module.py").toString();
        Files.write(Paths.get(module), moduleFunction.getBytes(StandardCharsets.UTF_8));
        PyModule pyModule = new PyModule(module);
        pyModule.functions = new ArrayList<>(Collections.singletonList(
                new PyFunction("", "foo", "Docstring body")
        ));

        PyWriterConfig config = new PyWriterConfig();
        config.loadTestFrameworkName(ABBREV_UNITTEST);
        config.loadTestFunctionDocstringType(ABBREV_DOCSTRINGS_SOURCE);

        // tests are outside of a class
        config.loadTestsInsideClass(ABBREV_NO);
        Path pathOutputOutsideClass = Paths.get(testFolder.getRoot().getAbsolutePath(), config.testModulePrefix + pyModule.nameWithExt);

        PyWriter writerInsideClass = new PyWriter(config, pyModule, pathOutputOutsideClass);
        writerInsideClass.writeTestsModule();
        assertTrue(Files.exists(pathOutputOutsideClass));
        String fileContentsOutsideClass = Files.readString(pathOutputOutsideClass, StandardCharsets.UTF_8);
        assertTrue(fileContentsOutsideClass.contains("test_foo"));

        // tests are inside a class
        config.loadTestsInsideClass(ABBREV_YES);
        Path pathOutputInsideClass = Paths.get(testFolder.getRoot().getAbsolutePath(), config.testModulePrefix + pyModule.nameWithExt);

        PyWriter writer = new PyWriter(config, pyModule, pathOutputInsideClass);
        writer.writeTestsModule();
        assertTrue(Files.exists(pathOutputInsideClass));
        String fileContentsInsideClass = Files.readString(pathOutputInsideClass, StandardCharsets.UTF_8);
        assertTrue(fileContentsInsideClass.contains("test_foo"));
    }
}
