// JOOS1:ENVIRONMENTS,DUPLICATE_VARIABLE
// JOOS2:ENVIRONMENTS,DUPLICATE_VARIABLE
// JAVAC:UNKNOWN
// 
/**
 * Environments:
 * - Check that no two local variables with overlapping scope have the
 * same name.
 */
public class Je_2_Locals_Overlapping_ForInitializer {

    public Je_2_Locals_Overlapping_ForInitializer() {}

    public static int test() {
	int r = 0;
	for (int r = 0; r < 42; r = r + 1) {
	    r = r + 1;
	}
	return 123;
    }
}
