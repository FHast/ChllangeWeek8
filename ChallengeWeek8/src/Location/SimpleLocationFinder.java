package Location;

import java.util.ArrayList;
import java.util.HashMap;
import Utils.*;

/**
 * Simple Location finder that returns the first known APs location from the
 * list of received MAC addresses
 * 
 * @author Bernd
 *
 */
public class SimpleLocationFinder implements LocationFinder {

	private HashMap<String, Position> knownLocations;
	private HashMap<String, Double> distances;
	private HashMap<String, Integer> signals;

	public SimpleLocationFinder() {
		distances = new HashMap<>();
		knownLocations = Utils.getKnownLocations();
	}

	@Override
	public Position locate(MacRssiPair[] data) {
		// printMacs(data);
		return getFirstKnownFromList(data);
	}

	/**
	 * Returns the position of the first known AP found in the list of MacRssi
	 * pairs
	 * 
	 * @param data
	 * @return
	 */
	private Position getFirstKnownFromList(MacRssiPair[] data) {
		// Distances
		for (MacRssiPair m : data) {
			double distance = Math.pow(10, (m.getRssi() + 42) / 22.5);
			distances.put(m.getMacAsString(), distance);
		}
		System.out.println(distances.toString());
		// Fill Signal Hashmap.
		for (int i = 0; i < data.length; i++) {
			if (knownLocations.containsKey(data[i].getMacAsString())) {
				signals.put(data[i].getMacAsString(), data[i].getRssi());
			}
		}
		System.out.println(signals.toString());
		// get the 3 best MacRssiPairs
		ArrayList<MacRssiPair> bestNodes = new ArrayList<>();
		for (int j = 0; j < 3; j++) {
			double max = -100;
			MacRssiPair best = null;
			for (MacRssiPair m : data) {
				String mac = m.getMacAsString();
				if (knownLocations.containsKey(mac) && !bestNodes.contains(m) && signals.get(mac) > max) {
					max = signals.get(mac);
					best = m;
				}
			}
			bestNodes.add(best);
		}

		double totaldistance = 0;
		for (MacRssiPair m : bestNodes) {
			totaldistance += distances.get(m);
		}

		Position myLoc = new Position(0, 0);
		
		for (MacRssiPair m : bestNodes) {
			String mac = m.getMacAsString();
			System.out.println("Adding influence for: " + mac);

			double distance = distances.get(mac);
			double weight = (distance / totaldistance);

			// get new X
			double apX = knownLocations.get(mac).getX();
			double currentX = myLoc.getX();
			double newX = currentX + (weight * (apX - currentX));

			System.out.println("NewX: " + newX);
			// get new Y
			double apY = knownLocations.get(mac).getY();
			double currentY = myLoc.getY();
			double newY = currentY + (weight * (apY - currentY));

			System.out.println("newY: " + newY);
			// update myLoc
			myLoc = new Position(newX, newY);

		}

		return myLoc;
	}

	/**
	 * Outputs all the received MAC RSSI pairs to the standard out This method
	 * is provided so you can see the data you are getting
	 * 
	 * @param data
	 */
	private void printMacs(MacRssiPair[] data) {
		for (MacRssiPair pair : data) {
			System.out.println(pair);
		}
	}

}
