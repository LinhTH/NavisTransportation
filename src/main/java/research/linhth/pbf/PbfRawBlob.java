package research.linhth.pbf;

/**
 * Represents a single piece of raw blob data extracted from the PBF stream. It has not yet been
 * decoded into a PBF blob object.
 * <p/>
 * @author Brett Henderson
 */
public class PbfRawBlob {
	private String type;
	private byte[] data;
	
	/**
     * Creates a new instance.
     * <p/>
     * @param type The type of data represented by this blob. This corresponds to the type field in
     * the blob header.
     * @param data The raw contents of the blob in binary undecoded form.
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
