package navis.transportation.reader;

public class OSMOffset {
	private final double offsetLat;
	private final double offsetLon;
	
	public OSMOffset(double offsetLat, double offsetLon) {
		this.offsetLat = offsetLat;
		this.offsetLon = offsetLon;
	}
	
	public double getOffsetLat() {
		return offsetLat;
	}
	
	public double getOffsetLon() {
		return offsetLon;
	}
}
