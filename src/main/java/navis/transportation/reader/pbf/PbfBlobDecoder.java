package navis.transportation.reader.pbf;

import gnu.trove.list.TLongList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import navis.transportation.reader.OSMElement;
import navis.transportation.reader.OSMNode;

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

    
    private void processNodes( Osmformat.DenseNodes nodes, PbfFieldDecoder fieldDecoder ) {
    	List<Long> idList  = nodes.getIdList();
    	List<Long> latList = nodes.getLatList();
    	List<Long> lonList = nodes.getLonList();
    	
    	if (checkData) {
            if ((idList.size() != latList.size()) || (idList.size() != lonList.size())) {
                throw new RuntimeException("Number of ids (" + idList.size() + "), latitudes (" + latList.size()
                        + "), and longitudes (" + lonList.size() + ") don't match");
            }
        }
    	
    	Iterator<Integer> keysValuesIterator = nodes.getKeysValsList().iterator();
    	long nodeId = 0;
        long latitude = 0;
        long longitude = 0;
        
    	for ( int i = 0; i < idList.size(); i++ ) {
    		nodeId    += idList.get(i);
    		latitude  += latList.get(i);
    		longitude += lonList.get(i);
    		
    		Map<String, String> tags = null;

    		while( keysValuesIterator.hasNext() ) { //build tags
    			int keyIndex = keysValuesIterator.next();
    			if (keyIndex == 0)
    				break;
    			
    			 if (checkData) {
                     if (!keysValuesIterator.hasNext()) {
                         throw new RuntimeException(
                                 "The PBF DenseInfo keys/values list contains a key with no corresponding value.");
                     }
                 }
    			 int valueIndex = keysValuesIterator.next();
    			 if (tags == null) {
                     tags = new HashMap<String, String>();
                 } 
    			 tags.put(fieldDecoder.decodeString(keyIndex), fieldDecoder.decodeString(valueIndex));
    			// if ( Thread.currentThread().getId() == 11) 
    	    			//System.out.println("put : " +fieldDecoder.decodeString(keyIndex) +", " + fieldDecoder.decodeString(valueIndex));
    		}
    		//if ( Thread.currentThread().getId() == 11 && tags != null && tags.size() > 2 ) 
    			//System.out.println("finish tags");
    		OSMNode node = new OSMNode(nodeId, ((double) latitude) / 10000000, ((double) longitude) / 10000000);
            node.setTags(tags);

            // Add the bound object to the results.
            decodedEntities.add(node);
    	}
    }
    
    private void processWays( List<Osmformat.Way> ways, PbfFieldDecoder fieldDecoder )
    {
        for (Osmformat.Way way : ways)
        {  	
            Map<String, String> tags = buildTags(way.getKeysList(), way.getValsList(), fieldDecoder);
            OSMWay osmWay = new OSMWay(way.getId());
            // EXAMPLE TAGS OF A OSMWAY  - http://www.openstreetmap.org/way/144391828
            // Way : 144391828
            // layer : 1
            // ref : 80
            // bridge : yes
            // highway : primary
            // name : Cầu Tà Xăng
            osmWay.setTags(tags);

            // Build up the list of way nodes for the way
            long nodeId = 0;
            TLongList wayNodes = osmWay.getNodes();
            for (long nodeIdOffset : way.getRefsList())
            {
                nodeId += nodeIdOffset; // delta encoding - http://en.wikipedia.org/wiki/Delta_encoding
                wayNodes.add(nodeId);
            }
            decodedEntities.add(osmWay);
        }
    }
    
    /**
     * Build Tags for ways
     * @return
     */
    private Map<String, String> buildTags( List<Integer> keys, List<Integer> values, PbfFieldDecoder fieldDecoder ) {
        if (checkData) {
            if (keys.size() != values.size()) {
                throw new RuntimeException("Number of tag keys (" + keys.size() + ") and tag values ("
                        + values.size() + ") don't match");
            }
        }
        Iterator<Integer> keyIterator = keys.iterator();
        Iterator<Integer> valueIterator = values.iterator();
        if (keyIterator.hasNext())
        {
            Map<String, String> tags = new HashMap<String, String>();
            while (keyIterator.hasNext())
            {
                String key = fieldDecoder.decodeString(keyIterator.next());
                String value = fieldDecoder.decodeString(valueIterator.next());
                tags.put(key, value);
            }
            return tags;
        }
        return null;
    }
   
    
    private void processOsmPrimitives( byte[] data ) throws InvalidProtocolBufferException {
    	//With vietnam.pbf , it has got ~ 356 OSMdata for the time being.
    	Osmformat.PrimitiveBlock block = Osmformat.PrimitiveBlock.parseFrom(data); 
    	PbfFieldDecoder fieldDecoder = new PbfFieldDecoder(block);
    	for ( Osmformat.PrimitiveGroup primitiveGroup :  block.getPrimitivegroupList() ) 
    	{
    		 processNodes( primitiveGroup.getDense(), fieldDecoder );
    		 processWays(primitiveGroup.getWaysList(), fieldDecoder);
    	}
    }
    
   
       
    private void runAndTrapExceptions() {
    	try {
    		//Store a decoded data from a blob
	    	decodedEntities = new ArrayList<OSMElement>();
	    	//process the blob that has "OSMHeader" type
	    	if ( "OSMHeader".equals(blobType) ) {
	    		processOsmHeader( readBlobContent() ); 
	    	} else if ( "OSMData".equals(blobType) ) {
	    		processOsmPrimitives(readBlobContent());
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
