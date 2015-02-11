// JOOS1:HIERARCHY,DIFFERENT_RETURN_TYPE
// JOOS2:HIERARCHY,DIFFERENT_RETURN_TYPE
// JAVAC:UNKNOWN
// 
/**
 * Hierarchy check:
 * - A class or interface must not have (declare or inherit) two
 * methods with the same name and parameter types but different return
 * types (8.1.1.1, 8.4, 8.4.2, 8.4.6.3, 8.4.6.4, 9.2, 9.4.1). (Method
 * compareTo(Object) must return type int)
 */
public abstract class Main implements Comparable {
    
    public Main(){}

    public boolean compareTo(Object o){
	return true;
    }

    public static int test(){
	return 123;
    }
}
