// TYPE_LINKING
/**
 * TypeLinking:
 * - Tests whether singletype imports indeed have higher priority than
 * on-demand imports.
 */
import Test.OutputStream;
import java.io.*;
import org.omg.CORBA.portable.*;

public class Main{

    public Main(){}

    public static int test(){
	OutputStream o = new OutputStream();
	return o.write(123).length();
    }

}
