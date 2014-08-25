package org.raz.pdb.graphics;

import java.util.ArrayList;

public class Atom {
	public String resn = "", elem = "", chain = "", atom = "", ss = "c";
	public float x = 0, y = 0, z = 0, b = 0;
	public int resi, serial;
	public Color color = ChemDatabase.defaultColor;
	public boolean hetflag;
	public ArrayList<Integer> bonds = null, bondOrder = null;

	public final int isConnected(Atom atom) {
		int s = -1;
		if (bonds != null && (s = bonds.indexOf(atom.serial)) != -1) return bondOrder.get(s);

		//if (this.protein.sdf && (atom1.hetflag || atom2.hetflag)) return 0; // CHECK: or should I ?
		
		float distSquared = (x - atom.x) * (x - atom.x) + (y - atom.y) * (y - atom.y) + (z - atom.z) * (z - atom.z);
		if (Float.isNaN(distSquared)) return 0;
		if (distSquared < 0.5) return 0;
		if (distSquared > 1.3 && (atom.elem.equals("H") || elem.equals("H"))) return 0;
		if (distSquared < 3.42 && (atom.elem.equals("S") || elem.equals("S"))) return 1;
		if (distSquared > 2.78) return 0;
//		Log.d("isConnected", "bond between " + serial + " and " + atom.serial + ", dist^2 = " + distSquared);		
		return 1;
	}
}
