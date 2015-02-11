// PARSER_WEEDER
public class J1_forifstatements2 {
    
    public J1_forifstatements2() {}

    public static int test() {
	int i = 42;
	if (i==42)
	    for (i=0; i<10; i=i+1) 
		if (i!=0) 
		    i=i; 
	else // must bind to closest if 
	    return 123;
	return 42;
    }
    
}
