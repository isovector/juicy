// JOOS1:TYPE_LINKING,AMBIGUOUS_CLASS_NAME
// JOOS2:TYPE_LINKING,AMBIGUOUS_CLASS_NAME
// JAVAC:UNKNOWN
// 
/**
 * TypeLinking
 * - Class Date can be found in both package java.util and package 
 * java.sql.
 */
import java.util.*;
import java.sql.*;

public class Main {

    public Main() { }

    public static int test() {
	Date d = new Date();
	return 123;
    }
}
