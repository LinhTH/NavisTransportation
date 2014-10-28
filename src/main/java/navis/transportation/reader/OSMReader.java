package navis.transportation.reader;

import gnu.trove.map.hash.TLongLongHashMap;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parsing an OSM pbf file.
 * <p/>
 * 1. a) Reads all ways from OSM pbf file and stores all associated node ids in osmNodeIdToIndexMap. If a
 * node occurs once it is a pillar node and if more it is a tower node (it represents for the junction), otherwise
 * osmNodeIdToIndexMap returns EMPTY.
 * <p>
 */
public class OSMReader implements DataReader {
	protected static final int EMPTY = -1;
	protected static final int PILLAR_NODE = 1;
	protected static final int TOWER_NODE = -2;
	
	private static final Logger logger = LoggerFactory.getLogger(OSMReader.class);
	
	private File osmFile = null;
	private int workerThreads = -1;
	
	// private LongIntMap osmNodeIdToInternalNodeMap;
     private TLongLongHashMap osmNodeIdToNodeFlagsMap;
     private TLongLongHashMap osmWayIdToRouteWeightMap;
	
	public OSMReader(  )
    {
       // osmNodeIdToInternalNodeMap = new GHLongIntBTree(200);
        osmNodeIdToNodeFlagsMap = new TLongLongHashMap(200, .5f, 0, 0);
        osmWayIdToRouteWeightMap = new TLongLongHashMap(200, .5f, 0, 0);
        //pillarInfo = new PillarInfo(nodeAccess.is3D(), graphStorage.getDirectory());
    }
	
	@Override
	public void readGraph() throws IOException {
		if (osmFile == null)
            throw new IllegalStateException("No OSM file specified");
		if (!osmFile.exists())
            throw new IllegalStateException("Your specified OSM file does not exist:" + osmFile.getAbsolutePath());
		
		preProcess(osmFile);
	}	
	
	void preProcess(File osmFile) {
		OSMInputFile in = null;
		try {
			in = new OSMInputFile(osmFile).setWorkerThreads(workerThreads).open();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public OSMReader setOSMFile( File osmFile ) {
		this.osmFile = osmFile;
		return this;
	}
	
	public OSMReader setWorkerThreads( int numOfWorkers ) {
		this.workerThreads = numOfWorkers;
        return this;
	}
}
