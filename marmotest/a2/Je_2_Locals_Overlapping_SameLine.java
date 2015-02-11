// JOOS1:ENVIRONMENTS,DUPLICATE_VARIABLE
// JOOS2:ENVIRONMENTS,DUPLICATE_VARIABLE
// JAVAC:UNKNOWN
// 
/**
 * Environments:
 * - Two different locals with the same name may not have overlapping
 * scopes.
 */
public class Je_2_Locals_Overlapping_SameLine{

    public Je_2_Locals_Overlapping_SameLine(){}

    public static int test(){
	int a = 42;
	int b = 12; {int b = 123; a = a + b;} 
	return a - b - 30 ;
    }

}
