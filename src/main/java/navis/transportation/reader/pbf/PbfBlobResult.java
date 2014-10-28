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
}
