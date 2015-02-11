// JOOS1:PARSER_WEEDER,INVALID_SOURCE_FILE_NAME
// JOOS2:PARSER_WEEDER,INVALID_SOURCE_FILE_NAME
// JAVAC:UNKNOWN
// 
/**
 * Parser/weeder:
 * - A class declaration must reside in a .java source file with the same
 * base name as the class.
 **/
public class Suffix{

    public Suffix(){}

    public static int test(){
	return 123;
    }
}
