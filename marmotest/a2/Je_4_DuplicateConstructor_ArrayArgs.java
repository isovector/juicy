// JOOS1:HIERARCHY,DUPLICATE_CONSTRUCTOR
// JOOS2:HIERARCHY,DUPLICATE_CONSTRUCTOR
// JAVAC:UNKNOWN
// 
/**
 * Hierarchy:
 * - A class must not declare two constructors with the same parameter
 * types (8.8.2, simple constraint 5).
 */
public class Je_4_DuplicateConstructor_ArrayArgs {
    public Je_4_DuplicateConstructor_ArrayArgs(String[] a) {}
    public Je_4_DuplicateConstructor_ArrayArgs(String[] b) {}
    
    public static int test() {
	return 123;
    }
}
