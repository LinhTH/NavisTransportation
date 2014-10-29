package navis.transportation.reader.pbf;

import navis.transportation.reader.OSMElement;

public interface ItfProcessBox
{
	void process( OSMElement item );
	void complete();
}
 
