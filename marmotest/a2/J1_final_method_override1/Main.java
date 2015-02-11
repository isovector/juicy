// HIERARCHY
/* A final method may not be overridden or hidden,
 * but another method with the same name may be 
 * defined. */

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
