package navis.transportation.support;

public interface ItfBtreeMap {
	
	/**
	 * put (key; value) data into Btree
	 */
	int put( long key, int value );

	/**
	 * return value associated with specified key.
	 */
    int get( long key );

    
    /**
     * Remove all unused resources.
     */
    void optimize();

}
