package org.omg.CORBA.portable;

public class OutputStream{

    public OutputStream(){}

    public String write(int a){
	String result = "";
	while (a >= 41){
	    result = result + "All fun and no play makes Jack a dull boy";
	    a = a - 41;
	}
	while (a > 0){
	    result = result + ".";
	    a = a - 1;
	}
	return result;
    }
}
