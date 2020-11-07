package com.scaffold.console;

import com.scaffold.writer.PyWriterConfig;

import java.text.MessageFormat;

import static com.scaffold.console.Definitions.*;

public class UserPrompt {

    public static final String modules = "Enter path to the modules, semicolon separated:\n> ";

    public static final String foldersToIgnore = MessageFormat.format(
            "Enter folder names to ignore, semicolon separated. Default is {0}.\n>",
            PyWriterConfig.foldersToIgnoreDefault);

    public static final String foldersToProcessMissing = "No path is provided. Try again:\n> ";

    public static final String moduleNamePatternsToIgnore = "Enter module name patterns to ignore.\n> ";

    public static final String testLocation = MessageFormat.format(
            "Where do you want to store output test modules?\n"
                    + "\t- New folder [{0}]: Default. Each test module will be written into a new folder.\n"
                    + "\t- With source [{1}]: Each test module will be created beside the source module hierarchically.\n"
                    + "[{0}|{1}] > ", ABBREV_TEST_LOCATION_NEW_FOLDER, ABBREV_TEST_LOCATION_BESIDE_SOURCE);

    public static final String testOutputDir = MessageFormat.format(
            "What is the directory name where the test modules will be written to? Default is `{0}`.\n> ",
            PyWriterConfig.testOutputDirNameDefault);

    public static final String testFramework = MessageFormat.format(
            "What test framework do you want to use?\n"
                    + "\t- unittest [{0}]: Default. Each test function will be defined inside `unittest.TestCase` class.\n"
                    + "\t- pytest [{1}]: You can choose where to define test functions.\n"
                    + "[{0}|{1}] >", ABBREV_UNITTEST, ABBREV_PYTEST);

    public static final String testModulePrefix = MessageFormat.format(
            "Enter the test module file prefix. Default is {0}. \n>",
            PyWriterConfig.testModulePrefixDefault);

    public static final String importPackages = "Enter list of packages (semicolon separated) "
            + "you want to add `import` statement for.\n> ";

    public static final String indentation = MessageFormat.format(
            "Enter indentation size (number of spaces). Default is {0}.\n> ",
            PyWriterConfig.indentSizeDefault);

    public static final String testState = MessageFormat.format(
            "Do you want the tests to fail or to pass?\n"
                    + "\t- Pass [{0}]: Default. Each test will have a passing assert statement (`assert True`).\n"
                    + "\t- Fail [{1}]: Each test will have a failing assert statement (`assert False`).\n"
                    + "[{0}|{1}] > ", ABBREV_TESTSTATE_PASS, ABBREV_TESTSTATE_FAIL);

    public static final String testsInsideClass = MessageFormat.format(
            "Do you want to put the tests inside a class?\n"
                    + "\t- Yes [{0}]: Default. Test functions will be put inside a class as class methods.\n"
                    + "\t- No [{1}]: Test functions will be put in the module scope.\n"
                    + "[{0}|{1}] > ", ABBREV_YES, ABBREV_NO);

    public static final String testNamePrefix = MessageFormat.format(
            "What do you want to use for test function name prefix? Default is `{0}`.\n>",
            PyWriterConfig.testFunctionNamePrefixDefault);

    public static final String testDocstring = MessageFormat.format(
            "What docstrings should test functions have?\n"
                    + "\t- Empty [{0}]: Test functions will have empty docstrings.\n"
                    + "\t- None [{1}]: Default. Test functions will have no docstrings.\n"
                    + "[{0}|{1}] > ", ABBREV_DOCSTRINGS_EMPTY, ABBREV_DOCSTRINGS_NONE);

    public static final String testSuiteClassName = MessageFormat.format(
            "What do you want to use for test suite class name? Default is {0}.\n>",
            PyWriterConfig.testSuiteClassNameDefault);

    public static final String testSuiteHaveSetUp = MessageFormat.format(
            "Do you want to have setUp method in the test suite class?\n"
                    + "\t- Yes [{0}]: Default. Each class will have a setUp method.\n"
                    + "\t- No [{1}]: Class won't have a setUp method.\n"
                    + "[{0}|{1}] > ", ABBREV_YES, ABBREV_NO);

    public static final String testSuiteHaveTearDown = MessageFormat.format(
            "Do you want to have tearDown method in the test suite class?\n"
                    + "\t- Yes [{0}]: Default. Each class will have a tearDown method.\n"
                    + "\t- No [{1}]: Class won't have a tearDown method.\n"
                    + "[{0}|{1}] > ", ABBREV_YES, ABBREV_NO);
}
