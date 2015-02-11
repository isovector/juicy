// PARSER_WEEDER
public class J1_forifstatements1 {
    
    public J1_forifstatements1() {}

    public static int test() {
	int i = 42;
	if (i==42)
	    for (i=0; i<10; i=i+1) 
		if (i!=0) 
		    i = i; 
		else 
		    return 123;
	else
	    return 42;
	return 42;
    }
    
}
