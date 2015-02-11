// HIERARCHY
/* A protected method must not override a public method,
 * but a new method with the same name may be defined. */

public class Main extends foo{

    public Main(){}

    protected int bar(String s){
	return 123;
    }

    public static int test(){

	Main m = new Main();
	return m.bar("Hello World!");
    }

}
