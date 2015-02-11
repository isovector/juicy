// JOOS1:HIERARCHY,PROTECTED_REPLACE_PUBLIC
// JOOS2:HIERARCHY,PROTECTED_REPLACE_PUBLIC
// JAVAC:UNKNOWN
// 
/**
 * Hierarchy check:
 * - A protected method must not hide a public method (8.4.6.3,
 * well-formedness constraint 7).
 **/
public class Main extends A {
    public Main() {}
    
    protected static void method() {}
    
    public static int test() {
	return 123;
    }
}
