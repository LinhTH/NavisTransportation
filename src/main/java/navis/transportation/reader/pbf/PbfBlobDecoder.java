package navis.transportation.reader.pbf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import navis.transportation.reader.OSMElement;

import org.openstreetmap.osmosis.osmbinary.Fileformat;
import org.openstreetmap.osmosis.osmbinary.Osmformat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Converts PBF block data into decoded entities 
 * allow multi-thread decoding
 */
public class PbfBlobDecoder implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(PbfBlobDecoder.class);
	
	// flag to check the correspondence of data
	private final boolean checkData = false;
	private final String blobType;
    private final byte[] rawBlob;
    private final ItfPbfBlobDecoderListener listener;
    private List<OSMElement> decodedEntities;
    
    public PbfBlobDecoder( String blobType, byte[] rawBlob, ItfPbfBlobDecoderListener listener ) {
    	this.blobType = blobType;
    	this.rawBlob  = rawBlob;
    	this.listener = listener;
    }
    
    /**
     * read blob data, if the data's format is Zlib encoding then decode it. Then uncompresses bytes in specified buffer (blobData)
     * @return blobData (byte)
     * @throws InvalidProtocolBufferException
     */
    private byte[] readBlobContent() throws InvalidProtocolBufferException {
    	Fileformat.Blob blob = Fileformat.Blob.parseFrom(rawBlob);
    	byte[] blobData;
    	if ( blob.hasRaw() ) { 
    		System.out.println(getClass().getName() + " handle the data raw");
    		blobData = blob.getRaw().toByteArray();
    	} else if ( blob.hasZlibData() ) { //decode the Zlib data
    		Inflater inflater = new Inflater();
    		inflater.setInput(blob.getZlibData().toByteArray());
    		blobData = new byte[blob.getRawSize()];
    		try {
    			inflater.inflate( blobData );
    		} catch ( DataFormatException e ) {
    			 throw new RuntimeException("Unable to decompress PBF blob.", e);
    		}
    		if ( !inflater.finished() ) {
    			throw new RuntimeException("PBF blob contains incomplete compressed data.");
    		}
    	} else {
    		throw new RuntimeException("PBF blob uses unsupported compression, only raw or zlib may be used.");
    	}
    	return blobData;
    }
    
    
    private void processOsmHeader( byte[] data ) throws InvalidProtocolBufferException {
    	Osmformat.HeaderBlock header = Osmformat.HeaderBlock.parseFrom(data);
    	// Build the list of active and unsupported features in the file.
        List<String> supportedFeatures = Arrays.asList("OsmSchema-V0.6", "DenseNodes");
        List<String> activeFeatures = new ArrayList<String>();
        List<String> unsupportedFeatures = new ArrayList<String>();
        for (String feature : header.getRequiredFeaturesList())
        {
        	//feature chi co hai string la OsmSchema-V0.6 va DenseNodes
            if (supportedFeatures.contains(feature))
            {
                activeFeatures.add(feature);
            } else
            {
                unsupportedFeatures.add(feature);
            }
        }

        if (unsupportedFeatures.size() > 0)
        {
            throw new RuntimeException("PBF file contains unsupported features " + unsupportedFeatures);
        }
        
    }
    
    
    private void runAndTrapExceptions() {
    	try {
	    	decodedEntities = new ArrayList<OSMElement>();
	    	//process the blob that has "OSMHeader" type
	    	if ( "OSMHeader".equals(blobType) ) {
	    		processOsmHeader( readBlobContent() ); //Chi kiem tra co du cac feature
	    	} else if ( "OSMData".equals(blobType) ) {
	    		//processOsmPrimitives(readBlobContent());
	    	} else {
	    		if (log.isDebugEnabled()) {
	    			log.debug("Skipping unrecognised blob type " + blobType);
	    		}
	    	}
    	}catch ( IOException e ) {
    		throw new RuntimeException("Unable to process PBF blob", e);
    	}
    }
    
	
	@Override
	public void run() {
		try {
			runAndTrapExceptions();
			listener.complete(decodedEntities);
		} catch( RuntimeException ex ) {
			listener.error(ex);
		}
	}
	
}
