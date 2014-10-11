package research.linhth.pbf;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Decodes all blocks from a PBF stream using worker threads, and passes the results to the
 * downstream sink.
 * <p/>
 * @author Brett Henderson
 */
public class PbfDecoder implements Runnable {
	
	private final PbfStreamSplitter streamSplitter;
	private final ExecutorService executorService;
	private final int maxPendingBlobs;
	private final Sink sink;
	private final Lock lock; 
	private final Condition dataWaitCondition;
	private final Queue<PbfBlobResult> blobResult;
	
	/**
     * Creates a new instance.
     * <p/>
     * @param streamSplitter The PBF stream splitter providing the source of blobs to be decoded.
     * @param executorService The executor service managing the thread pool.
     * @param maxPendingBlobs The maximum number of blobs to have in progress at any point in time.
     * @param sink The sink to send all decoded entities to.
     */
	public PbfDecoder( PbfStreamSplitter streamSplitter, ExecutorService executorService, int maxPendingBlobs,
            Sink sink ) {
		this.streamSplitter = streamSplitter;
		this.executorService = executorService;
		this.maxPendingBlobs = maxPendingBlobs;
		this.sink = sink;
		
		//Khởi tạo kiểu đồng bộ hóa nguyên thủy
		lock = new ReentrantLock();
        dataWaitCondition = lock.newCondition();
        
        // Create the queue of blobs being decoded.
        blobResult = new LinkedList<PbfBlobResult>();
	}
	
	

	public void run() {
		// TODO Auto-generated method stub
		
	}
	
}
