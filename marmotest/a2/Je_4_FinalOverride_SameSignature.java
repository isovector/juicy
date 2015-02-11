// JOOS1:HIERARCHY,REPLACE_FINAL
// JOOS2:HIERARCHY,REPLACE_FINAL
// JAVAC:UNKNOWN
// 
/**  
 * Hierarchy:
 * - A method must not override a final method (8.4.3.3,
 * well-formedness constraint 9).  
 */
public class Je_4_FinalOverride_SameSignature {

    public Je_4_FinalOverride_SameSignature(){}

    public Class getClass(){
	return new Object().getClass();
    }

    public static int test(){
	return 123;
    }

}
