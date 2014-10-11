package research.linhth.pbf;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Reading an OSM data source from a PBF file.
 * @author rimberry
 * @refer: graphhopper.com
 *
 */
public class PbfReader implements Runnable
{
	private InputStream inputStream;
	private Sink sink;
	private int workers;
	
	public static void main(String []args) {
		System.out.println("Start");
	}
	
	public PbfReader( InputStream in, Sink sink, int workers ) 
	{
		this.inputStream = in;
		this.sink = sink;
		this.workers = workers;
	}

	public void run() {
		ExecutorService executorService = Executors.newFixedThreadPool(workers);
		try {
			//Tạo bộ tách dòng để chia dòng pbf thành các blob
			PbfStreamSplitter streamSplitter = new PbfStreamSplitter(new DataInputStream(this.inputStream));
			
			// Process all blobs of data in the stream using threads from the
            // executor service. We allow the decoder to issue an extra blob
            // than there are workers to ensure there is another blob
            // immediately ready for processing when a worker thread completes.
            // The main thread is responsible for splitting blobs from the
            // request stream, and sending decoded entities to the sink.
			PbfDecoder pbfDecoder = new PbfDecoder( streamSplitter, executorService, workers + 1, sink );
			pbfDecoder.run();		
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	
}
