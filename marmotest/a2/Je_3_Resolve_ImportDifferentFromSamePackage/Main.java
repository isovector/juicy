// JOOS1:TYPE_LINKING,UNRESOLVED_TYPE
// JOOS2:TYPE_LINKING,UNRESOLVED_TYPE
// JAVAC:UNKNOWN
// 
/**
 * Typelinking:
 * -Check that all types actually resolve to defined types in the
 * program or the class library.
 */
import java.util.ArrayList;

public class Main {

    public Main() { }

    public static int test() {
	List l = null;
	return 123;
    }
}
