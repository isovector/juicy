// JOOS1:HIERARCHY,REPLACE_FINAL,DIFFERENT_RETURN_TYPE
// JOOS2:HIERARCHY,REPLACE_FINAL,DIFFERENT_RETURN_TYPE
// JAVAC:UNKNOWN
// 
/**  
 * Hierarchy:
 * - A method must not override a final method (8.4.3.3,
 * well-formedness constraint 9).  
 */
public class Je_4_FinalOverride_DifferentReturnTypes {

    public Je_4_FinalOverride_DifferentReturnTypes(){}

    public Object getClass(){
	return new Object();
    }

    public static int test(){
	return 123;
    }

}
