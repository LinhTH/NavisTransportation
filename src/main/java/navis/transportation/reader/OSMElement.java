package navis.transportation.reader;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Base class for all OSM objects
 */
public abstract class OSMElement {
	 public static final int NODE = 0;
     public static final int WAY = 1;
     public static final int RELATION = 2;
     
     private final int type;
     private final long id;
     private final Map<String, Object> properties = new HashMap<String, Object>(5); //containes tags of Node
     
     protected OSMElement( long id, int type ) {
         this.id = id;
         this.type = type;
     }
     
     public long getId() {
         return id;
     }
     
     public int getType() {
    	 return type;
     }
     
     protected Map<String, Object> getTags() {
         return properties;
     }
     
     public void setTags( Map<String, String> newTags ) {
         properties.clear();
         if (newTags != null)
             for (Entry<String, String> e : newTags.entrySet())
             {
                 setTag(e.getKey(), e.getValue());
             }
     }
     
     public void setTag( String name, Object value ) {
         properties.put(name, value);
     }
     
     public String getTag(String name) {
    	 if (hasTag(name)) 
    		 return (String) properties.get(name);
    	 else
    		 return "No name";
     }
     
     public boolean hasTag( String key, String value )
     {
         return value.equals(properties.get(key));
     }
     
     public boolean hasTags() {
    	 return !properties.isEmpty();
     }
     
     public boolean hasTag( String name )
     {
    	 return properties.containsKey(name);
     }
     
     
}
