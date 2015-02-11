// PARSER_WEEDER
public class  J1_forAlwaysInitAsWhile {

    public J1_forAlwaysInitAsWhile () {}

    public int foo() {
		return 123;
    }

    public int bar() {
		int i = 0;
		{
			i=foo();
			
				while(i>123) {
					{
							
					}
					i=i+1;
				}
			
		}
		return i;
    }

    public static int test() {
		J1_forAlwaysInitAsWhile j = new J1_forAlwaysInitAsWhile();
		
		return j.bar();
    }

}
