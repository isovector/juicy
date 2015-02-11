//JOOS1:PARSER_WEEDER,PARSER_EXCEPTION,JOOS1_MULTI_ARRAY
//JOOS2:TYPE_CHECKING,NON_NUMERIC_ARRAY_SIZE
//JAVAC:UNKNOWN
public class Je_16_MultiArrayCreation_Null {
	public Je_16_MultiArrayCreation_Null() {}
	
	public static int test() {
		int[][][] a = new int[5][null][];
		return 123;
	}
}
