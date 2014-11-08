package navis.transportation.support;

import static java.lang.Math.cos;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;
import navis.transportation.reader.OSMOffset;

public class CalOnOSM { 
	 public final static double R = 6371000; 
	 public final static double distanceTopEdge = 5;
	 public final static double distanceParts = 7; //distance between 2 parts of a segment
	 public static double calcDist( double fromLat, double fromLon, double toLat, double toLon )
	    {
			 double dLat = toRadians((toLat - fromLat));
		     double dLon = toRadians((toLon - fromLon));

		     double tmp = cos(toRadians((fromLat + toLat) / 2)) * dLon;
	         double normedDist = dLat * dLat + tmp * tmp;
	         return R * sqrt(normedDist);
	    }
	 public static OSMOffset getNewNode (double TopLat, double TopLon, double EdgeLat, double EdgeLon, double expectingDistance ) {
		 double distance = calcDist(TopLat, TopLon, EdgeLat, EdgeLon);
		 double offsetLat = (EdgeLat - TopLat)*(expectingDistance/distance);
		 double offsetLon = (EdgeLon - TopLon)*(expectingDistance/distance);
		 OSMOffset newOffset = new OSMOffset(offsetLat, offsetLon);
		 return newOffset;
	 }
}
