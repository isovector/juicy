// HIERARCHY
/*
 * The abstract class A declares the abstract method m()
 * B extends A and implements A
 */
public class Main {
    public Main() {}
    public static int test() {
	A a = new B();
	return a.m();
    }
}
