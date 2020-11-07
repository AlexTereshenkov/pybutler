package com.scaffold.writer;

import com.scaffold.console.Definitions;
import com.scaffold.util.FilesUtils;
import com.scaffold.util.MappingUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.scaffold.console.Definitions.*;
import static com.scaffold.util.FilesUtils.isBusinessModule;
import static com.scaffold.writer.PyModule.pythonModuleExtension;

/**
 * Configuration class used for generating tests and saving them on disk.
 */
public class PyWriterConfig {
    public final Logger logger;
    public ArrayList<String> modulePaths = new ArrayList<>();

    public static final ArrayList<String> foldersToIgnoreDefault = new ArrayList<>(
            Collections.singletonList("tests"));
    public ArrayList<String> foldersToIgnore;

    public final ArrayList<String> moduleNamePatternsToIgnoreDefault = new ArrayList<>();
    public ArrayList<String> moduleNamePatternsToIgnore;

    public static final TestFramework testFrameworkDefault = TestFramework.UNITTEST;
    public TestFramework testFramework;

    public static final String testOutputDirNameDefault = "tests";
    public String testOutputDirName;

    public static final ArrayList<String> importPackagesDefault = new ArrayList<>();
    public List<String> importPackages;

    public static final int indentSizeDefault = 2;
    public int indentSize;

    public static final TestStateType testStateDefault = TestStateType.PASS;
    public static final Map<TestStateType, String> testStateMapping = PyWriterConfig
            .testStateMapper();
    public TestStateType testState;
    public String testStateStatement;

    public static final String testModulePrefixDefault = "test_";
    public String testModulePrefix;

    public static final String testFunctionNamePrefixDefault = "test_";
    public String testFunctionNamePrefix;

    public static final DocstringType testDocstringDefault = DocstringType.NONE;
    public DocstringType testDocstring;

    public static final boolean testSuiteHaveSetUpDefault = true;
    public boolean testSuiteHaveSetUp;
    public static final Map<String, Boolean> testSuiteHaveSetUpMapping = MappingUtils
            .yesNoTrueFalseMapper();

    public static final boolean testSuiteHaveTearDownDefault = true;
    public boolean testSuiteHaveTearDown;
    public static final Map<String, Boolean> testSuiteHaveTearDownMapping = MappingUtils
            .yesNoTrueFalseMapper();

    public static final boolean testsInsideClassDefault = true;
    public boolean testsInsideClass;
    public static final Map<String, Boolean> testsInsideClassMapping = MappingUtils
            .yesNoTrueFalseMapper();

    public static final String testSuiteClassNameDefault = "TestCase";
    public String testSuiteClassName;

    public static Map<TestStateType, String> testStateMapper() {
        Map<TestStateType, String> map = new HashMap<>();
        map.put(TestStateType.PASS, "assert True");
        map.put(TestStateType.FAIL, "assert False");
        return map;
    }

    public static Map<String, TestFramework> testFrameworkAbbrevToName() {
        Map<String, TestFramework> map = new HashMap<>();
        map.put(Definitions.ABBREV_PYTEST, TestFramework.PYTEST);
        map.put(Definitions.ABBREV_UNITTEST, TestFramework.UNITTEST);
        return map;
    }

    public PyWriterConfig() {
        this.logger = Logger.getAnonymousLogger();
        this.moduleNamePatternsToIgnore = moduleNamePatternsToIgnoreDefault;
        this.foldersToIgnore = foldersToIgnoreDefault;
        this.testModulePrefix = testModulePrefixDefault;
        this.importPackages = importPackagesDefault;
        this.testFunctionNamePrefix = testFunctionNamePrefixDefault;
        this.testDocstring = testDocstringDefault;
        this.testOutputDirName = testOutputDirNameDefault;
        this.testFramework = testFrameworkDefault;
        this.indentSize = indentSizeDefault;
        this.testState = testStateDefault;
        this.testStateStatement = testStateMapping.get(this.testState);

        this.testSuiteHaveSetUp = testSuiteHaveSetUpDefault;
        this.testSuiteHaveTearDown = testSuiteHaveTearDownDefault;
        this.testsInsideClass = testsInsideClassDefault;
        this.testSuiteClassName = testSuiteClassNameDefault;
    }

