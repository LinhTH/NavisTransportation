package navis.transportation.reader.pbf;

import navis.transportation.reader.OSMElement;

public interface ItfSink 
{
	void process( OSMElement item );
	void complete();
}
 
