// TYPE_LINKING

/*
Unqualified names are handled by these rules: 
1. try the enclosing class or interface 
2. try any single-type-import (A.B.C.D) 
3. try the same package 
4. try any import-on-demand package (A.B.C.*) including java.lang.* 
*/

// This testcase is supposed to test 2 vs. 5
import bar.System;

public class Main {
    
    public Main() {}

    public static int test() {
	return new System().x;
    }

    public static void main(String[] args) {
	// System.out.println(""+Main.test());
    }
}
