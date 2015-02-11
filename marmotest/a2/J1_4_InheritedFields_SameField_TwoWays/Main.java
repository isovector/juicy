//HIERARCHY

/**
 * Both Main and Foo implement org.omg.CORBA.ARG_IN and thus inherited
 * the same .value field.
 */
public class Main extends Foo implements org.omg.CORBA.ARG_IN {
	public Main() {}
	
	public static int test() {
		return 123;
	}
}
	
