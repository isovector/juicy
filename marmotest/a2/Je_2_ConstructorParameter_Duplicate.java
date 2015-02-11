// JOOS1:ENVIRONMENTS,DUPLICATE_VARIABLE
// JOOS2:ENVIRONMENTS,DUPLICATE_VARIABLE
// JAVAC:UNKNOWN
// 
/**
 * Environments:
 * - Check that no two local variables with overlapping scope have the
 * same name.
 */
public class Je_2_ConstructorParameter_Duplicate {

    public Je_2_ConstructorParameter_Duplicate (String f, int f) {}

    public static int test() {
        return 123;
    }

}
