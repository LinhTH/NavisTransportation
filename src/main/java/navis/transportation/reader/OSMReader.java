package navis.transportation.reader;

import gnu.trove.list.TLongList;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import navis.transportation.CONF;
import navis.transportation.reader.pbf.OSMWay;
import navis.transportation.support.CalOnOSM;
import navis.transportation.support.Helper;
import navis.transportation.support.ItfBtreeMap;
import navis.transportation.support.LongIntBTreeMap;
import navis.transportation.support.StopWatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static navis.transportation.support.CalOnOSM.calcDist;
import static navis.transportation.support.CalOnOSM.getNewNode;
/**
 * Parsing an OSM pbf file.
 * <p/>
 * 1. a) Reads all ways from OSM pbf file and stores all associated node ids in osmNodeIdToIndexMap. If a
 * node occurs once it is a pillar node and if more it is a tower node (it represents for the junction), otherwise
 * osmNodeIdToIndexMap returns EMPTY.
 * <p>
 */
/*
 * Phần bao của hồ chí minh
 * 10.870566, 106.729768 N-E
 * 10.708010, 106.569436 S-W
 */

public class OSMReader implements DataReader {
	protected static final int EMPTY = -1;
	protected static final int EDGE_NODE = 1;
	protected static final int TOP_NODE = -2;
	
/*	private final double maxHCMlatitude = 10.870566;
	private final double minHCMlatitude = 10.708010;
	private final double maxHCMlongtitude = 106.729768;
	private final double minHCMlongtitude = 106.569436;*/
	
	private String urlOutput = CONF.FILEOUTPUT;
	
	private static final Logger logger = LoggerFactory.getLogger(OSMReader.class);
	
	private File osmFile = null;
	private int workerThreads = -1;
	
	private ItfBtreeMap osmNodeIdToBTreeMap;
	private Queue<OSMWay> osmWayToQueue;
	private TLongObjectHashMap<OSMNode> osmNodeIdToLOMap;

	
	public OSMReader(  )
    {
		osmNodeIdToBTreeMap = new LongIntBTreeMap(200);
		osmNodeIdToLOMap = new TLongObjectHashMap<OSMNode>(200, 0.5f);
		osmWayToQueue = new LinkedList<OSMWay>();
    }
	
	@Override
	public void readGraph() throws IOException {
		if (osmFile == null)
            throw new IllegalStateException("No OSM file specified");
		if (!osmFile.exists())
            throw new IllegalStateException("Your specified OSM file does not exist:" + osmFile.getAbsolutePath());
		StopWatch swp = new StopWatch().start();
		preProcess(osmFile);
		logger.info("preProcess took: " + swp.stop().getSeconds() + " s");
		
		swp.restart().start();
		process();
		logger.info("Process took: " + swp.stop().getSeconds() + " s");
	}	

	
	void preProcess(File osmFile) {
		OSMInputFile in = null;
		try {
			in = new OSMInputFile(osmFile).setWorkerThreads(workerThreads).open();
            OSMElement item;
            long tmpWayCounter = 0;
            long nodeCounter = 0;
            while ((item = in.getNext()) != null)
            {
            	if (item.getType() == OSMElement.WAY) {
            		final OSMWay way = (OSMWay) item;
            		boolean valid = filterWay(way, way.getId());
            		if (valid) {
            			TLongList wayNodes = way.getNodes();
            			int size = wayNodes.size();
            			for (int i = 0; i < size; i++) {
            				prepareHighwayNode(wayNodes.get(i));
            			}
            			tmpWayCounter++;
            			getWayQueue().add(way);
            		}
            	}
            	
            	//Store all nodes to HashMap
            	if (item.getType() == OSMElement.NODE) {
            		final OSMNode node = (OSMNode) item;
            		getNodeLOMap().put(node.getId(), node);
            		nodeCounter++;
            	}
            }
      
            //optimize LongIntBTreeMap
            getBTreeMap().optimize();
            getNodeLOMap().compact();
            logger.info(tmpWayCounter + " ways and " + nodeCounter + " nodes were handled!");
            logger.info("Info of BTreeMap:  " + getBTreeMap().toString());
		} catch (IOException e) {
			throw new RuntimeException("Problem while parsing file", e);
		} finally {
			Helper.close(in);
		}
	}
	

