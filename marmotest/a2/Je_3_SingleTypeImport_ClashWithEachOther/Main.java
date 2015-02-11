// JOOS1:TYPE_LINKING,TWO_SINGLE_TYPE_IMPORTS_CLASH
// JOOS2:TYPE_LINKING,TWO_SINGLE_TYPE_IMPORTS_CLASH
// JAVAC:UNKNOWN
// 
/**
 * Typelinking:
 * - Check that no two single-type-import declarations clash with each
 * other.
 */
import java.util.List;
import java.awt.List;

public class Main {

    public Main () {}

    public static int test() {
        return 123;
    }

}
