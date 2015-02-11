// JOOS1:HIERARCHY,DUPLICATE_METHOD
// JOOS2:HIERARCHY,DUPLICATE_METHOD
// JAVAC:UNKNOWN
// 
/**
 * Hierarchy:
 * - A class or interface must not declare two methods with the same
 * name and parameter types (8.4, 9.4, well-formedness constraint 2).
 */
public class Je_4_DuplicateMethodDeclare_NoArgs {

    public Je_4_DuplicateMethodDeclare_NoArgs() { }

    public static int test() { 
	return 123; 
    }

    public void foo() {}

    public void foo() {}
}
