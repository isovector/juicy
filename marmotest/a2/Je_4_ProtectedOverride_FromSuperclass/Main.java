// JOOS1:HIERARCHY,PROTECTED_REPLACE_PUBLIC
// JOOS2:HIERARCHY,PROTECTED_REPLACE_PUBLIC
// JAVAC:UNKNOWN
// 
/**
 * Hierarchy check:
 * - A protected method must not override a public method (8.4.6.3,
 * well-formedness constraint 7).
 **/
public class Main extends Thread{

    public Main(){}

    protected void interrupt(){}

    public static int test(){
	return 123;
    }
}
