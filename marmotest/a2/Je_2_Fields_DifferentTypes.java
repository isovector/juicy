// JOOS1:ENVIRONMENTS,DUPLICATE_FIELD
// JOOS2:ENVIRONMENTS,DUPLICATE_FIELD
// JAVAC:UNKNOWN
// 
/**
 * - Environments
 * Check that no two fields in the same class have the same name.
 */
public class Je_2_Fields_DifferentTypes {

    public Object foo;

    public String foo;

    public Je_2_Fields_DifferentTypes () {}

    public static int test() {
        return 123;
    }

}
