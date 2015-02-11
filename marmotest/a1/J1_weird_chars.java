// PARSER_WEEDER
public class J1_weird_chars{

    public J1_weird_chars(){}

    public static int test(){

	String s = "\\b";
	if (s.indexOf("\\") == 0
	    && s.indexOf("b") == 1
	    && s.indexOf("\b") == -1
	    && s.length() == 2) {
		return 123;
	}

	return 42;
    }
}
