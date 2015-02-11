// JOOS1:HIERARCHY,DIFFERENT_RETURN_TYPE
// JOOS2:HIERARCHY,DIFFERENT_RETURN_TYPE
// JAVAC:UNKNOWN
// 
/*
 * Hierarchy:
 * - A class or interface must not contain (declare or inherit) two
 * methods with the same name and parameter types but different return
 * types (8.1.1.1, 8.4, 8.4.2, 8.4.6.3, 8.4.6.4, 9.2, 9.4.1,
 * well-formedness constraint 3).
 *
 * Interfaces javax.naming.Name and javax.naming.directory.Attribute are
 * incompatible; both define getAll(), but with unrelated return
 * types.
 *
 *
 *  javax.naming.directory.Attribute                  javax.naming.Name
 *	  interface                                     interface
 *  method NamingEnumeration getAll             method Enumeration getAll
 *	      \                                         /
 *		\                                       /
 *				   foo
 *			  abstract 
*/
public abstract class Main implements A, B {

    public Main () {}


    public static int test(){
	return 123;
    }
}
