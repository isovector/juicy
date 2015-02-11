// PARSER_WEEDER
public class J1_1_Cast_MultipleCastOfSameValue_3{

    public J1_1_Cast_MultipleCastOfSameValue_3 () {}

    public static int test() {
	Object a = new Integer(0);
	a = (Object) (Number) (Integer) a;
        return 123;
    }

}
