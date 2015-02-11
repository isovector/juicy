// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION
// JAVAC:
/**
 * Parser/weeder:
 * - Switch statements not allowed in Joos.
 */
public class Je_1_NonJoosConstructs_Switch {

    public Je_1_NonJoosConstructs_Switch(){}

    public static int test(){
	int x = 42;
	switch (x){
	case 42: return 123;
	}
	return 2;
    }

}
