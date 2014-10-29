package navis.transportation.reader.pbf;

import java.util.Map;
import java.util.Map.Entry;

import navis.transportation.reader.OSMElement;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;

/**
 * Represents an OSM Way
 */
public class OSMWay extends OSMElement{
	protected final TLongList nodes = new TLongArrayList(5);
	
	public OSMWay( long id ) {
        super(id, WAY);
    }
	
    public TLongList getNodes()
    {
        return nodes;
    }
    
    @Override
    public String toString() {
    	System.out.println("Way : " + getId());
    	/*Map<String, Object> tags = getTags();
    	for (Entry<String, Object> e : tags.entrySet())
    	{
         System.out.println(e.getKey() + " : " + (String)e.getValue());
    	}*/
    	System.out.println("node of Way: " + nodes.size());
    	return null;
    }
}
