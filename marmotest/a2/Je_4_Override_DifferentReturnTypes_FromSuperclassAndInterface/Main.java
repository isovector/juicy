// JOOS1:HIERARCHY,DIFFERENT_RETURN_TYPE
// JOOS2:HIERARCHY,DIFFERENT_RETURN_TYPE
// JAVAC:UNKNOWN
// 
/**
 * Hierarchy:
 * - A class or interface must not have (declare or inherit) two
 * methods with the same name and parameter types but different return
 * types (8.1.1.1, 8.4, 8.4.2, 8.4.6.3, 8.4.6.4, 9.2, 9.4.1). (Method
 * run() must return type void, but inherited method run() returns int)
 */
public abstract class Main extends Foo implements Runnable {
    
    public Main(){}

    public static int test(){
	return 123;
    }
}
