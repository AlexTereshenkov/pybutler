package com.scaffold.console;

public class Definitions {
    public static final String ABBREV_PYTEST = "p";
    public static final String ABBREV_UNITTEST = "u";

    public static final String ABBREV_TESTSTATE_FAIL = "f";
    public static final String ABBREV_TESTSTATE_PASS = "p";

    public static final String ABBREV_DOCSTRINGS_SOURCE = "s";
    public static final String ABBREV_DOCSTRINGS_EMPTY = "e";
    public static final String ABBREV_DOCSTRINGS_NONE = "n";

    public static final String ABBREV_YES = "y";
    public static final String ABBREV_NO = "n";

    public static final String ABBREV_TEST_LOCATION_NEW_FOLDER = "n";
    public static final String ABBREV_TEST_LOCATION_BESIDE_SOURCE = "s";

    public static final String UNITTEST_NAME = "unittest";
    public static final String PYTEST_NAME = "pytest";

    public enum TestFramework {
        UNITTEST, PYTEST
    }

    public enum DocstringType {
        SOURCE, EMPTY, NONE
    }

    public enum TestStateType {
        PASS, FAIL
    }
}
