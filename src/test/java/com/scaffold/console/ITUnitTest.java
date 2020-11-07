package com.scaffold.console;

import com.scaffold.parser.SourceParser;
import com.scaffold.writer.PyWriter;
import com.scaffold.writer.PyWriterConfig;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ITUnitTest {

    // Java only knows about the platform it is currently running on, so it can only give
    // you a platform-dependent output on that platform. Since the expected files to compare
    // against have been produced on a Linux machine, one cannot compare the files contents byte-by-byte
    // when running on Windows/MacOS machine, and instead need to compare an array of lines with file contents.

    @Test
    public void simpleClassTests() throws Exception {
        SourceParser parser = new SourceParser(
                "src/test/resources/unittest/class/simple/simple.py");
        PyWriterConfig config = new PyWriterConfig();

        config.loadTestImportPackages("");
        parser.loadFunctions();
        Path path = Paths.get(parser.pyModule.baseDir, "test_" + parser.pyModule.nameWithExt);
        PyWriter writer = new PyWriter(config, parser.pyModule, path);
        writer.writeTestsModule();

        List<String> actualStrings = Files.readAllLines(Paths.get("src/test/resources/unittest/class/simple/test_simple.py"));
        List<String> expectedStrings = Files.readAllLines(Paths.get("src/test/resources/unittest/class/simple/test_simple_expected.py"));
        assertEquals(actualStrings, expectedStrings);
    }

    @Test
    public void advancedClassTests() throws Exception {
        SourceParser parser = new SourceParser(
                "src/test/resources/unittest/class/advanced/advanced.py");
        PyWriterConfig config = new PyWriterConfig();
        config.importPackages = Arrays.asList("os;sys;re;shutil;unittest".split(";", -1));
        config.indentSize = 4;
        config.testDocstring = Definitions.DocstringType.EMPTY;
        config.testFramework = Definitions.TestFramework.UNITTEST;
        config.testModulePrefix = "testing_";
        config.testFunctionNamePrefix = "unit_";
        config.testsInsideClass = true;
        config.testOutputDirName = "custom_tests";
        config.testStateStatement = "assert 1 == 1";
        config.testSuiteClassName = "CustomTestSuite";
        config.testSuiteHaveSetUp = true;
        config.testSuiteHaveTearDown = true;

        Path destDir = Paths.get(parser.pyModule.baseDir, config.testOutputDirName);
        if (!Files.exists(destDir)) {
            (new File(destDir.toString())).mkdirs();
        }

        parser.loadFunctions();
        Path path = Paths.get(destDir.toString(),
                config.testModulePrefix + parser.pyModule.nameWithExt);
        PyWriter writer = new PyWriter(config, parser.pyModule, path);
        writer.writeTestsModule();

        Path expectedPath = Paths.get(MessageFormat.format(
                "src/test/resources/unittest/class/advanced/{0}/{1}advanced_expected.py",
                config.testOutputDirName, config.testModulePrefix));

        List<String> actualStrings = Files.readAllLines(path);
        List<String> expectedStrings = Files.readAllLines(expectedPath);
        if (!actualStrings.equals(expectedStrings)) {
            fail(MessageFormat.format("Diff found between {0} -> {1} {0} -> {2}",
                    System.lineSeparator(), path, expectedPath));
        }
    }

}
