// ENVIRONMENTS
public class J1_localvariablescope {

    public Object o;

    public J1_localvariablescope () {
	{
	    { Object o = new Object(); }
	    { 
		{ Object o = new Object(); }
		{ Object o = new Object(); }
	    }
	    { Object o = new Object(); }
	    Object o = new Object();
	}
	Object o = new Object();
    }
    
    public static Object m() {
	{
	    { Object o = new Object(); }
	    { 
		{ Object o = new Object(); }
		{ Object o = new Object(); }
	    }
	    { Object o = new Object(); }
	    Object o = new Object();
	}
	Object o = new Object();
	return o;
    }

    public static int test() {
	Object o = null;
	o = new J1_localvariablescope();
	o = J1_localvariablescope.m();
        return 123;
    }

}
