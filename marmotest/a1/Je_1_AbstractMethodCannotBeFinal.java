// JOOS1:PARSER_WEEDER,ABSTRACT_METHOD_FINAL_OR_STATIC,PARSER_EXCEPTION
// JOOS2:PARSER_WEEDER,ABSTRACT_METHOD_FINAL_OR_STATIC,PARSER_EXCEPTION
// JAVAC:UNKNOWN
/**
  * Parser/weeder:
	* - An abstract method cannot be final.
	*/
public abstract class Je_1_AbstractMethodCannotBeFinal {
	public Je_1_AbstractMethodCannotBeFinal() {}
	public final abstract void foo();
}
