package navis.transportation.reader;

import gnu.trove.list.TLongList;
import gnu.trove.map.hash.TLongLongHashMap;

import java.io.File;
import java.io.IOException;

import navis.transportation.reader.pbf.OSMWay;
import navis.transportation.support.Helper;
import navis.transportation.support.ItfBtreeMap;
import navis.transportation.support.LongIntBTreeMap;

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
	protected static final int EDGE_NODE = 1;
	protected static final int TOP_NODE = -2;
	
	private static final Logger logger = LoggerFactory.getLogger(OSMReader.class);
	
	private File osmFile = null;
	private int workerThreads = -1;
	
	 private ItfBtreeMap osmNodeIdToBTreeMap;
	
	public OSMReader(  )
    {
		osmNodeIdToBTreeMap = new LongIntBTreeMap(200);
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
            OSMElement item;
            long tmpWayCounter = 1;
            while ((item = in.getNext()) != null)
            {
            	if (item.getType() == OSMElement.WAY) {
            		final OSMWay way = (OSMWay) item;
            		boolean valid = filterWay(way);
            		if (valid) {
            			TLongList wayNodes = way.getNodes();
            			int size = wayNodes.size();
            			for (int i = 0; i < size; i++) {
            				prepareHighwayNode(wayNodes.get(i));
            			}
            			tmpWayCounter++;
            		}
            	}
            }
            //optimize LongIntBTreeMap
            getBTreeMap().optimize();
            logger.info("Number of handeled way: " + tmpWayCounter);
            logger.info("Info of BTreeMap:  " + getBTreeMap().toString());
		} catch (IOException e) {
			throw new RuntimeException("Problem while parsing file", e);
		} finally {
			Helper.close(in);
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
	
    protected ItfBtreeMap getBTreeMap() {
    	return osmNodeIdToBTreeMap;
    }
	
	 /**
     * analyze properties (tags) wayNodes, will be filled with participating node id.
     */
    boolean filterWay( OSMWay item )
    {
        // ignore broken geometry
        if (item.getNodes().size() < 2)
            return false;
        //  40471 ways haven't got "highway" tag. and remove it
        if (!item.hasTags() || !item.hasTag("highway") )
            return false;

        return true;
    }
    
    void prepareHighwayNode( long osmId )
    {
        int tmpIndex = getBTreeMap().get(osmId);
        if (tmpIndex == EMPTY)
        {
        	getBTreeMap().put(osmId, EDGE_NODE);
        } else if (tmpIndex > EMPTY)
        {
            // mark node as tower node as it occured at least twice times
        	getBTreeMap().put(osmId, TOP_NODE);
        } 
    }
}
