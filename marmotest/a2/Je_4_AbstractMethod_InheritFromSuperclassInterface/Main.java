// JOOS1:HIERARCHY,CLASS_MUST_BE_ABSTRACT
// JOOS2:HIERARCHY,CLASS_MUST_BE_ABSTRACT
// JAVAC:UNKNOWN
// 
/**
 * Hierarchy check:
 * - A class that has (declares or inherits) any abstract methods must
 * be abstract (8.1.1.1). (Method run() from Runnable not
 * implemented) 
 */
public class Main extends Foo{
    
    public Main(){}

    public static int test(){
	return 123;
    }
}
