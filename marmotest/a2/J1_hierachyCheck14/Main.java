// HIERARCHY
/*
A type must not inherit two fields with the same name 
(extra Joos restriction) - but may hide...
*/

public class Main {

    public Main() {}

    public static int test() {
	foo f = new foo();
	return 123;
    }

}
