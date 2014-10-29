package navis.transportation.reader.pbf;

import java.util.Date;
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
	private final ItfProcessBox processBox;
	private final Lock lock; 
	private final Condition dataWaitCondition;
	private final Queue<PbfBlobResult> blobResults;
	
	public PbfDecoder( PbfStreamSplitter streamSplitter, ExecutorService executorService, int maxPendingBlobs,
            ItfProcessBox processBox ) {
		this.streamSplitter = streamSplitter;
		this.executorService = executorService;
		this.maxPendingBlobs = maxPendingBlobs; // = number of Workers + 1
		this.processBox = processBox;
		
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
					lock.lock();
                    try
                    {
                        System.out.println(getClass().getName() + " -- ERROR: " + new Date());
                        blobResult.storeFailureResult(ex);
                        signalUpdate();
                    } finally
                    {
                        lock.unlock();
                    }		
				}
				
				@Override
				public void complete(List<OSMElement> decodedEntities) {
					//System.out.println(Thread.currentThread().getId() + " process complete");
					lock.lock();
                    try
                    {
                    //	System.out.println(Thread.currentThread().getId() + " process complete");
                    	blobResult.storeSuccessResult(decodedEntities);
						signalUpdate();
                    } finally
                    {
                        lock.unlock();
                    }		
				}
			};
			
			// Create the blob decoder itself and execute it on a worker thread.
			PbfBlobDecoder blobDecoder = new PbfBlobDecoder(rawBlob.getType(), rawBlob.getData(), blobDecoderListener);
			executorService.execute(blobDecoder);
			// If the number of pending blobs has reached capacity then we send results to ProcessBox(OSMInputFile).
			sendResultsToProcessBox(maxPendingBlobs - 1);
		}
		// eof of stream. Send all remaining data to ProcessBox (OSMInputFile)
		sendResultsToProcessBox(0);
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
	
	/**
     * Any thread can call this method when they wish to wait until an update has been performed by
     * another thread.
     */
    private void waitForUpdate()
    {
        try
        {
            dataWaitCondition.await();
        } catch (InterruptedException e)
        {
            throw new RuntimeException("Thread was interrupted.", e);
        }
    }
	
	/**
	 * wake up the waiting threads
	 */
	private void signalUpdate() {
		dataWaitCondition.signal();
	}
	
	private void sendResultsToProcessBox( int numberOfWorker ) { //
		while (blobResults.size() > numberOfWorker) {
			// Get the next result from the queue and wait for it to complete.
			PbfBlobResult blobResult = blobResults.remove(); // get blob from the head of Queue
			//System.out.println(Thread.currentThread().getId() + " come here");
			while (!blobResult.isComplete()) {
				waitForUpdate(); 
			}
			if (!blobResult.isSuccess()) {
	                throw new RuntimeException("A PBF decoding worker thread failed, aborting.", blobResult.getException());
	        }
			//Duy nhat chi thay 1 luong 8 la hoat dong - Can xem lai de day luong len
			//System.out.println(Thread.currentThread().getId() + " unlock");
			lock.unlock();
			try {
				//Luoong put ket qua vao la luong hien tai - 8
				for ( OSMElement entity : blobResult.getEntities() ) {
					processBox.process(entity);
				}
			} finally {
				//System.out.println(Thread.currentThread().getId() + " lock");
				lock.lock();
			}
		}
	}
}
