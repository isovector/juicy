// PARSER_WEEDER
public class J1_if_then_for{
    
    public J1_if_then_for(){}

    public static int test(){
	int a = 43;
	if (a == 43)
	    for (int i = 0; a+i < 123; a = a + 1){ i = i+1; }
	return a+40;
    }

}
