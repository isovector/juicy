// JOOS1: PARSER_WEEDER,PARSER_EXCEPTION,JOOS1_INTERFACE
// JOOS2: HIERARCHY

public class Main {
    public Main() {}
    public Main(Interface obj) {obj.foo();}
    public static int test() {return 123;}
}
