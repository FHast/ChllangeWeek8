package Location;

import java.util.HashMap;

import Utils.MacRssiPair;
import Utils.Position;
import Utils.Utils;

public class HardcoreLocationFinder implements LocationFinder {

	private HashMap<String, Position> knownLocations;
	private HashMap<String, Integer> signals;
	
	public HardcoreLocationFinder() {
		knownLocations = Utils.getKnownLocations();
		signals = new HashMap<>();
	}
	
	@Override
	public Position locate(MacRssiPair[] data) {
		printMacs(data);
		return null;
	}
	
	private Position getFirstKnownFromList(MacRssiPair[] data){
		Position ret = new Position(0,0);
		// Fill Signal Hashmap.
		for(int i=0; i<data.length; i++){
			if(knownLocations.containsKey(data[i].getMacAsString())){
				signals.put(data[i].getMacAsString(), data[i].getRssi());
			}
		}
		// Calculate position...
		System.out.println(signals.toString());
		return ret;
	}
	
	private void printMacs(MacRssiPair[] data) {
		for (MacRssiPair pair : data) {
			System.out.println(pair);
		}
	}

}
