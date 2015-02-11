// PARSER_WEEDER
public class J1_for_no_short_if{
    
    public J1_for_no_short_if(){}

    public static int test(){
	int a = 43;
	if (a == 43)
	    for (int i = 0; a+i < 123; a = a + 1){ i = i+1; }
	else return 10;
	return a+40;
    }

}
