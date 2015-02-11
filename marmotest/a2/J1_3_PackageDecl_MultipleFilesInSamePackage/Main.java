// TYPE_LINKING
/**
 * TypeLinking:
 * - This testcase tests whether it is allowed to declare two
 * different classes in the same package.
 **/
public class Main{

    public Main(){}

    public static int test(){
	return test.A.foo();
    }

}