    /**
     * Add folders to ignore to the configuration.
     */
    public void loadFoldersToIgnore(String foldersToIgnore) {
        if (foldersToIgnore.isEmpty()) {
            this.foldersToIgnore = foldersToIgnoreDefault;
        } else {
            this.foldersToIgnore.addAll(Arrays.asList(foldersToIgnore.split(";", -1)));
        }
    }

    /**
     * Get path of modules to read and then apply filters.
     */
    public void loadSourceModules(String modulePathsInput) throws IOException {
        List<Path> paths = Collections.emptyList();
        ArrayList<Path> fileModulePaths = new ArrayList<>();

        for (String path : modulePathsInput.split(";")) {
            // handle both files and folders
            File object = new File(path);
            if (object.isFile()) {
                fileModulePaths.add(Paths.get(path));
            } else if (object.isDirectory()) {
                paths = Files.walk(Paths.get(path)).filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().endsWith(pythonModuleExtension))
                        .collect(Collectors.toList());
            }
        }
        fileModulePaths.addAll(paths);

        this.modulePaths = this.filterModulesToIgnore(fileModulePaths);
    }

    /**
     * Filter out modules
     * - located in directories that should be ignored.
     * - dunder modules
     * - matching ignore pattern in filename
     */
    public ArrayList<String> filterModulesToIgnore(List<Path> paths) {
        List<Path> pathsRequired = new ArrayList<>();
        for (Path path : paths) {
            if (this.foldersToIgnore.stream().noneMatch(path.getParent().toString()::contains)) {
                pathsRequired.add(path);
            }
        }

        ArrayList<String> pathsFiltered = new ArrayList<>();
        for (Path path : pathsRequired) {
            String fileName = FilesUtils
                    .getFilenameWithoutExt(new File(path.toString()).getName());
            if (isBusinessModule(fileName)) {
                if (this.moduleNamePatternsToIgnore.size() > 0) {
                    if (this.moduleNamePatternsToIgnore.stream().noneMatch(path.toString()::contains)) {
                        pathsFiltered.add(path.toString());
                    }
                } else {
                    pathsFiltered.add(path.toString());
                }
            }
        }
        return pathsFiltered;
    }

    /**
     * Add module name patterns to ignore.
     */
    public void loadModuleNamePatternsToIgnore(String moduleNamePatternsToIgnore) {
        if (!moduleNamePatternsToIgnore.isEmpty()) {
            this.moduleNamePatternsToIgnore.addAll(Arrays.asList(moduleNamePatternsToIgnore.split(";", -1)));
        }
    }

    /**
     * Where to put the test modules (beside the source or in the new folder)?
     */
    public void loadTestsOutputDir(String testOutputDir) {
        if (testOutputDir.isEmpty()) {
            this.testOutputDirName = PyWriterConfig.testOutputDirNameDefault;
        } else {
            this.testOutputDirName = testOutputDir;
        }
    }

    /**
     * Which test framework paradigm to use when generating tests
     */
    public void loadTestFrameworkName(String testFramework) {
        if (testFramework.isEmpty()) {
            this.testFramework = testFrameworkDefault;
        } else {
            this.testFramework = testFrameworkAbbrevToName().get(testFramework);
        }
    }

    /**
     * Get packages to be imported in each test module.
     */
    public void loadTestImportPackages(String importPackages) {
        if (!importPackages.isEmpty()) {
            this.importPackages = Arrays.asList(importPackages.split(";", -1));
            // remove duplicates preserving the order
            this.importPackages = MappingUtils.removeDuplicates(this.importPackages);
        } else {
            this.importPackages = importPackagesDefault;
        }

    }

    /**
     * Get indentation size to use in output test modules.
     */
    public void loadTestIndentation(String indentSize) {
        if (indentSize.isEmpty()) {
            this.indentSize = PyWriterConfig.indentSizeDefault;
        } else {
            try {
                this.indentSize = Integer.parseInt(indentSize);
            } catch (NumberFormatException e) {
                this.logger.info(MessageFormat.format(
                        "Invalid indentation size provided. " + "Will use the default, {0} spaces.",
                        PyWriterConfig.indentSizeDefault));
                this.indentSize = PyWriterConfig.indentSizeDefault;
            }
        }
    }

    /**
     * Get state of tests (should they fail or pass).
     */
    public void loadTestState(String testState) {
        if (testState.isEmpty() || testState.equals(ABBREV_TESTSTATE_PASS)) {
            this.testState = TestStateType.PASS;
            this.testStateStatement = PyWriterConfig.testStateMapping
                    .get(TestStateType.PASS);
        } else if (testState.equals(ABBREV_TESTSTATE_FAIL)) {
            this.testState = TestStateType.FAIL;
            this.testStateStatement = PyWriterConfig.testStateMapping
                    .get(TestStateType.FAIL);
        }
    }

    /**
     * Get test module name prefix.
     */
    public void loadTestModulePrefix(String testModulePrefix) {
        if (testModulePrefix.isEmpty()) {
            this.testModulePrefix = PyWriterConfig.testModulePrefixDefault;
        } else {
            this.testModulePrefix = testModulePrefix;
        }
    }

    /**
     * Get test function name prefix.
     */
    public void loadTestFunctionNamePrefix(String testNamePrefix) {
        if (testNamePrefix.isEmpty()) {
            this.testFunctionNamePrefix = PyWriterConfig.testFunctionNamePrefixDefault;
        } else {
            this.testFunctionNamePrefix = testNamePrefix;
        }
    }


    /**
     * Get test function docstring.
     */
    public void loadTestFunctionDocstringType(String testDocstring) {
        if (testDocstring.equals(ABBREV_DOCSTRINGS_SOURCE)) {
            this.testDocstring = DocstringType.SOURCE;
        } else if (testDocstring.equals(ABBREV_DOCSTRINGS_EMPTY)) {
            this.testDocstring = DocstringType.EMPTY;
        } else if (testDocstring.equals(ABBREV_DOCSTRINGS_NONE) || testDocstring.isEmpty()) {
            this.testDocstring = DocstringType.NONE;
        }
    }

    /**
     * Get whether tests should be inside a class.
     */
    public void loadTestsInsideClass(String testsInsideClass) {
        if (testsInsideClass.isEmpty()) {
            this.testsInsideClass = PyWriterConfig.testsInsideClassDefault;
        } else {
            this.testsInsideClass = PyWriterConfig.testsInsideClassMapping
                    .get(testsInsideClass);
        }
    }

    /**
     * Get test suite class name.
     */
    public void loadTestSuiteClassName(String testSuiteClassName) {
        if (testSuiteClassName.isEmpty()) {
            this.testSuiteClassName = PyWriterConfig.testSuiteClassNameDefault;
        } else {
            this.testSuiteClassName = testSuiteClassName;
        }
    }

    /**
     * Get whether setUp class method should be used.
     */
    public void loadTestSuiteHaveSetUp(String testSuiteHaveSetUp) {
        if (testSuiteHaveSetUp.isEmpty()) {
            this.testSuiteHaveSetUp = PyWriterConfig.testSuiteHaveSetUpDefault;
        } else {
            this.testSuiteHaveSetUp = PyWriterConfig.testSuiteHaveSetUpMapping
                    .get(testSuiteHaveSetUp);
        }
    }

    /**
     * Get whether tearDown class method should be used.
     */
    public void loadTestSuiteHaveTearDown(String testSuiteHaveTearDown) {
        if (testSuiteHaveTearDown.isEmpty()) {
            this.testSuiteHaveTearDown = PyWriterConfig.testSuiteHaveTearDownDefault;
        } else {
            this.testSuiteHaveTearDown = PyWriterConfig.testSuiteHaveTearDownMapping
                    .get(testSuiteHaveTearDown);
        }
    }
}
