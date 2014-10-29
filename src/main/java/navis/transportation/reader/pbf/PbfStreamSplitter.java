package navis.transportation.reader.pbf;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.osmbinary.Fileformat;

/**
 * Parse a Pbf data stream and extracts the raw data of each blob
 */
public class PbfStreamSplitter implements Iterator<PbfRawBlob> {
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
	
	private void getNextBlob() {
		try {
			// Read the length of the next header block.
			int headerLength;
			try {
				headerLength = dis.readInt(); //Đọc được 13
			} catch ( EOFException e ) {
				eof = true;
				return;
			}
			
		//	System.out.println("Reading header for blob " + dataBlockCount++);
			Fileformat.BlobHeader blobHeader = readHeader(headerLength);
			
		//	System.out.println("Processing blob of type " + blobHeader.getType() + ".");
			byte[] blobData = readRawBlob(blobHeader);
			
			nextBlob = new PbfRawBlob(blobHeader.getType(), blobData);
				
		}catch (IOException e) {
			 throw new RuntimeException("Unable to get next blob from PBF stream.", e);
		}
	}
	
	@Override
	public boolean hasNext() {
		if ( nextBlob == null && !eof  ) {
			getNextBlob();
		}
		return nextBlob != null;
	}
	
	
	/**
	 * Get the next blob
	 */
	@Override
	public PbfRawBlob next() {
		PbfRawBlob currentBlob = nextBlob;
		nextBlob = null;
		return currentBlob;
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}
}
