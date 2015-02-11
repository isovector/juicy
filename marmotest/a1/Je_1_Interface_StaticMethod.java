// PARSER_WEEDER
// JOOS1: JOOS1_INTERFACE,PARSER_EXCEPTION
// JOOS2: STATIC_OR_FINAL_INTERFACE_METHOD,PARSER_EXCEPTION
// JAVAC: UNKNOWN
/**
 * Parser/weeder:
 * - (Joos 1) No interfaces allowed
 * - (Joos 2) An interface method cannot be static or final
 */
public interface Je_1_Interface_StaticMethod {

    public static void foo();

}
