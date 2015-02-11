// TYPE_LINKING
/**
 * TypeLinking:
 * - Tests whether it is allowed to use a package name that clashes
 * with the identifier of a singletype import.
 */ 
 
public class Main{

    public Main() {}

    public static int test() {
    	List.A j = new List.A();
	return 123;
    }

}
