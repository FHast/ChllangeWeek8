package Location;

import java.util.HashMap;

import Utils.MacRssiPair;
import Utils.Position;
import Utils.Utils;

public class HardcoreLocationFinder implements LocationFinder {

	private HashMap<String, Position> knownLocations;
	private HashMap<String, Integer> signals;
	private HashMap<String, Double> distances;

	public HardcoreLocationFinder() {
		distances = new HashMap<>();
		knownLocations = Utils.getKnownLocations();
		// remove other rooms aps
		knownLocations.remove("64:D9:89:43:CF:E0");
		knownLocations.remove("64:D9:89:43:D4:F0");
		knownLocations.remove("64:D9:89:43:CD:60");
		signals = new HashMap<>();
	}

	@Override
	public Position locate(MacRssiPair[] data) {
		System.out.println("Starting location turn... \n ===============================");
		// printMacs(data);
		return getPosition(data);
	}

	private Position getPosition(MacRssiPair[] data) {
		for (MacRssiPair m : data) {
			double distance = Math.pow(10, (m.getRssi()+42)/22.5);
			distances.put(m.getMacAsString(), distance);
		}
		
		Position myLoc = new Position(0, 0);
		// Fill Signal Hashmap.
		for (int i = 0; i < data.length; i++) {
			if (knownLocations.containsKey(data[i].getMacAsString())) {
				signals.put(data[i].getMacAsString(), data[i].getRssi());
			}
		}
		// Print determined signals
		System.out.println(signals.toString());
		// Strongest AP
		String bestAP = "";
		double max = -100;
		double totalSSI = 0;
		for (String s : signals.keySet()) {
			if (signals.get(s) > max) {
				max = signals.get(s);
				bestAP = s;
			}
			totalSSI += 100 + signals.get(s);
		}
		// print result
		System.out.println("Strongest Signal from: " + bestAP + " with: " + max);
		// get strongest AP position
		Position bestAPloc = knownLocations.get(bestAP);
		// Set out positon to this
		myLoc = bestAPloc;
		// take other AP into account
		
		System.out.println(myLoc);
		for (String s : signals.keySet()) {
			if (!s.equals(bestAP)) {
				System.out.println("Adding influence for: " + s);

				double ssi = 100 + signals.get(s);
				double weight = (ssi / totalSSI);

				System.out.println(
						"SSI: " + ssi + "/ total: " + totalSSI + "/ weight: " + weight + "(" + ssi / totalSSI + ")");
				// get new X
				double apX = knownLocations.get(s).getX();
				double currentX = myLoc.getX();
				double newX = currentX + (weight * (apX - currentX));

				System.out.println("NewX: " + newX);
				// get new Y
				double apY = knownLocations.get(s).getY();
				double currentY = myLoc.getY();
				double newY = currentY + (weight * (apY - currentY));

				System.out.println("newY: " + newY);
				// update myLoc
				myLoc = new Position(newX, newY);
			}
		}

		return myLoc;
	}

	private void printMacs(MacRssiPair[] data) {
		for (MacRssiPair pair : data) {
			System.out.println(pair);
		}
	}

}
