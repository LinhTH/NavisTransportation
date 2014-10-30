package navis.transportation.support;

import java.io.Closeable;
import java.io.IOException;

public class Helper {
	public static final long MB = 1L << 20; // tính theo bytes
	
	public static boolean isEmpty(String str) {
		if ( str == null || str.trim().length() == 0 )
			return true;
		else 
			return false;
	}
	
	public static void close( Closeable cl ) {
		try {
			if (cl != null) {
				cl.close();
			}
		} catch (IOException ex) {
			throw new RuntimeException("Couldn't close resource", ex);
		}
	}
}
