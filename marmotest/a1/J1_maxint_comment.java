// PARSER_WEEDER
public class J1_maxint_comment {
    public /*one*/J1_maxint_comment/*two*/(/*three*/)/*four*/{/*five*/}

    protected int huge = -/*helo?*/2147483648/*hello!*/;
    public static int test() {return new J1_maxint_comment().test2();}
    public int test2() {
	int gargantuan = -/*word?*/2147483648/*world!*/;
	return (huge+123-gargantuan);
    }

}
