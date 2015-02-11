// JOOS1:PARSER_WEEDER,JOOS1_INTERFACE,PARSER_EXCEPTION
// JOOS2:HIERARCHY,PROTECTED_REPLACE_PUBLIC
// JAVAC:UNKNOWN
/**
 * Parser/weeder:
 * - (Joos 1) No interfaces allowed
 * Hierarchy check:
 * - (Joos 2) A protected method must not override a public method
 * (8.4.6.3). (Interface methods are implicitly public).
 */
public abstract class Main implements Foo{
    
    public Main() { }
    
    public static int test() { 
	return 123; 
    }

    protected abstract void bleh();
}
