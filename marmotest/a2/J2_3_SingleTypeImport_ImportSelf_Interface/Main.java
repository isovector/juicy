//TYPE_LINKING
//JOOS1:PARSER_WEEDER,JOOS1_INTERFACE,PARSER_EXCEPTION
/**
 * TypeLinking:
 * - The name of a class must not clash with the name of a singletype
 * import, but a class may import itself.
 */
public class Main{

    public Main(){}

    public static int test(){
		return 123;
    }

}
