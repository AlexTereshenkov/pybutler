package com.scaffold.console;

import com.scaffold.util.FilesUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ITRunnerTest {

    @Test
    public void testMain() throws Exception {
        InputStream in = new ByteArrayInputStream(
                MessageFormat.format(
                        "{0}" // not providing the path for the first time
                                + "src/test/resources/runner/simple.py{0}" // path to input dir
                                + "testing{0}" // foldersToIgnore
                                + "_database{0}" // moduleNamePatternsToIgnore
                                + "{0}" // testLocation
                                + "tests{0}" // testOutputDir
                                + "u{0}" // testFramework
                                + "os;sys{0}" // importPackages
                                + "4{0}" // indentation
                                + "f{0}" // testState
                                + "test_{0}" // testModulePrefix
                                + "test_{0}" // testNamePrefix
                                + "{0}" // testDocstring
                                // + "y{0}" // testsInsideClass not required for unittest framework
                                + "TestBase{0}" // testSuiteClassName
                                + "y{0}" // testSuiteHaveSetUp
                                + "y{0}" // testSuiteHaveTearDown
                        , System.lineSeparator())
                        .getBytes());
        System.setIn(in);
        Runner.main(null);

        List<String> actualStrings = Files.readAllLines(Paths.get("src/test/resources/runner/tests/test_simple.py"));
        List<String> expectedStrings = Files.readAllLines(Paths.get("src/test/resources/runner/test_simple_expected.py"));
        assertEquals(actualStrings, expectedStrings);
        FilesUtils.deleteFolder(new File("src/test/resources/runner/tests"));
    }
}
