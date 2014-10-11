package research.linhth.pbf;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.osmbinary.Fileformat;

/**
 * Parses a PBF data stream and extracts the raw data of each blob in sequence until the end of the
 * stream is reached.
 * <p/>
 * @author Brett Henderson
 */
public class PbfStreamSplitter implements Iterable<PbfRawBlob> {
	
	private static Logger log = Logger.getLogger(PbfStreamSplitter.class.getName());
	private DataInputStream dis;
	private int dataBlockCount;
	private boolean eof;
	private PbfRawBlob nextBlob;
	
	/**
     * Creates a new instance.
     * <p/>
     * @param pbfStream The PBF data stream to be parsed.
     */
	public PbfStreamSplitter( DataInputStream pbfStream ) {
		dis = pbfStream;
		dataBlockCount = 0;
		eof = false;
	}
	
	private Fileformat.BlobHeader readHeader( int headerLength ) throws IOException {
		byte[] headerBuffer = new byte[headerLength];
		dis.readFully(headerBuffer);
		
		Fileformat.BlobHeader blobHeader = Fileformat.BlobHeader.parseFrom(headerBuffer);
		
		return blobHeader;
	}
	 
	

	public Iterator<PbfRawBlob> iterator() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
