// JOOS1:HIERARCHY,DIFFERENT_RETURN_TYPE
// JOOS2:HIERARCHY,DIFFERENT_RETURN_TYPE
// JAVAC:UNKNOWN
// 
public class Main extends A {
    public Main() {}
    
    /* HierarchyCheck => return type incompatible with A.test() (JLS 8.4.6.3) */ 
    public static int test() {
	return 123;
    }
}
