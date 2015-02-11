// JOOS1:PARSER_WEEDER,JOOS1_INTERFACE,PARSER_EXCEPTION
// JOOS2:HIERARCHY,CIRCULAR_INHERITANCE
// JAVAC:UNKNOWN
/**
 * Parser/weeder:
 * - (Joos 1) No interfaces allowed
 * Hierarchy check:
 * - (Joos 2) A class or interface must not depend on itself 
 * (8.1.3, 9.1.2)
 */
public class Main{
    public Main() {
    }
	
    public static int test(){
	return 123;
    }

}
