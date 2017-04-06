package Location;

import java.util.ArrayList;
import java.util.HashMap;

import Utils.MacRssiPair;
import Utils.Position;
import Utils.Utils;

public class TrilaterationLocationFinder implements LocationFinder {

	private HashMap<String, Position> knownLocations;
	private HashMap<String, Integer> signals;
	private Position lastloc = new Position(0, 0);

	private static final double difflimit = 10.0;
	private static final double factor = 0.5;

	public TrilaterationLocationFinder() {
		knownLocations = Utils.getKnownLocations();
		signals = new HashMap<>();
	}

	@Override
	public Position locate(MacRssiPair[] data) {
		System.out.println("Starting location turn... \n ===============================");
		// printMacs(data);
		return getPosition(data);
	}

	private Position getPosition(MacRssiPair[] data) {
		// Fill Signal Hashmap.
		for (int i = 0; i < data.length; i++) {
			if (knownLocations.containsKey(data[i].getMacAsString())) {
				signals.put(data[i].getMacAsString(), data[i].getRssi());
			}
		}
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
		System.out.println(bestNodes.toString());

		if (!bestNodes.contains(null)) {
			Position newloc = trilaterate(bestNodes.toArray(new MacRssiPair[3]));
			System.out.println(newloc.toString());
			double Xdiff = newloc.getX() - lastloc.getX();
			double Ydiff = newloc.getY() - lastloc.getY();
			if (((Xdiff <= difflimit && Xdiff >= (-1 * difflimit)) || lastloc.getX() == 0)
					&& ((Ydiff <= difflimit && Ydiff >= (-1 * difflimit)) || lastloc.getY() == 0)) {
				lastloc = newloc;
				System.out.println("normal");
				return newloc;
			} else {
				System.out.println("alternate");
				lastloc = new Position(lastloc.getX() + (factor * Xdiff), lastloc.getY() + (factor * Ydiff));
				return lastloc;
			}
		}
		System.out.println("failure");
		return lastloc;
	}

	private Position trilaterate(MacRssiPair[] data) {

		// DECLARE VARIABLES

		double[] P1 = new double[2];
		double[] P2 = new double[2];
		double[] P3 = new double[2];
		double[] ex = new double[2];
		double[] ey = new double[2];
		double[] p3p1 = new double[2];
		double jval = 0;
		double temp = 0;
		double ival = 0;
		double p3p1i = 0;
		double triptx;
		double tripty;
		double xval;
		double yval;
		double t1;
		double t2;
		double t3;
		double t;
		double exx;
		double d;
		double eyy;

		// TRANSALTE POINTS TO VECTORS
		// POINT 1
		P1[0] = knownLocations.get(data[0].getMacAsString()).getX();
		P1[1] = knownLocations.get(data[0].getMacAsString()).getY();
		// POINT 2
		P2[0] = knownLocations.get(data[1].getMacAsString()).getX();
		P2[1] = knownLocations.get(data[1].getMacAsString()).getY();
		// POINT 3
		P3[0] = knownLocations.get(data[2].getMacAsString()).getX();
		P3[1] = knownLocations.get(data[2].getMacAsString()).getY();

		// DISTANCE BETWEEN POINT 1 AND MY LOCATION
		double distance1 = signals.get(data[0].getMacAsString()) * -1;
		// DISTANCE BETWEEN POINT 2 AND MY LOCATION
		double distance2 = signals.get(data[1].getMacAsString()) * -1;
		// DISTANCE BETWEEN POINT 3 AND MY LOCATION
		double distance3 = signals.get(data[2].getMacAsString()) * -1;

		for (int i = 0; i < P1.length; i++) {
			t1 = P2[i];
			t2 = P1[i];
			t = t1 - t2;
			temp += (t * t);
		}
		d = Math.sqrt(temp);
		for (int i = 0; i < P1.length; i++) {
			t1 = P2[i];
			t2 = P1[i];
			exx = (t1 - t2) / (Math.sqrt(temp));
			ex[i] = exx;
		}
		for (int i = 0; i < P3.length; i++) {
			t1 = P3[i];
			t2 = P1[i];
			t3 = t1 - t2;
			p3p1[i] = t3;
		}
		for (int i = 0; i < ex.length; i++) {
			t1 = ex[i];
			t2 = p3p1[i];
			ival += (t1 * t2);
		}
		for (int i = 0; i < P3.length; i++) {
			t1 = P3[i];
			t2 = P1[i];
			t3 = ex[i] * ival;
			t = t1 - t2 - t3;
			p3p1i += (t * t);
		}
		for (int i = 0; i < P3.length; i++) {
			t1 = P3[i];
			t2 = P1[i];
			t3 = ex[i] * ival;
			eyy = (t1 - t2 - t3) / Math.sqrt(p3p1i);
			ey[i] = eyy;
		}
		for (int i = 0; i < ey.length; i++) {
			t1 = ey[i];
			t2 = p3p1[i];
			jval += (t1 * t2);
		}
		xval = (Math.pow(distance1, 2) - Math.pow(distance2, 2) + Math.pow(d, 2)) / (2 * d);
		yval = ((Math.pow(distance1, 2) - Math.pow(distance3, 2) + Math.pow(ival, 2) + Math.pow(jval, 2)) / (2 * jval))
				- ((ival / jval) * xval);

		t1 = 190;
		t2 = ex[0] * xval;
		t3 = ey[0] * yval;
		triptx = t1 + t2 + t3;

		t1 = 6;
		t2 = ex[1] * xval;
		t3 = ey[1] * yval;
		tripty = t1 + t2 + t3;

		if (tripty < 0) {
			tripty = tripty * -1;
		}
		if (triptx < 0) {
			triptx = triptx * -1;
		}

		return new Position(triptx, tripty);
	}
}
