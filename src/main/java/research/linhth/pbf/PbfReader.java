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
			
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	
}
