
package be.cosic.android.util;

public class MathUtils {
	public static int unsignedInt(int a) {
		if (a < 0) {
			return a + 256;
		}
		return a;
	}
	public static int min(int a, int b) {
		if (a < b) {
			return a;
		}
		return b;
	}
}
