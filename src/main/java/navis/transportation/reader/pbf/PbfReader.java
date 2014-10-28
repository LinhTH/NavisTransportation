package navis.transportation.reader.pbf;

import java.io.InputStream;

/**
 * Reading an OSM data source from a PBF file
 */
public class PbfReader implements Runnable {
	private InputStream inputStream;
	private ItfSink sink;
	private int workers;
	
	public PbfReader( InputStream in, ItfSink sink, int workers ) 
	{
		this.inputStream = in;
		this.sink = sink;
		this.workers = workers;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
