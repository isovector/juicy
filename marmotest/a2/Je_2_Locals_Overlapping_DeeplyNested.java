// JOOS1:ENVIRONMENTS,DUPLICATE_VARIABLE
// JOOS2:ENVIRONMENTS,DUPLICATE_VARIABLE
// JAVAC:UNKNOWN
// 
/**
 * Environments:
 * - Check that no two local variables with overlapping scope have the
 * same name.
 */
public class Je_2_Locals_Overlapping_DeeplyNested {
	public Je_2_Locals_Overlapping_DeeplyNested() {}
	
	public static int test() {
		int a = 123;
		boolean b = true;
		boolean c = true;
		boolean d = true;
		boolean e = true;
		boolean f = true;
  		if (b) if (c) if (d) if (e) if (f) { int a = 43; return a+80; }
		return a;
	}
}
