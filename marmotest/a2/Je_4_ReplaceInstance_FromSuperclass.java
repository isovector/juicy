// JOOS1:HIERARCHY,STATIC_REPLACE_NONSTATIC
// JOOS2:HIERARCHY,STATIC_REPLACE_NONSTATIC
// JAVAC:UNKNOWN
// 
/**
 * Hierarchy: 
 * - A static method must not replace an instance method * (8.4.6.2,
 * well-formedness constraint 5).
 */
public class Je_4_ReplaceInstance_FromSuperclass{

    public Je_4_ReplaceInstance_FromSuperclass(){}

    public static int test(){
	return 123;
    }

    public static boolean equals(Object o){
	return true;
    }
}
