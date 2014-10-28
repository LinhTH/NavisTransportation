package navis.transportation.reader.pbf;

import java.util.List;

import navis.transportation.reader.OSMElement;

/**
 * Stores the results for a decoded Blob.
 */
public class PbfBlobResult {
	private List<OSMElement> entities;
	private boolean complete;
	private boolean success;
	private Exception ex;
	
	public PbfBlobResult() {
		complete = false;
		success = false;
		ex = new RuntimeException("no success result stored");
	}
	
	/**
	 * Stores a success result for a blob decoding operation
	 * @param decodedEntities
	 */
	public void storeSuccessResult( List<OSMElement> decodedEntities ) {
		entities = decodedEntities;
		complete = true;
		success = true;
	}
	
	/**
	 * Opposite with above method
	 * @param ex
	 */
	public void storeFailureResult( Exception ex ) {
		complete = true;
		success = false;
		this.ex = ex;
	}
	
	public boolean isComplete()
    {
        return complete;
    }
    
    public boolean isSuccess()
    {
        return success;
    }
    
    public Exception getException()
    {
        return ex;
    }
    
    public List<OSMElement> getEntities()
    {
        return entities;
    }
}
