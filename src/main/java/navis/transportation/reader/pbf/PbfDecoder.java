package navis.transportation.reader.pbf;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import navis.transportation.reader.OSMElement;

/**
 * Decodes all blocks from a PBF stream using worker threads
 */
public class PbfDecoder implements Runnable{
	private final PbfStreamSplitter streamSplitter;
	private final ExecutorService executorService;
	private final int maxPendingBlobs;
	private final ItfSink sink;
	private final Lock lock; 
	private final Condition dataWaitCondition;
	private final Queue<PbfBlobResult> blobResults;
	
	public PbfDecoder( PbfStreamSplitter streamSplitter, ExecutorService executorService, int maxPendingBlobs,
            ItfSink sink ) {
		this.streamSplitter = streamSplitter;
		this.executorService = executorService;
		this.maxPendingBlobs = maxPendingBlobs;
		this.sink = sink;
		
		//Khởi tạo kiểu đồng bộ hóa nguyên thủy
		lock = new ReentrantLock();
		// http://congdongjava.com/forum/threads/%C4%90a-lu%C3%B4%CC%80ng-trong-java-multithreading-in-java.802/
        dataWaitCondition = lock.newCondition();
        
        // Create the queue of blobs being decoded.
        blobResults = new LinkedList<PbfBlobResult>();
	}
	
	private void processBlobs() {
		while (streamSplitter.hasNext()) {
			PbfRawBlob rawBlob = streamSplitter.next();
			//create the result object to capture the result of the decoded blob and it to the blob results queue
			final PbfBlobResult blobResult = new PbfBlobResult();
			blobResults.add(blobResult);
			
			ItfPbfBlobDecoderListener blobDecoderListener = new ItfPbfBlobDecoderListener() {				
				@Override
				public void error(Exception ex) {
					
				}
				
				@Override
				public void complete(List<OSMElement> decodedEntities) {
					
				}
			};
		}
	}
	
	
	@Override
	public void run() {
		lock.lock();
		try {
			processBlobs();
		} finally {
			lock.unlock();
		}
		
	}
	
}
