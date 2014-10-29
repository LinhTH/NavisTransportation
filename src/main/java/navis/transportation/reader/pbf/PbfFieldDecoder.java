package navis.transportation.reader.pbf;

import org.openstreetmap.osmosis.osmbinary.Osmformat;

public class PbfFieldDecoder {
	private static final double COORDINATE_SCALING_FACTOR = 0.000000001;
    private String[] strings;
    private int coordGranularity;
    private long coordLatitudeOffset;
    private long coordLongitudeOffset;
    private int dateGranularity;
    
    /**
     * "Each PrimitiveBlock is independently decompressable, 
     *  containing all of the information to decompress the entities it contains.
     *  It contains a string table, it also encodes the granularity for both position and timestamps."
     *  FROM (http://wiki.openstreetmap.org/wiki/PBF_Format - read it) 
     * @param primitiveBlock The primitive block containing the fields to be decoded.
     */
    public PbfFieldDecoder( Osmformat.PrimitiveBlock primitiveBlock )
    {
        this.coordGranularity = primitiveBlock.getGranularity();
        this.coordLatitudeOffset = primitiveBlock.getLatOffset();
        this.coordLongitudeOffset = primitiveBlock.getLonOffset();
        this.dateGranularity = primitiveBlock.getDateGranularity();

        Osmformat.StringTable stringTable = primitiveBlock.getStringtable();
        strings = new String[stringTable.getSCount()];
        for (int i = 0; i < strings.length; i++)
        {
        	strings[i] = stringTable.getS(i).toStringUtf8();
        }
    }
    
    
    /**
     * latitude = .000000001 * (lat_offset + (granularity * lat))
	 * longitude = .000000001 * (lon_offset + (granularity * lon))
	 * FROM (http://wiki.openstreetmap.org/wiki/PBF_Format - read it) 
	 * @param rawLatitude The PBF encoded value.
     * @return The latitude in degrees.
     */
    public double decodeLatitude( long rawLatitude )
    {
        return COORDINATE_SCALING_FACTOR * (coordLatitudeOffset + (coordGranularity * rawLatitude));
    }
    
    /**
     * latitude = .000000001 * (lat_offset + (granularity * lat))
	 * longitude = .000000001 * (lon_offset + (granularity * lon))
	 * FROM (http://wiki.openstreetmap.org/wiki/PBF_Format - read it) 
	 * @param rawLongtitude The PBF encoded value.
     * @return The longtitude in degrees.
     */
    public double decodeLongitude( long rawLongitude )
    {
        return COORDINATE_SCALING_FACTOR * (coordLongitudeOffset + (coordGranularity * rawLongitude));
    }
    
    /**
     * Decodes a raw string into a String.
     * <p/>
     * @param rawString The PBF encoding string.
     * @return The string as a String.
     */
    public String decodeString( int rawString )
    {
        return strings[rawString];
    }
}
