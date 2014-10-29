package navis.transportation.reader;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.management.openmbean.InvalidOpenTypeException;

import navis.transportation.reader.pbf.ItfProcessBox;
import navis.transportation.reader.pbf.PbfReader;

public class OSMInputFile implements ItfProcessBox, Closeable {
    private boolean eof;
    private InputStream bis;
    
    private final BlockingQueue<OSMElement> itemQueue;
    private boolean hasIncomingData;
    private int workerThreads = -1;
    private boolean binary = false;
    
    public OSMInputFile( File file ) throws IOException {
	   	 // Decode and determine the type of file.
	   	 bis = decode(file);
	   	 //if queue is full, it blocks itself => don't put anything into it
		 itemQueue = new LinkedBlockingQueue<OSMElement>(50000);
	}
    
    public OSMInputFile open() {
    	openPBFReader(bis);
    	return this;
    }
    
    @SuppressWarnings({ "unchecked" })
	private InputStream decode( File file ) throws IOException {
    	final String name = file.getName();
    	
    	InputStream is = null;	
    	try {
			is = new BufferedInputStream(new FileInputStream(file), 50000);
		} catch (FileNotFoundException e) {
			 throw new RuntimeException(e);
		}
    	is.mark(10); //mark 10th bit
    	
    	// check file header
        byte header[] = new byte[6];
        is.read(header); //read 6 first bit.
        
        if (header[0] == 0 && header[1] == 0 && header[2] == 0
                && header[4] == 10 && header[5] == 9
                && (header[3] == 13 || header[3] == 14))
        {
            is.reset();
            binary = true;
            return is;
        } else 
        	throw new InvalidOpenTypeException("Just using the pbf file.");
    }  
    
    
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void process(OSMElement item) {
		try
        {
            // blocks if full
            itemQueue.put(item);
        } catch (InterruptedException ex)
        {
            throw new RuntimeException(ex);
        }
	}

	@Override
	public void complete() {
		hasIncomingData = true;
	}
	
    public OSMInputFile setWorkerThreads( int num )
    {
        workerThreads = num;
        return this;
    }
    
    Thread pbfReaderThread;
	private void openPBFReader( InputStream stream ) {
		hasIncomingData = true;

        if (workerThreads <= 0)
            workerThreads = 2;
        
        PbfReader reader = new PbfReader( stream, this, workerThreads );
       
        pbfReaderThread  = new Thread(reader, "PBF Reader"); 
        pbfReaderThread.start();
	}
}
