// PARSER_WEEDER
public class J1_1_Cast_MultipleCastOfSameValue_1{

    public J1_1_Cast_MultipleCastOfSameValue_1 () {}

    public static int test() {
	Object a = new Object();
	a = (Object) (Object) a;
        return 123;
    }

}
