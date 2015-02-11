// TYPE_LINKING
/* Check that all simple type names resolve uniquely, 
 * i.e. not to types from more than one import-on-demand 
 * declaration. 
 * OK, but what if we don't use it? */

import foo.*;
import bar.*;

public class Main{

    public Main(){}

    public static int test(){

	return 123;
    }

}
