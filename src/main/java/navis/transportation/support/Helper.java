package navis.transportation.support;

public class Helper {
	public static final long MB = 1L << 20; // tính theo bytes
	
	public static boolean isEmpty(String str) {
		if ( str == null || str.trim().length() == 0 )
			return true;
		else 
			return false;
	}
}
