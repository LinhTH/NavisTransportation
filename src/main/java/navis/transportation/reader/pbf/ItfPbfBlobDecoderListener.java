package navis.transportation.reader.pbf;

import java.util.List;

import navis.transportation.reader.OSMElement;


/**
 * Instances of this interface are used to receive results from PBFBlobDecoder.
 */
public interface ItfPbfBlobDecoderListener {
	/**
     * Provides the listener with the list of decoded entities.
     * <p/>
     * @param decodedEntities The decoded entities.
     */
    void complete( List<OSMElement> decodedEntities );

    /**
     * Notifies the listener that an error occurred during processing.
     */
    void error( Exception ex );
}
