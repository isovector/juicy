// TYPE_LINKING
/* TypeLinking:
 * Check that all import-on-demand declarations refer to existing packages.
 */

import test.*;

public class Main{
     public Main(){}

     public static int test(){
	 return A.test();
     }
}