	void process() throws IOException {
		FileWriter fw = new FileWriter(urlOutput, false);
		fw.write("points = [");
		int count = 0;
		Iterator<OSMWay> wayIterator = getWayQueue().iterator();
		while (wayIterator.hasNext()) {
			count++;
			final OSMWay way = (OSMWay) wayIterator.next();
			TLongList wayNodes = way.getNodes();
			int size = wayNodes.size();
			double lastLon = 0, lastLat = 0;
			boolean lastTopNode = false;
			long lastnodeid = 0;
			int nameGoup = 1;
			
			for (int i = 0; i < size; i++) {
				long nodeId = wayNodes.get(i);
				OSMNode node = getNodeLOMap().get(nodeId);
				
				if (node.getLat() < CONF.minHCMlatitude || node.getLat() > CONF.maxHCMlatitude ||
					node.getLon() < CONF.minHCMlongtitude || node.getLon() > CONF.maxHCMlongtitude)
				{
					break;
				}
				
				//	System.out.println(nodeId + " " + isTopNode(nodeId) + " " + node.getLat() + " " + node.getLon());	
				if (lastLat == 0) {
					lastLat = node.getLat();
					lastLon = node.getLon();
					lastTopNode = isTopNode(nodeId);
					lastnodeid = nodeId;
					if (!lastTopNode) {
							//fw.write("\n{ lat: "+ lastLat +", lon: "+ lastLon + ", group: " + nameGoup + "},");
							fw.write( way.getId() +"\t" + nameGoup + "\t" + lastLat + "\t" + lastLon + "\n");
					}
					continue;
				}
				
				//remove all same node
				if (node.getLat() == lastLat && node.getLon() == lastLon)
					continue;
				
				double currentLat = node.getLat();
				double currentLon = node.getLon();
				boolean currentTopNode = isTopNode(nodeId);
				long currentnodeid = nodeId;
				
				//2 temp node will represent for start node and end node in order to divide a segment into several parts 
				double headLat = lastLat;
				double headLon = lastLon;
				double tailLat = currentLat;
				double tailLon = currentLon;
				
				if (lastTopNode ) {
					OSMOffset offset = getNewNode(lastLat, lastLon, currentLat, currentLon, CalOnOSM.distanceTopEdge);
					//update head Node 
					headLat = compactDouble(lastLat + offset.getOffsetLat());
					headLon = compactDouble(lastLon + offset.getOffsetLon());
					
				//	fw.write("\n{ lat: "+ headLat +", lon: "+ headLon + ", group: " + nameGoup + " },");					
					fw.write( way.getId() +"\t" + nameGoup + "\t" + headLat + "\t" + headLon + "\n");
					
				//	System.out.println( way.getId() +"\t" + nameGoup + "\t" + compactDouble(lastLat + offset.getOffsetLat()) + "\t" +compactDouble(lastLon + offset.getOffsetLon()) + "\n");
					//System.out.println( way.getId() +"\t" + nameGoup + "\t" + Double.parseDouble(compactDouble(lastLat + offset.getOffsetLat())) + "\t" + Double.parseDouble(compactDouble(lastLon + offset.getOffsetLon()) + "\n"));	
				}
				if (currentTopNode){
					OSMOffset offset = getNewNode(currentLat, currentLon, lastLat, lastLon, CalOnOSM.distanceTopEdge);
					//update tail Node 
					tailLat = compactDouble(currentLat + offset.getOffsetLat());
					tailLon = compactDouble(currentLon + offset.getOffsetLon());
					
					//System.out.println( lastnodeid + ", " + currentnodeid + ", " + way.getId() +"\t" + offset.getOffsetLat() + "\t" + offset.getOffsetLon() + "\t" + currentLon + ", " + currentLat + ", " +  lastLon + ", " + lastLat);
				//	System.out.println( way.getId() +"\t" + nameGoup + "\t" + Double.parseDouble(compactDouble(currentLat + offset.getOffsetLat())) + "\t" + Double.parseDouble(compactDouble(currentLon + offset.getOffsetLon()) + "\n"));
				}
				
				
				//Add some new node in segment
				while (calcDist(headLat, headLon, tailLat, tailLon) >= 7.0) {
					OSMOffset offset = getNewNode(headLat, headLon, tailLat, tailLon, CalOnOSM.distanceParts);
					headLat = compactDouble(headLat + offset.getOffsetLat());
					headLon = compactDouble(headLon + offset.getOffsetLon());
					
					//fw.write("\n{ lat: "+ headLat +", lon: "+ headLon + ", group: " + nameGoup + " },");
					fw.write( way.getId() +"\t" + nameGoup + "\t" + headLat + "\t" + headLon + "\n");
				}
				
				//fw.write("\n{ lat: "+ tailLat +", lon: "+ tailLon + ", group: " + nameGoup + " },");
				fw.write( way.getId() +"\t" + nameGoup + "\t" + tailLat + "\t" + tailLon + "\n");
				
				//next segment
				if (currentTopNode) 
					nameGoup++;

				lastLat = currentLat;
				lastLon = currentLon;
				lastTopNode = currentTopNode;	
				lastnodeid = currentnodeid;
			}
		}
		fw.write("\n];");
		fw.close();
	}
	
	public OSMReader setOSMFile( File osmFile ) {
		this.osmFile = osmFile;
		return this;
	}
	
	/**
	 * @return true : if node is top node.
	 */
	private boolean isTopNode(long nodeId) {
		return getBTreeMap().get(nodeId) == TOP_NODE;
	}
	
	public OSMReader setWorkerThreads( int numOfWorkers ) {
		this.workerThreads = numOfWorkers;
        return this;
	}
	
    protected ItfBtreeMap getBTreeMap() {
    	return osmNodeIdToBTreeMap;
    }
    
    protected TLongObjectHashMap<OSMNode> getNodeLOMap() {
    	return osmNodeIdToLOMap;
    }
    
    protected Queue<OSMWay> getWayQueue() {
    	return osmWayToQueue;
    }
    
    /**
     * a.XXXXXX...XX -> a.XXXXXXX
     */
    private double compactDouble(double coor) {
    	DecimalFormat df = new DecimalFormat("#.0000000");
    	return Double.parseDouble(df.format(coor));
    }
	
	 /**
     * analyze properties (tags) wayNodes, will be filled with participating node id.
     */
    boolean filterWay( OSMWay item, long wayId )
    {
        // ignore broken geometry
        if (item.getNodes().size() < 2)
            return false;

        //  45434 ways haven't got "highway" tag. and remove it
        if (!item.hasTags() || !item.hasTag("highway") || item.hasTag("highway", "footway") || 
        		 item.hasTag("highway", "path") || item.hasTag("highway", "service") ||item.hasTag("highway", "pedestrian")) {
            return false;
        }

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