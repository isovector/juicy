//JOOS1:TYPE_LINKING,NON_EXISTING_PACKAGE
//JOOS2:TYPE_LINKING,NON_EXISTING_PACKAGE
//JAVAC:UNKNOWN
/**
 * The package jav does not exist though it's a prefix of the package java from for instance java.lang.Object.
 */
import jav.*;

public class Main {
	public Main() {}
	
	public static int test() {
		return 123;
	}
}
