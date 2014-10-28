package navis.transportation.reader.pbf;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
		ExecutorService executorService = Executors.newFixedThreadPool(workers);
		try {
			//Splitter a pbf stream data into blob
			PbfStreamSplitter streamSplitter = new PbfStreamSplitter(new DataInputStream(this.inputStream));
			
			// Process all blobs of data in the stream using threads from the
            // executor service. We allow the decoder to issue an extra blob
            // than there are workers to ensure there is another blob
            // immediately ready for processing when a worker thread completes.
            // The main thread is responsible for splitting blobs from the
            // request stream, and sending decoded entities to the sink.
			PbfDecoder pbfDecoder = new PbfDecoder( streamSplitter, executorService, workers + 1, sink );
			//Chay luong run cua PbfDecoder
			pbfDecoder.run();		
		} catch (Exception e) {
			throw new RuntimeException("Unable to read PBF file.", e);
		} finally {
			sink.complete();
			executorService.shutdownNow();
		}
	}
}
