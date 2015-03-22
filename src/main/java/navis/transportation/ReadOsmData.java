package navis.transportation;

import java.io.File;
import java.io.IOException;

import navis.transportation.reader.DataReader;
import navis.transportation.reader.OSMReader;
import navis.transportation.support.Helper;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadOsmData {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private String locationFolder = "";
	private String osmFile;
	private int workerThreads = -1;
	
	public static void main( String []args ) {
		//Configure to use log4j
		BasicConfigurator.configure();
		
		/*String urlOsm = "/home/rimberry/research/OSM/Commonly/vietnam-latest.osm.pbf"; */
		String urlToOsmFile = CONF.FILEINPUT;
		ReadOsmData readOsmData = new ReadOsmData();
		readOsmData.init(urlToOsmFile);
		readOsmData.importOrLoad();
	}
	
	
	/**
	 * set a path to osm file
	 * @param urlToOsmFile
	 */
	public void init(String urlToOsmFile) {
		
		String urlToFile = urlToOsmFile;
		
		if (!Helper.isEmpty(urlToFile)) {
			this.osmFile = urlToFile;
		} else {
			throw new IllegalArgumentException("You need to specify the url of an OSM file.");
		}	
		
		//Folder contains the resulting file.
		this.locationFolder = "";
	}
	
	/**
	 *  Import provided data from disc.
	 *  The results will be stored to disc ( i haven't set up it ).
	 */
	public void importOrLoad() {
		if (!load(locationFolder)) {
			process();
		} else {
			
		}	
	}
	
	/**
	 *  Import the resulting data
	 *  @return true if it exists
	 */
	private boolean load(String location) {
		//TODO: Set up it later.
		return false;
	}
	
	private void process() {
		try {
			importData();
		} catch (IOException e) {
			throw new RuntimeException("Cannot parse OSM file " + getOSMFile(), e);
		}
	}
	
	private void importData() throws IOException {
		if (osmFile == null) 
			throw new IllegalStateException("Cannot import from OSM file as it wasn't specified! ");
		
		DataReader reader = createReader();
		reader.readGraph();
	}
	
	protected DataReader createReader() {
		return initOSMReader( new OSMReader() );
	}
	
	protected OSMReader initOSMReader( OSMReader reader ) {
		logger.info("start reading " + osmFile);
		File osmTmpFile = new File(osmFile);
		
		return reader.setOSMFile(osmTmpFile).
        		setWorkerThreads(workerThreads);
	}	
	
	private String getOSMFile() {
		return osmFile;
	}
	
	
}






