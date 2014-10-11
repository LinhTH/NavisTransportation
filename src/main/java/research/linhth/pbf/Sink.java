package research.linhth.pbf;

import research.linhth.OSM.OSMElement;
 
/*
 * @author: graphhopper.com
 */
public interface Sink 
{
	void process( OSMElement item );
	void complete();
}
 