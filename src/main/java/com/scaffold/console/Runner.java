package com.scaffold.console;

import com.scaffold.parser.SourceParser;
import com.scaffold.writer.PyWriter;
import com.scaffold.writer.PyWriterConfig;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Scanner;
import java.util.logging.Logger;

import static com.scaffold.console.Definitions.ABBREV_TEST_LOCATION_BESIDE_SOURCE;
import static com.scaffold.console.Definitions.ABBREV_TEST_LOCATION_NEW_FOLDER;

public class Runner {
    public final PyWriterConfig config;
    public final Scanner scanner;
    public final Logger logger;

    public Runner() {
        this.config = new PyWriterConfig();
        this.scanner = new Scanner(System.in);
        this.logger = Logger.getAnonymousLogger();
    }

    public static void main(String[] args) throws Exception {
        Runner runner = new Runner();
        System.out.println(UserPrompt.modules);
        String modulePathsInput = runner.scanner.nextLine();
        while (modulePathsInput.isEmpty()) {
            System.out.println(UserPrompt.foldersToProcessMissing);
            modulePathsInput = runner.scanner.nextLine();
        }

        System.out.println(UserPrompt.foldersToIgnore);
        String foldersToIgnore = runner.scanner.nextLine();
        runner.config.loadFoldersToIgnore(foldersToIgnore);

        System.out.println(UserPrompt.moduleNamePatternsToIgnore);
        String moduleNamePatternsToIgnore = runner.scanner.nextLine();
        runner.config.loadModuleNamePatternsToIgnore(moduleNamePatternsToIgnore);

        // loading of source modules after got patterns for folders and files to exclude
        runner.config.loadSourceModules(modulePathsInput);

        System.out.println(UserPrompt.testLocation);
        String testLocation = runner.scanner.nextLine();
        if (testLocation.equals(ABBREV_TEST_LOCATION_NEW_FOLDER) || testLocation.isEmpty()) {
            System.out.println(UserPrompt.testOutputDir);
            String testOutputDir = runner.scanner.nextLine();
            runner.config.loadTestsOutputDir(testOutputDir);
        } else if (testLocation.equals(ABBREV_TEST_LOCATION_BESIDE_SOURCE)) {
            runner.config.loadTestsOutputDir(".");
        }

        System.out.println(UserPrompt.testFramework);
        String testFramework = runner.scanner.nextLine();
        runner.config.loadTestFrameworkName(testFramework);

        System.out.println(UserPrompt.importPackages);
        String importPackages = runner.scanner.nextLine();
        runner.config.loadTestImportPackages(importPackages);

        System.out.println(UserPrompt.indentation);
        String indentSize = runner.scanner.nextLine();
        runner.config.loadTestIndentation(indentSize);

        System.out.println(UserPrompt.testState);
        String testState = runner.scanner.nextLine();
        runner.config.loadTestState(testState);

        System.out.println(UserPrompt.testModulePrefix);
        String testModulePrefix = runner.scanner.nextLine();
        runner.config.loadTestModulePrefix(testModulePrefix);

        System.out.println(UserPrompt.testNamePrefix);
        String testNamePrefix = runner.scanner.nextLine();
        runner.config.loadTestFunctionNamePrefix(testNamePrefix);

        System.out.println(UserPrompt.testDocstring);
        String testDocstring = runner.scanner.nextLine();
        runner.config.loadTestFunctionDocstringType(testDocstring);

        if (runner.config.testFramework != Definitions.TestFramework.UNITTEST) {
            System.out.println(UserPrompt.testsInsideClass);
            String testsInsideClass = runner.scanner.nextLine();
            runner.config.loadTestsInsideClass(testsInsideClass);
        }

        if (runner.config.testsInsideClass) {
            System.out.println(UserPrompt.testSuiteClassName);
            String testSuiteClassName = runner.scanner.nextLine();
            runner.config.loadTestSuiteClassName(testSuiteClassName);

            if (runner.config.testFramework == Definitions.TestFramework.UNITTEST) {
                System.out.println(UserPrompt.testSuiteHaveSetUp);
                String testSuiteHaveSetUp = runner.scanner.nextLine();
                runner.config.loadTestSuiteHaveSetUp(testSuiteHaveSetUp);

                System.out.println(UserPrompt.testSuiteHaveTearDown);
                String testSuiteHaveTearDown = runner.scanner.nextLine();
                runner.config.loadTestSuiteHaveTearDown(testSuiteHaveTearDown);
            }
        }

        runner.generateTests();
    }

    /**
     * Generate the tests code and save modules with this code on disk.
     */
    public void generateTests() throws Exception {
        this.logger.info("Starting!");

        for (String modulePath : this.config.modulePaths) {
            this.logger.info(MessageFormat.format("Reading the source code of {0}", modulePath));
            SourceParser parser = new SourceParser(modulePath);

            Path destDir = Paths.get(parser.pyModule.baseDir, this.config.testOutputDirName);
            if (!Files.exists(destDir)) {
                (new File(destDir.toString())).mkdirs();
            }
            try {
                parser.loadFunctions();
            } catch (Exception e) {
                this.logger.info(MessageFormat.format("No functions found in {0}",
                        parser.pyModule.nameWithExt));
                continue;
            }

            // skipping modules with only non-testable functions
            if (parser.pyModule.functions.size() == 0) {
                continue;
            }

            Path path = Paths.get(destDir.toString(),
                    this.config.testModulePrefix + parser.pyModule.nameWithExt);

            this.logger.info(MessageFormat.format("Processing the source code of {0}", modulePath));
            PyWriter writer = new PyWriter(this.config, parser.pyModule, path);
            writer.writeTestsModule();
        }

        this.logger.info("Complete!");
    }

}
