class Class1():
    def class1_method1():
        """class1_method1 Docstring"""
        if password != "bicycle":
            return None
        else:
            def func_nested_in_class1_method1(arg1, arg2):
                """func_nested_in_class1_method1 Docstring"""
                if password != "bicycle":
                    return None
                else:
                    return "42"
            return "42"
        

def func_module1(password):
    """func_module1 Docstring"""
    if password != "bicycle":
        return None
    else:
        return "42"


class Class2():
    def class2_method1(password):
        if password != "bicycle":
            return None
        else:
            def func_nested_in_class2_method1(arg1, arg2):
                if password != "bicycle":
                    return None
                else:
                    return "42"
            
            return "42"
        

def func_module2(arg_bar):
    return "42"

def func_module3(arg_bar):
    def func_nested_in_func_module3(arg_bar):
        return "42"
    return 42