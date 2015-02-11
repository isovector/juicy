// HIERARCHY,TYPE_CHECKING
public class J1_cast_to_same_type{

    public J1_cast_to_same_type(){}

    public static int test(){

	String s = "123";

	return Integer.parseInt((String)s);
    }

}
