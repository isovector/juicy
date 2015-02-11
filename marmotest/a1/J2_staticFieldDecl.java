// JOOS1: PARSER_WEEDER,JOOS1_STATIC_FIELD_DECLARATION,PARSER_EXCEPTION
// JOOS2: PARSER_WEEDER,DISAMBIGUATION
public class J2_staticFieldDecl {
	protected static int field = 23;
	public J2_staticFieldDecl() { }
	public static int test() { return J2_staticFieldDecl.field+100; }
}
