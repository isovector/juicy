//ENVIRONMENTS

public class J1_2_Locals_Overlapping_AfterBlock {
	public J1_2_Locals_Overlapping_AfterBlock() {}
	
	public static int test() {
		{
			int a = 0;
		}
		int a = 123;
		return a;
	}
}
