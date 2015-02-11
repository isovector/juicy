// HIERARCHY
/**
 * Hierarchy:
 * - A class may not declare two methods with the same signature, but
 * it may declare a method with the same name and parameter types as a
 * constructor.
 */
public class J1_4_DuplicateMethodDeclare_MethodNameEqualsConstructorName{

    public J1_4_DuplicateMethodDeclare_MethodNameEqualsConstructorName(){}
    public J1_4_DuplicateMethodDeclare_MethodNameEqualsConstructorName(int a){}

    public int J1_4_DuplicateMethodDeclare_MethodNameEqualsConstructorName(int a){
	return a;
    }

    public static int test(){
	return new J1_4_DuplicateMethodDeclare_MethodNameEqualsConstructorName(0).J1_4_DuplicateMethodDeclare_MethodNameEqualsConstructorName(123);
    }
}

