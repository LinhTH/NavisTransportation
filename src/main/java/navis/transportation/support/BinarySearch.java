package navis.transportation.support;

public class BinarySearch {
	/**
	 * Use Binary Search to look up a key on keys array
	 * @param keys
	 * @param start
	 * @param len
	 * @param key
	 * @return
	 */
	 static int binarySearch( long array[], int start, int len, long key )
	   {
	       int high = start + len, low = start - 1, position;
	       while (high - low > 1)
	       {
	           position = (high + low) / 2;
	           long guessedKey = array[position];
	           if (guessedKey < key)
	           {
	               low = position;
	           } else
	           {
	               high = position;
	           }
	       }

	       if (high == start + len)
	       {
	           return ~(start + len);
	       }

	       long highKey = array[high];
	       if (highKey == key)
	       {
	           return high;
	       } else
	       {
	           return ~high;
	       }
	  }
}
