//JOOS1:TYPE_LINKING,NON_EXISTING_PACKAGE
//JOOS2:TYPE_LINKING,NON_EXISTING_PACKAGE
//JAVAC:UNKNOWN
/**
 * The package fo does not exist though it's a prefix of the package foo.bar from class foo.bar.Baz.
 */
import fo.*;

public class Main {
	public Main() {}
	
	public static int test() {
		return 123;
	}
}
