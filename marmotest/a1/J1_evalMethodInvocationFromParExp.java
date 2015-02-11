// PARSER_WEEDER,TYPE_CHECKING
public class J1_evalMethodInvocationFromParExp {
    
    public J1_evalMethodInvocationFromParExp() {}

    public static int test() {
	return new Integer(("12"+"3").toString()).intValue();
    }
}
