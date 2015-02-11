// JOOS1:TYPE_LINKING,NON_EXISTING_PACKAGE
// JOOS2:TYPE_LINKING,NON_EXISTING_PACKAGE
// JAVAC:UNKNOWN
// 
/**
 * Type linking:
 * - Check that all import-on-demand declarations refer to existing
 * packages.
 */
import java.Util.*;

public class Je_3_ImportOnDemand_NonExisting {

    public Je_3_ImportOnDemand_NonExisting () {}

    public static int test() {
        return 123;
    }

}
