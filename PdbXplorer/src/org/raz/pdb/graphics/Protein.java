

package org.raz.pdb.graphics;

import java.util.HashMap;

public class Protein {
	public float a, b, c, alpha, beta, gamma;
	public float ax, ay, az, bx, by, bz, cx, cy, cz;
	public String spacegroup;
	public boolean sdfFile = false;
	public Atom [] atoms = new Atom[100001];
	public HashMap<Integer, float[]> symmetryMatrices = new HashMap<Integer, float[]>();
	public HashMap<Integer, float[]> biomtMatrices = new HashMap<Integer, float[]>();
}
