package com.scaffold.writer;

import com.scaffold.console.Definitions;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static com.scaffold.console.Definitions.*;
import static com.scaffold.writer.PyWriterConfig.*;
import static org.junit.Assert.*;

public class PyWriterConfigTest {

    @Test
    public void testLoadFoldersToIgnore() {
        PyWriterConfig config = new PyWriterConfig();
        config.loadFoldersToIgnore("pattern1;pattern2");
        assertTrue(config.foldersToIgnore.contains("pattern1"));
        assertTrue(config.foldersToIgnore.contains("pattern2"));

        config.loadFoldersToIgnore("");
        assertEquals(config.foldersToIgnore, PyWriterConfig.foldersToIgnoreDefault);
    }

    @Test
    public void testLoadSourceModules() throws Exception {
        PyWriterConfig config = new PyWriterConfig();
        final TemporaryFolder testFolder = new TemporaryFolder();
        testFolder.create();
        final String module = testFolder.newFile("module.py").toString();
        testFolder.newFolder("package");
        final String packageModule1 = testFolder.newFile("package/module1.py").toString();
        final String packageModule2 = testFolder.newFile("package/module2.py").toString();
        final String packageModuleNonPython = testFolder.newFile("package/module").toString();

        config.loadSourceModules(testFolder.getRoot().getAbsolutePath());
        assertTrue(config.modulePaths.containsAll(Arrays.asList(packageModule1, packageModule2, module)));
        assertFalse(config.modulePaths.contains(packageModuleNonPython));

        config.loadSourceModules(MessageFormat.format("{0};{1}", testFolder.getRoot().getAbsolutePath(), module));
        assertTrue(config.modulePaths.containsAll(Arrays.asList(packageModule1, packageModule2, module)));
        assertFalse(config.modulePaths.contains(packageModuleNonPython));

    }


    @Test
    public void testFilterModulesToIgnore() throws Exception {
        PyWriterConfig config = new PyWriterConfig();

        config.foldersToIgnore = new ArrayList<>(Collections.singletonList("tests"));
        final TemporaryFolder testFolder = new TemporaryFolder();
        testFolder.create();
        final Path module = testFolder.newFile("module.py").toPath();
        testFolder.newFolder("tests");
        testFolder.newFolder("tests/valid");
        testFolder.newFolder("valid");
        final Path packageModule1 = testFolder.newFile("tests/module1.py").toPath();
        final Path packageModule2 = testFolder.newFile("tests/module2.py").toPath();

        // checking that folders with valid names that have parents that should be excluded are removed too
        final Path packageModule11 = testFolder.newFile("tests/valid/module1.py").toPath();
        final Path packageModule22 = testFolder.newFile("tests/valid/module2.py").toPath();

        // module names matching patterns must be excluded
        config.moduleNamePatternsToIgnore = new ArrayList<>(Arrays.asList("badPattern1", "badPattern2"));
        final Path packageModule3 = testFolder.newFile("valid/module3_badPattern1.py").toPath();
        final Path packageModule4 = testFolder.newFile("valid/module4_badPattern1.py").toPath();

        ArrayList<String> filteredPaths = config.filterModulesToIgnore(Arrays.asList(module, packageModule1,
                packageModule2, packageModule11, packageModule22, packageModule3, packageModule4));
        assertEquals(filteredPaths, Collections.singletonList(module.toString()));

        testFolder.newFolder("invalid");
        final Path packageModule5 = testFolder.newFile("invalid/module5.py").toPath();
        final Path packageModule6 = testFolder.newFile("invalid/module6.py").toPath();

        config.foldersToIgnore = new ArrayList<>(Arrays.asList("tests", "invalid"));

        ArrayList<String> filteredPathsMultipleDirsToExclude = config.filterModulesToIgnore(Arrays.asList(module,
                packageModule1, packageModule2, packageModule11, packageModule22, packageModule3, packageModule4,
                packageModule5, packageModule6));
        assertEquals(filteredPathsMultipleDirsToExclude, Collections.singletonList(module.toString()));
    }

    @Test
    public void testLoadModuleNamePatternsToIgnore() {
        PyWriterConfig config = new PyWriterConfig();
        config.loadModuleNamePatternsToIgnore("pattern1;pattern2");
        assertTrue(config.moduleNamePatternsToIgnore.containsAll(Arrays.asList("pattern1", "pattern2")));
    }

    @Test
    public void testLoadTestsOutputDir() {
        PyWriterConfig config = new PyWriterConfig();
        config.loadTestsOutputDir("");
        assertEquals(config.testOutputDirName, PyWriterConfig.testOutputDirNameDefault);
        config.loadTestsOutputDir("output_tests");
        assertEquals(config.testOutputDirName, "output_tests");
    }

    @Test
    public void testLoadTestFrameworkName() {
        PyWriterConfig config = new PyWriterConfig();
        config.loadTestFrameworkName(Definitions.ABBREV_PYTEST);
        assertEquals(config.testFramework, TestFramework.PYTEST);

        config.loadTestFrameworkName(Definitions.ABBREV_UNITTEST);
        assertEquals(config.testFramework, TestFramework.UNITTEST);

        config.loadTestFrameworkName("");
        assertEquals(config.testFramework, TestFramework.UNITTEST);
    }

    @Test
    public void testLoadTestImportPackages() {
        PyWriterConfig config = new PyWriterConfig();
        config.loadTestImportPackages("os;sys;re;os;sys;io");
        assertEquals(config.importPackages, Arrays.asList("os", "sys", "re", "io"));

        PyWriterConfig configNoImportPackages = new PyWriterConfig();
        configNoImportPackages.loadTestImportPackages("");
        assertEquals(configNoImportPackages.importPackages, Collections.emptyList());
    }

