// JOOS1:TYPE_LINKING,NON_EXISTING_PACKAGE
// JOOS2:TYPE_LINKING,NON_EXISTING_PACKAGE
// JAVAC:UNKNOWN
// 
/* TypeLinking:
 * Check that all import-on-demand declarations refer to existing packages.
 * 
 * java.util.foo does not exist, but java.util does.
 */

import java.util.foo.*;

public class Main {
    public Main() {}
    
    public static int test() {
	return 123;
    }
}
