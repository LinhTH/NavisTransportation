package navis.transportation.support;

public interface ItfPointAccess {
	/**
	 * @return true if elevation data is stored
	 */
	boolean is3D();
	
	int getDimension();
	
	/**
	 * This methods sets the latitude, longitude and elevation to the specified value.
	*/
	void setNode( int nodeId, double lat, double lon );
	
	void setNode( int nodeId, double lat, double lon, double ele );
	
	/**
	 * @param nodeId
	 * @return latitude of the specified node
	 */
	double getLatitude( int nodeId );
	
	/**
	 * @param nodeId
	 * @return longtitude of the specified node
	 */
	double getLongtitude( int nodeId );
	
	
	/**
	 * @param nodeId
	 * @return elevation of the specified node
	 */
	double getElevation( int nodeId );
}
