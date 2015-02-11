// JOOS1: PARSER_WEEDER,JOOS1_INTERFACE,PARSER_EXCEPTION
// JOOS2: HIERARCHY
/*
       boz interface
      /   \
    bar   baz
     f     g
      \   /
       foo
*/

public class Main {

    public Main() {}

    public static int test() {
	foo f = new foo();
	return f.f();
    }

}
