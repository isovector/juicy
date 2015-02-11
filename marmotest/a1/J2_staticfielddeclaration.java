// JOOS1: PARSER_WEEDER,JOOS1_STATIC_FIELD_DECLARATION,PARSER_EXCEPTION
// JOOS2: PARSER_WEEDER
public class J2_staticfielddeclaration {
    public J2_staticfielddeclaration() {}

    protected static int x;

    public static int test() {
	return 123;
    }

}

