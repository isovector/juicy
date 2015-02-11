// JOOS1:HIERARCHY,PROTECTED_REPLACE_PUBLIC
// JOOS2:HIERARCHY,PROTECTED_REPLACE_PUBLIC
// JAVAC:UNKNOWN
// 
/**
 * Hierarchy:
 * - A protected method must not override a public method (8.4.6.3,
 * well-formedness constraint 7).
 */
public abstract class Main extends Foo implements Comparable{

    public Main(){}

    public static int test(){
	return 123;
    }

}
