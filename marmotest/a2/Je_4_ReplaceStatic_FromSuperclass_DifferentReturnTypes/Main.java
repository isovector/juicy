// JOOS1:HIERARCHY,DIFFERENT_RETURN_TYPE,NONSTATIC_REPLACE_STATIC
// JOOS2:HIERARCHY,DIFFERENT_RETURN_TYPE,NONSTATIC_REPLACE_STATIC
// JAVAC:UNKNOWN
// 
/**
 * Hierarchy check:
 * - An instance method must not replace a static method (8.4.6.1,
 * well-formedness constraint 5).  
*/
public class Main extends Thread{
    
    public Main(){}

    public static int test(){
	return 123;
    }

    public String activeCount(){
	return "0";
    }
}
