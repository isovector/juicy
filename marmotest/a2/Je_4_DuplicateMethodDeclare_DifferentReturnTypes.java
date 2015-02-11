// JOOS1:HIERARCHY,DUPLICATE_METHOD
// JOOS2:HIERARCHY,DUPLICATE_METHOD
// JAVAC:UNKNOWN
// 
/**
 * - Hierarchy check:
 * - A class or interface must not declare two methods with the same
 * name and parameter types (8.4, 9.4).  
 */
public class Je_4_DuplicateMethodDeclare_DifferentReturnTypes {

    public Je_4_DuplicateMethodDeclare_DifferentReturnTypes() {}
    
    public int foo(String s) {
        return 7;
    }
    
    public String foo(String s) {
        return "7";
    }
    
    public static int test() {
        return 123;
    }
}
