// TYPE_LINKING
/**
 * TypeLinking:
 * - The name of a class must not clash with the name of a singletype
 * import, but a class may import itself.
 */
public class Main{

    public Main(){}

    public static int test(){
	return Test.Foo.test();
    }

}
