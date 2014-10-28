package navis.transportation.reader.pbf;

import java.io.DataInputStream;
import java.util.Iterator;
import java.util.logging.Logger;

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
	
	@Override
	public Iterator<PbfRawBlob> iterator() {
		// TODO Auto-generated method stub
		return null;
	}
}
