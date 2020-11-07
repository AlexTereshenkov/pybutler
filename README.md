## Introduction

`pybutler` - a humble servant to Python developers - is a tool that can be used for generation of 
boilerplate code (scaffolding) required to run unit tests for a Python 3 project.
The test functions in the generated test modules will be named after found source module functions
so that one can immediately start writing unit tests.
Using this tool can be particularly useful when working with legacy 
Python projects that don't have any tests.

The workflow of the tool is relatively simple:

* get user configuration about how tests modules with test functions 
  should be generated (e.g. whether the test modules
  should be created alongside the source modules or in a new directory, 
  what testing framework to use, what packages should be imported in each test module,
  and so on)

* read and parse each source file in the input directory (recursively) 
  using the [ANTLR]((https://www.antlr.org/)) Python grammar file extracting 
  all standalone functions and class methods

* generate test module for each source module found (each of the generated test modules 
  will contain the test function for each source module function)

The motivation behind writing this helper tool was to be able 
to quickly generate the boilerplate code
for a Python project that doesn't have any tests.
This makes it possible to avoid copying function names 
from the sources into the test modules manually. 
Once the scaffolding is in place, one can fill the test functions with the required code
so that a lot of time can be saved.
 
## Build and test

Unit tests are written using the [JUnit4](https://junit.org/junit4/) framework 
and [JaCoCo](https://www.eclemma.org/jacoco/) library is used for generating HTML code coverage reports.

To avoid providing class paths to the `ANTLR`, other dependencies, and the `pybutler`'s compiled classes
when calling the interactive CLI tool, a Maven plugin 
[Appassembler](https://www.mojohaus.org/appassembler/appassembler-maven-plugin/usage-program.html) came in handy.
It can be used to generate a single `.jar` artifact which contains the application's code as well as all dependencies
artifacts:

```
$ mvn clean compile test assembly:single
```

The path to artifact can be simplified by using an alias if you have access to Bash:

```
$ alias pybutler="java -jar target/pybutler.jar"
$ pybutler
```

## Usage

The tool can be used in Windows and Linux environments (you can run `mvn test` in either OS
and tests should pass).
There isn't any platform specific functionality that is being used, 
so it should just work on either operating system (MacOS shouldn't be a problem either).

Given this source module, `simple.py`

```python
def function1(arg1, arg2):
    """Docstring of function1"""
    return 42

def function2(arg3, arg4):
    """Docstring of function2"""
    return 52
```

and this user configuration provided,

```
$ pybutler
Enter path to the modules, semicolon separated:
>
/mnt/c/Temp/simple.py
Enter folder names to ignore, semicolon separated. Default is [tests].
>
tests
Enter module name patterns to ignore.
>
_core
Where do you want to store output test modules?
        - New folder [n]: Default. Each test module will be written into a new folder.
        - With source [s]: Each test module will be created beside the source module hierarchically.
[n|s] >
n
What is the directory name where the test modules will be written to? Default is `tests`.
>
tests
What test framework do you want to use?
        - unittest [u]: Default. Each test function will be defined inside `unittest.TestCase` class.
        - pytest [p]: You can choose where to define test functions.
[u|p] >
u
Enter list of packages (semicolon separated) you want to add import statements for.
>
os;sys;re
Enter indentation size (number of spaces). Default is 2.
>
4
Do you want the tests to fail or to pass?
        - Pass [p]: Default. Each test will have a passing assert statement (`assert True`).
        - Fail [f]: Each test will have a failing assert statement (`assert False`).
[p|f] >
p
Enter the test module file prefix. Default is test_.
>
test_
What do you want to use for test function name prefix? Default is `test_`.
>
test_
What docstrings should test functions have?
        - Empty [e]: Test functions will have empty docstrings.
        - None [n]: Default. Test functions will have no docstrings.
[e|n] >
e
What do you want to use for test class name? Default is TestCase.
>
BaseTestCase
Do you want to have setUp method in the test suite class?
        - Yes [y]: Default. Each class will have a setUp method.
        - No [n]: Class won't have a setUp method.
[y|n] >
y
Do you want to have tearDown method in the test suite class?
        - Yes [y]: Default. Each class will have a tearDown method.
        - No [n]: Class won't have a tearDown method.
[y|n] >
y
```

the test module, `test_simple.py` with the following contents will be created:

```python
import os
import sys
import re
import unittest

class BaseTestCase(unittest.TestCase):

    def setUp(self):
        return

    def test_function1(self):
        """"""
        assert True
        return

    def test_function2(self):
        """"""
        assert True
        return

    def tearDown(self):
        return
```

## Resources and adopted code

A couple of resources on ANTLR project were helpful 
([1](https://github.com/teverett/antlr4example), [2](https://github.com/alexec/antlr-tutorial)).
I have adopted code from the [python3-parser](https://github.com/bkiers/python3-parser) project 
written by Bart Kiers as Python parser of this project.
