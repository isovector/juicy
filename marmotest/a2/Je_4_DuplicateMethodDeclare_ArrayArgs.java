// JOOS1:HIERARCHY,DUPLICATE_METHOD
// JOOS2:HIERARCHY,DUPLICATE_METHOD
// JAVAC:UNKNOWN
// 
/**
 * Hierarchy:
 * - A class or interface must not declare two methods with the same
 * name and parameter types (8.4, 9.4, well-formedness constraint 2).
 */
public class Je_4_DuplicateMethodDeclare_ArrayArgs {
    public Je_4_DuplicateMethodDeclare_ArrayArgs() {}
    
    public void method(String[] a) {}
    public void method(String[] b) {}
    
    public static int test() {
	return 123;
    }
}
