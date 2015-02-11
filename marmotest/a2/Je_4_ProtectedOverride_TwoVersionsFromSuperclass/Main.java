// JOOS1:HIERARCHY,PROTECTED_REPLACE_PUBLIC
// JOOS2:HIERARCHY,PROTECTED_REPLACE_PUBLIC
// JAVAC:UNKNOWN
// 
/**
 * Hierarchy:
 * - A protected method must not override a public method (8.4.6.3,
 * well-formedness constraint 7).
 */
public class Main extends C{

    public Main() {}

    protected void remove() {
	System.out.println("not so good... actually wrong");
    }

    public static int test() {
	return 123;
    }

}
