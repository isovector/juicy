// HIERARCHY
/* An instance method must not override a static method, 
 * but a new method with the same name may be defined. */

public class Main extends foo{

    public Main(){}

    public int bar(String s){
	return 123;
    }

    public static int test(){

	Main m = new Main();
	return m.bar("Hello World!");
    }

}
