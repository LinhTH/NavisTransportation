package research.linhth;

import java.lang.invoke.ConstantCallSite;

import javax.swing.SpringLayout.Constraints;

import org.apache.log4j.BasicConfigurator;
import org.omg.CORBA.REBIND;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import research.linhth.Storage.DAType;
import research.linhth.Storage.RFDirectory;
import research.linhth.util.Helper;

public class ReadPbfFile {
	
	//private static Logger log = Logger.getLogger(ReadPbfFile.class.getName());
	
	//nơi lưu file đã được chuyển định dạng
	private String rfLocation = "";
	private String osmFile;
	private boolean fullyLoaded = false;
	
	// for OSM import
	private int workerThreads = -1;
	
	// for graph
	private DAType dataAccessType = DAType.RAM_STORE;
	
	
	public static void main( String []args ) {
		//Configure to use log4j
		BasicConfigurator.configure();
		
		String urlOsm = "/home/rimberry/research/OSM/Commonly/vietnam-latest.osm.pbf"; 
		ReadPbfFile readPbfFile = new ReadPbfFile().init(urlOsm);
		 readPbfFile.importOrLoad();
	}
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	/*
	 * Sets the folder to save readFile
	 */
	public void setReadFileLocation( String rfLocation ) {
		ensureNotLoaded();
		if ( Helper.isEmpty(rfLocation) )
			throw new IllegalArgumentException("readFile Location cannot be null");
		this.rfLocation = rfLocation;
	}
	
	
	/*
	 * Cài đặt môi trường
	 */
	public ReadPbfFile init( String url ) {
		String tmpOsmFile = url;
		
		if ( !Helper.isEmpty(tmpOsmFile) ) {
			osmFile = tmpOsmFile;	
		}
		String readFileFolder = "vietname-rf";
		if ( Helper.isEmpty(osmFile))
			throw new IllegalArgumentException("You need to specify an OSM file.");
		
		setReadFileLocation(readFileFolder);
		
		return this;
	}
	
	private void printInfo(String mess) {
		logger.info(mess);
	}
	
	/**
	 * Import provided data from disc and creates graph.
	 * Depending on the settings the resulting graph will be stored to disc so on
	 * a second call this method will only load the graph from disc which is usually a lot faster
	 */
	public ReadPbfFile importOrLoad() {
		if ( !load(rfLocation) )
		{
			printInfo("Khởi tạo lần đầu!");
			//TODO: Thuc hien tao location
		} else 
		{
			printInfo("Đã khởi tạo từ trước!");
			//TODO: load ban do da tao
		}
		return this;
	}
	
	/*
	 * Opens existing graph if 
	 */
	public boolean load(String rfLocation) {
		if ( Helper.isEmpty(rfLocation) )
			throw new IllegalStateException("read File Location is not specified. call init before");
		
		if (fullyLoaded)
			throw new IllegalStateException("graph is already successfully loaded");
		
		RFDirectory dir = new RFDirectory(rfLocation, dataAccessType);
		
		return true;
	}
	
	
	
	protected void ensureNotLoaded() {
		if ( fullyLoaded )
			throw new IllegalStateException("No configuration changes are possible after loading the graph");
	}
}
