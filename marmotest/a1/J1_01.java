// PARSER_WEEDER,CODE_GENERATION
public class J1_01 {
    public J1_01() {}
    public static int test() {
	int r1 = J1_01.m0(0);
	int r2 = J1_01.m0(1);
	int r3 = J1_01.m0(100);
	int r4 = J1_01.m1(0);
	int r5 = J1_01.m1(1);
	int r6 = J1_01.m1(100);

	int r = 0;
	if (r1==9) r=r+1;
	if (r2==6) r=r+1;
	if (r3==6) r=r+1;

	if (r4==6) r=r+1;
	if (r5==9) r=r+1;
	if (r6==6) r=r+1;
	
	return 117+r;
    }

    public static int m0(int a) {
	int r = 0;
	if (a==0) r=r+1;
	if (a!=0) r=r+2;
	if (!(a==0)) r=r+4;
	if (!(a!=0)) r=r+8;
	return r;
    }

    public static int m1(int a) {
	int r = 0;
	if (a==1) r=r+1;
	if (a!=1) r=r+2;
	if (!(a==1)) r=r+4;
	if (!(a!=1)) r=r+8;
	return r;
    }
}
