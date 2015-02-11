// JOOS1: PARSER_WEEDER,JOOS1_INTERFACE,PARSER_EXCEPTION
// JOOS2: HIERARCHY,DUPLICATE_METHOD
// JAVAC:UNKNOWN
/**
 * Parser/weeder:
 * - (Joos 1) No interfaces allowed
 * Hierarchy check:
 * - (Joos 2) A class or interface must not declare two methods with
 * the same name and parameter types (8.4, 9.4, well-formedness
 * constraint 2).
 */
public interface Je_14_Interface_DuplicateMethodDeclare {
    
    public void foo();
    
    public void foo();

}
