// PARSER_WEEDER,HIERARCHY
public class J1_constructorWithSameNameAsMethod {

    public J1_constructorWithSameNameAsMethod () {}

    public int J1_constructorWithSameNameAsMethod () {
        return 123;
    }

    public static int test() {
        return new J1_constructorWithSameNameAsMethod().J1_constructorWithSameNameAsMethod();
    }

}
