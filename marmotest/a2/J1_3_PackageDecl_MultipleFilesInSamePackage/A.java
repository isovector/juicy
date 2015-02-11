package test;

public class A{

    public A(){}

    public static int foo(){
	return test.B.foo();
    }
}
