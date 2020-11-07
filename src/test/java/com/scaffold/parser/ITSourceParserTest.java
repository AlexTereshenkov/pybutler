package com.scaffold.parser;

import com.scaffold.console.Runner;
import com.scaffold.util.FilesUtils;
import junit.framework.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.scaffold.console.Definitions.*;
import static com.scaffold.writer.PyModule.pythonModuleExtension;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ITSourceParserTest {

    @Test
    public void getClassMethods() throws Exception {
        SourceParser parser = new SourceParser("src/test/resources/get_class_methods.py");
        parser.loadFunctions();
        assertEquals("class1_method1", parser.pyModule.functions.get(0).name);
        assertEquals("Class1", parser.pyModule.functions.get(0).parent);
    }

    @Test(expected = RuntimeException.class)
    public void parseInvalidPythonFile() throws Exception {
        SourceParser parser = new SourceParser("src/test/resources/invalid_python.py");
        parser.loadFunctions();
    }

    @Test
    public void simpleFunctionTestNoneDocstrings() throws Exception {
        Runner runner = new Runner();
        runner.config.loadSourceModules("src/test/resources/cases/Docstrings/none.py");
        runner.config.testOutputDirName = ".";
        runner.config.testDocstring = DocstringType.NONE;
        runner.config.testSuiteHaveSetUp = false;
        runner.config.testSuiteHaveTearDown = false;
        runner.generateTests();

        List<String> actualStrings = Files.readAllLines(Paths.get("src/test/resources/cases/Docstrings/test_none.py"));
        List<String> expectedStrings = Files.readAllLines(Paths.get("src/test/resources/cases/Docstrings/test_none_expected.py"));
        assertEquals(actualStrings, expectedStrings);
        FilesUtils.deleteFolder(new File("src/test/resources/cases/Docstrings/test_none.py"));
    }

    @Test
    public void simpleFunctionTestEmptyDocstrings() throws Exception {
        Runner runner = new Runner();
        runner.config.loadSourceModules("src/test/resources/cases/Docstrings/empty.py");
        runner.config.testOutputDirName = ".";
        runner.config.testDocstring = DocstringType.EMPTY;
        runner.config.testSuiteHaveSetUp = false;
        runner.config.testSuiteHaveTearDown = false;
        runner.generateTests();

        List<String> actualStrings = Files.readAllLines(Paths.get("src/test/resources/cases/Docstrings/test_empty.py"));
        List<String> expectedStrings = Files.readAllLines(Paths.get("src/test/resources/cases/Docstrings/test_empty_expected.py"));
        assertEquals(actualStrings, expectedStrings);
        FilesUtils.deleteFolder(new File("src/test/resources/cases/Docstrings/test_empty.py"));
    }

    @Test
    public void simpleFunctionTestSourceDocstrings() throws Exception {
        Runner runner = new Runner();
        runner.config.loadSourceModules("src/test/resources/cases/Docstrings/source.py");
        runner.config.testOutputDirName = ".";
        runner.config.testDocstring = DocstringType.SOURCE;
        runner.config.testSuiteHaveSetUp = false;
        runner.config.testSuiteHaveTearDown = false;
        runner.generateTests();

        List<String> actualStrings = Files.readAllLines(Paths.get("src/test/resources/cases/Docstrings/test_source.py"));
        List<String> expectedStrings = Files.readAllLines(Paths.get("src/test/resources/cases/Docstrings/test_source_expected.py"));
        assertEquals(actualStrings, expectedStrings);

        FilesUtils
                .deleteFolder(new File("src/test/resources/cases/Docstrings/test_source.py"));
    }

    @Test
    public void createTestsRecursively() throws Exception {
        FilesUtils.copyFolder(
                Paths.get("src/test/resources/cases/MultiplePaths/MultiplePathsTemplate"),
                Paths.get("src/test/resources/cases/MultiplePaths/MultiplePathsGenTests"));

        Runner runner = new Runner();
        runner.config.loadSourceModules("src/test/resources/cases/MultiplePaths/MultiplePathsGenTests");
        runner.config.loadTestsOutputDir("tests");
        runner.config.loadTestImportPackages("os;sys;unittest;re;shutil");
        runner.config.loadTestIndentation("2");
        runner.config.loadTestState(ABBREV_TESTSTATE_PASS);
        runner.config.loadTestFunctionNamePrefix("test");
        runner.config.loadTestFunctionDocstringType(ABBREV_DOCSTRINGS_SOURCE);
        runner.config.loadTestsInsideClass(ABBREV_YES);
        if (runner.config.testsInsideClass) {
            runner.config.loadTestSuiteClassName("TestCase");
            runner.config.loadTestSuiteHaveSetUp(ABBREV_YES);
            runner.config.loadTestSuiteHaveTearDown(ABBREV_YES);
        }
        runner.generateTests();

        List<Path> paths = Files
                .walk(Paths.get("src/test/resources/cases/MultiplePaths/MultiplePathsGenTests"))
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().endsWith(pythonModuleExtension))
                .collect(Collectors.toList());

        for (Path path : paths) {
            Path actualPath = Paths.get(path.toString());
            Path expectedPath = Paths.get(path.toString().replace("MultiplePathsGenTests",
                    "MultiplePathsGenTestsExpected"));

            List<String> actualStrings = Files.readAllLines(actualPath);
            List<String> expectedStrings = Files.readAllLines(expectedPath);

            if (!actualStrings.equals(expectedStrings)) {
                fail(MessageFormat.format("Diff found between {0} -> {1} {0} -> {2}",
                        System.lineSeparator(), actualPath, expectedPath));
            }
        }
    }

    @Test
    public void skipModulesWithNoFuncs() throws Exception {
        Runner runner = new Runner();
        runner.config.loadSourceModules("src/test/resources/cases/ModulesWithNoFuncs");
        runner.config.testOutputDirName = "tests";
        runner.config.testSuiteHaveSetUp = false;
        runner.config.testSuiteHaveTearDown = false;
        runner.generateTests();

        List<Path> actualPaths = Files
                .walk(Paths.get("src/test/resources/cases/ModulesWithNoFuncs",
                        runner.config.testOutputDirName))
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().endsWith(pythonModuleExtension))
                .collect(Collectors.toList());
        List<Path> expectedPaths = Arrays.asList(
                Paths.get("src/test/resources/cases/ModulesWithNoFuncs",
                        runner.config.testOutputDirName, "test_simple.py"),
                Paths.get("src/test/resources/cases/ModulesWithNoFuncs",
                        runner.config.testOutputDirName, "test_dunder_and_testable.py"));
        actualPaths.sort(Comparator.comparing(Path::getFileName));
        expectedPaths.sort(Comparator.comparing(Path::getFileName));
        Assert.assertEquals(actualPaths, expectedPaths);
        FilesUtils
                .deleteFolder(new File(Paths.get("src/test/resources/cases/ModulesWithNoFuncs",
                        runner.config.testOutputDirName).toString()));
    }

    @Test
    public void skipDunderModules() throws Exception {
        Runner runner = new Runner();
        runner.config.loadSourceModules("src/test/resources/cases/DunderModules");
        runner.config.testOutputDirName = "tests";
        runner.config.testSuiteHaveSetUp = false;
        runner.config.testSuiteHaveTearDown = false;
        runner.generateTests();

        List<Path> actualPaths = Files
                .walk(Paths.get("src/test/resources/cases/DunderModules",
                        runner.config.testOutputDirName))
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().endsWith(pythonModuleExtension))
                .collect(Collectors.toList());
        List<Path> expectedPaths = Arrays.asList(
                Paths.get("src/test/resources/cases/DunderModules",
                        runner.config.testOutputDirName, "test_simple.py"),
                Paths.get("src/test/resources/cases/DunderModules",
                        runner.config.testOutputDirName, "test___private.py"));
        actualPaths.sort(Comparator.comparing(Path::getFileName));
        expectedPaths.sort(Comparator.comparing(Path::getFileName));
        Assert.assertEquals(actualPaths, expectedPaths);
        FilesUtils.deleteFolder(new File(Paths
                .get("src/test/resources/cases/DunderModules", runner.config.testOutputDirName)
                .toString()));
    }

    @Test
    public void folderToExclude() throws Exception {
        Runner runner = new Runner();
        runner.config.foldersToIgnore.add("testing");
        runner.config.testOutputDirName = "unit_tests";
        runner.config.testSuiteHaveSetUp = false;
        runner.config.testSuiteHaveTearDown = false;

        runner.config.loadSourceModules("src/test/resources/cases/FolderToExclude");
        runner.generateTests();

        Long actualFilesCount = Files.walk(Paths.get("src/test/resources/cases/FolderToExclude"))
                .parallel()
                .filter(p -> !p.toFile().isDirectory())
                .count();
        Long expectedFilesCount = Files.walk(Paths.get("src/test/resources/cases/FolderToExcludeExpected"))
                .parallel()
                .filter(p -> !p.toFile().isDirectory())
                .count();
        Assert.assertEquals(actualFilesCount, expectedFilesCount);

        List<Path> actualPaths = Files.walk(Paths.get("src/test/resources/cases/FolderToExclude"))
                .parallel()
                .filter(p -> !p.toFile().isDirectory())
                .collect(Collectors.toList());
        List<Path> expectedPaths = Files.walk(Paths.get("src/test/resources/cases/FolderToExclude"))
                .parallel()
                .filter(p -> !p.toFile().isDirectory())
                .collect(Collectors.toList());
        actualPaths.sort(Comparator.comparing(Path::getFileName));
        expectedPaths.sort(Comparator.comparing(Path::getFileName));

        for (int i = 0; i < actualPaths.size(); i++) {
            List<String> actualStrings = Files.readAllLines(actualPaths.get(i));
            List<String> expectedStrings = Files.readAllLines(expectedPaths.get(i));
            assertEquals(actualStrings, expectedStrings);
        }

        ArrayList<String> contentsDirectories = new ArrayList<>(
                Arrays.asList("src/test/resources/cases/FolderToExclude",
                        "src/test/resources/cases/FolderToExclude/simpleFolder"));

        for (String dir : contentsDirectories) {
            FilesUtils.deleteFolder(
                    new File(Paths.get(dir, runner.config.testOutputDirName).toString()));
        }
    }

    @Test
    public void modulesToExcludeWithPattern() throws Exception {
        Runner runner = new Runner();
        runner.config.testOutputDirName = "tests";
        runner.config.moduleNamePatternsToIgnore.add("staging_");
        runner.config.testSuiteHaveSetUp = false;
        runner.config.testSuiteHaveTearDown = false;

        runner.config.loadSourceModules("src/test/resources/cases/ModulesToExcludeWithPattern");
        runner.generateTests();

        List<Path> actualPaths = Files
                .walk(Paths.get("src/test/resources/cases/ModulesToExcludeWithPattern",
                        runner.config.testOutputDirName))
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().endsWith(pythonModuleExtension))
                .collect(Collectors.toList());
        List<Path> expectedPaths = Arrays
                .asList(Paths.get("src/test/resources/cases/ModulesToExcludeWithPattern",
                        runner.config.testOutputDirName, "test_simple.py"));
        actualPaths.sort(Comparator.comparing(Path::getFileName));
        expectedPaths.sort(Comparator.comparing(Path::getFileName));
        Assert.assertEquals(actualPaths, expectedPaths);

        for (int i = 0; i < actualPaths.size(); i++) {
            List<String> actualStrings = Files.readAllLines(actualPaths.get(i));
            List<String> expectedStrings = Files.readAllLines(expectedPaths.get(i));
            assertEquals(actualStrings, expectedStrings);
        }
    }
}
