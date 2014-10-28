package navis.transportation.reader.pbf;

public class PbfRawBlob {
	private String type;
	private byte[] data;
	
	/**
	 * @param type The type of data represented by this blob (OSMHeader or OSMData)
	 * @param data The raw data
	 */
	public PbfRawBlob( String type, byte[] data ) {
		this.type = type;
		this.data = data;
	}
	
	public String getType() {
		return type;
	}

	public byte[] getData() {
		return data;
	}
}
