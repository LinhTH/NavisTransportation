package navis.transportation.reader.pbf;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.osmbinary.Fileformat;

/**
 * Parse a Pbf data stream and extracts the raw data of each blob
 */
public class PbfStreamSplitter implements Iterable<PbfRawBlob> {
	private static Logger log = Logger.getLogger(PbfStreamSplitter.class.getName());
	private DataInputStream dis;
	private int dataBlockCount;
	private boolean eof;
	private PbfRawBlob nextBlob;
	
	public PbfStreamSplitter( DataInputStream pbfStream ) {
		dis = pbfStream;
		dataBlockCount = 0;
		eof = false;
	}
	
	/**
	 * Read a number of byte from DataInputStream and convert it to BlobHeader format
	 * @param headerLength
	 * @return
	 * @throws IOException
	 */
	private Fileformat.BlobHeader readHeader(int headerLength) throws IOException {
		byte[] headerBuffer = new byte[headerLength];
		dis.readFully(headerBuffer);
		Fileformat.BlobHeader blobHeader = Fileformat.BlobHeader.parseFrom(headerBuffer);
		
		return blobHeader;
	}
	
	private byte[] readRawBlob( Fileformat.BlobHeader blobHeader ) throws IOException {
		byte[] rawBlob = new byte[blobHeader.getDatasize()]; 
		dis.readFully(rawBlob);
		
		return rawBlob;
	}
	
	@Override
	public Iterator<PbfRawBlob> iterator() {
		// TODO Auto-generated method stub
		return null;
	}
}
