// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:UNKNOWN
/**
 * Parser/weeder:
 * - A PType node in an extends clause, implements clause, throws
 * clause or class instantiation expression must be an ANamedType
 */
public class Je_1_ClassInstantiation_InstantiateSimpleValue {

    public Je_1_ClassInstantiation_InstantiateSimpleValue() { }

    public static int test() { 
	new 1();
	return 123; 
    }

}
