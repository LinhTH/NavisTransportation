package navis.transportation.reader;

import navis.transportation.support.ItfPointAccess;

/**
 * Represent an OSM Node
 *
 */
public class OSMNode extends OSMElement{
    private final double lat;
    private final double lon;
    
    public OSMNode( long id, double lat, double lon )
    {
        super(id, NODE);

        this.lat = lat;
        this.lon = lon;
    }
    
    /**
     * @return latitude of node
     */
    public double getLat() {
    	return lat;
    }
    
    /**
     * @return longtitude of node
     */
    public double getLon() {
    	return lon;
    }
   
}
