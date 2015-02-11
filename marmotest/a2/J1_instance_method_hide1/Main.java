// HIERARCHY
/* A static method must not hide an instance method,
 * but a new method with the same name may be defined. */

public class Main extends foo{

    public Main(){}

    public static int bar(String s){
	return 123;
    }

    public static int test(){

	return Main.bar("Hello World!");
    }

}
