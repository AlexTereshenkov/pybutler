import os
import sys
import re
import shutil
import unittest

class CustomTestSuite(unittest.TestCase):

    def setUp(self):
        return

    def unit_function1(self):
        """"""
        assert 1 == 1
        return

    def unit_function2(self):
        """"""
        assert 1 == 1
        return

    def tearDown(self):
        return
