package research.linhth.OSM;

import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

/*
 * @author: graphhopper.com
 */

public abstract class OSMElement {
	public static final int NODE = 0;
	public static final int WAY  = 1;
	public static final int RELATION = 2;
	
	private final int type;
	private final long id;
	private final Map<String, Object> properties = new HashMap<String, Object>(5);
	
	protected OSMElement( long id, int type ) {
		this.id = id;
		this.type = type;
	}
	
	public long getId()
	{
		return id;
	}
	
	protected void readTags( XMLStreamReader parser ) 
	{
		int event = parser.getEventType();
		while ( event != XMLStreamConstants.END_DOCUMENT && parser.getLocalName().equals("tag")) {
			
		}
		
	}
	
}






















