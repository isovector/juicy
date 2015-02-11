// HIERARCHY
/**
 * Hierarchy:
 * - A class or interface must not declare two methods with the same
 * name and parameter types (8.4, 9.4, well-formedness constraint 2).
 */
public class J1_4_MethodDeclare_DuplicateArrayTypes {
    public J1_4_MethodDeclare_DuplicateArrayTypes() {}
    
    public void method(Object[] a) {}
    public void method(int[] a) {}
    
    public static int test() {
	return 123;
    }
}
