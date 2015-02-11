// HIERARCHY
/*
A type must not inherit two different fields with the same name, but
may hide fields in odd ways.
*/

public class Main {

    public Main() {}

    public static int test() {
	foo f = new foo();
	return 123;
    }

}