    @Test
    public void testLoadTestIndentation() {
        PyWriterConfig config = new PyWriterConfig();
        config.loadTestIndentation("2");
        assertEquals(config.indentSize, 2);

        PyWriterConfig configDefaultIndent = new PyWriterConfig();
        configDefaultIndent.loadTestIndentation("");
        assertEquals(configDefaultIndent.indentSize, PyWriterConfig.indentSizeDefault);

        PyWriterConfig configParseFail = new PyWriterConfig();
        configParseFail.loadTestIndentation("foo");
        assertEquals(configParseFail.indentSize, PyWriterConfig.indentSizeDefault);
    }

    @Test
    public void testLoadTestState() {
        PyWriterConfig configFail = new PyWriterConfig();
        configFail.loadTestState(ABBREV_TESTSTATE_FAIL);
        assertEquals(configFail.testState, TestStateType.FAIL);
        assertEquals(configFail.testStateStatement, PyWriterConfig.testStateMapping
                .get(TestStateType.FAIL));

        PyWriterConfig configPass = new PyWriterConfig();
        configPass.loadTestState(ABBREV_TESTSTATE_PASS);
        assertEquals(configPass.testState, TestStateType.PASS);
        assertEquals(configPass.testStateStatement, PyWriterConfig.testStateMapping
                .get(TestStateType.PASS));

        PyWriterConfig configPassEmpty = new PyWriterConfig();
        configPassEmpty.loadTestState("");
        assertEquals(configPassEmpty.testState, TestStateType.PASS);
        assertEquals(configPassEmpty.testStateStatement, PyWriterConfig.testStateMapping
                .get(TestStateType.PASS));
    }

    @Test
    public void testLoadTestModulePrefix() {
        PyWriterConfig config = new PyWriterConfig();
        config.loadTestModulePrefix("testing_");
        assertEquals(config.testModulePrefix, "testing_");

        PyWriterConfig configEmpty = new PyWriterConfig();
        configEmpty.loadTestModulePrefix("");
        assertEquals(configEmpty.testModulePrefix, testModulePrefixDefault);
    }

    @Test
    public void testLoadTestFunctionNamePrefix() {
        PyWriterConfig config = new PyWriterConfig();
        config.loadTestFunctionNamePrefix("testfunc_");
        assertEquals(config.testFunctionNamePrefix, "testfunc_");

        PyWriterConfig configEmpty = new PyWriterConfig();
        configEmpty.loadTestFunctionNamePrefix("");
        assertEquals(configEmpty.testModulePrefix, testModulePrefixDefault);
    }

    @Test
    public void testLoadTestFunctionDocstrings() {
        PyWriterConfig config = new PyWriterConfig();
        config.loadTestFunctionDocstringType(ABBREV_DOCSTRINGS_SOURCE);
        assertEquals(config.testDocstring, DocstringType.SOURCE);
        config.loadTestFunctionDocstringType(ABBREV_DOCSTRINGS_NONE);
        assertEquals(config.testDocstring, DocstringType.NONE);
        config.loadTestFunctionDocstringType(ABBREV_DOCSTRINGS_EMPTY);
        assertEquals(config.testDocstring, DocstringType.EMPTY);
        config.loadTestFunctionDocstringType("");
        assertEquals(config.testDocstring, DocstringType.NONE);
    }

    @Test
    public void testLoadTestsInsideClass() {
        PyWriterConfig config = new PyWriterConfig();
        config.loadTestsInsideClass(ABBREV_YES);
        assertEquals(config.testsInsideClass, PyWriterConfig.testsInsideClassMapping
                .get(ABBREV_YES));

        config.loadTestsInsideClass(ABBREV_NO);
        assertEquals(config.testsInsideClass, PyWriterConfig.testsInsideClassMapping
                .get(ABBREV_NO));

        config.loadTestsInsideClass("");
        assertEquals(config.testsInsideClass, testsInsideClassDefault);
    }

    @Test
    public void testLoadTestSuiteClassName() {
        PyWriterConfig config = new PyWriterConfig();
        config.loadTestSuiteClassName("");
        assertEquals(config.testSuiteClassName, testSuiteClassNameDefault);
        config.loadTestSuiteClassName("ClassName");
        assertEquals(config.testSuiteClassName, "ClassName");
    }

    @Test
    public void testLoadTestSuiteHaveSetUp() {
        PyWriterConfig config = new PyWriterConfig();
        config.loadTestSuiteHaveSetUp("");
        assertEquals(config.testSuiteHaveSetUp, testSuiteHaveSetUpDefault);
        config.loadTestSuiteHaveSetUp(ABBREV_YES);
        assertEquals(config.testSuiteHaveSetUp, PyWriterConfig.testSuiteHaveSetUpMapping
                .get(ABBREV_YES));
        config.loadTestSuiteHaveSetUp(ABBREV_NO);
        assertEquals(config.testSuiteHaveSetUp, PyWriterConfig.testSuiteHaveSetUpMapping
                .get(ABBREV_NO));
    }

    @Test
    public void testLoadTestSuiteHaveTearDown() {
        PyWriterConfig config = new PyWriterConfig();
        config.loadTestSuiteHaveTearDown("");
        assertEquals(config.testSuiteHaveTearDown, testSuiteHaveTearDownDefault);
        config.loadTestSuiteHaveTearDown(ABBREV_YES);
        assertEquals(config.testSuiteHaveTearDown, PyWriterConfig.testSuiteHaveTearDownMapping
                .get(ABBREV_YES));
        config.loadTestSuiteHaveTearDown(ABBREV_NO);
        assertEquals(config.testSuiteHaveTearDown, PyWriterConfig.testSuiteHaveTearDownMapping
                .get(ABBREV_NO));
    }


}
