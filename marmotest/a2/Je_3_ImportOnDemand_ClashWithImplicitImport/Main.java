// JOOS1:TYPE_LINKING,AMBIGUOUS_CLASS_NAME
// JOOS2:TYPE_LINKING,AMBIGUOUS_CLASS_NAME
// JAVAC:UNKNOWN
// 
/**
 * Typelinking:
 * - Unqualified names are handled by these rules: 
 *   1. try the enclosing class or interface 
 *   2. try any single-type-import (A.B.C.D) 
 *   3. try the same package 
 *   4. try any import-on-demand package (A.B.C.*), including java.lang.* 
 *
 * This testcase tests item 4.
 */
import bar.*;

public class Main {
    
    public Main() {}

    public static int test() {
	return new Integer(123).intValue();
    }

}

