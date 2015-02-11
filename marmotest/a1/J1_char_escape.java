// PARSER_WEEDER
public class J1_char_escape {
    public J1_char_escape() {}
    public static int test() {
	int r = 107;

	if ('\b' == 8) r=r+1;
	if ((int)("\b".charAt(0)) == 8) r=r+1;

	if ('\t' == 9) r=r+1;
	if ((int)("\t".charAt(0)) == 9) r=r+1;

	if ('\n' == 10) r=r+1;
	if ((int)("\n".charAt(0)) == 10) r=r+1;

	if ('\f' == 12) r=r+1;
	if ((int)("\f".charAt(0)) == 12) r=r+1;

	if ('\r' == 13) r=r+1;
	if ((int)("\r".charAt(0)) == 13) r=r+1;

	if ('\"' == 34) r=r+1;
	if ((int)("\"".charAt(0)) == 34) r=r+1;

	if ('\'' == 39) r=r+1;
	if ((int)("\'".charAt(0)) == 39) r=r+1;

	if ('\\' == 92) r=r+1;
	if ((int)("\\".charAt(0)) == 92) r=r+1;

	return r;
    }
}
