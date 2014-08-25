

package org.raz.pdb.graphics;

import java.util.ArrayList;

public class SDFReader {
	public Protein protein = new Protein();
	public Atom [] atoms = protein.atoms; 

	public void parseSDF(String str) {
		String[] lines = str.split("\n");

		protein.sdfFile = true;
		if (lines.length < 4) return;
		
		int atomCount = PDBReader.safeParseInt(lines[3], 0, 3);
		if (atomCount <= 0) return;
		
		int bondCount = PDBReader.safeParseInt(lines[3], 3, 3);
		int offset = 4;
		if (lines.length < 4 + atomCount + bondCount) return;
		
		for (int i = 1; i <= atomCount; i++) {
			String line = lines[offset++];
			Atom atom = new Atom();
			atom.serial = i;
			atom.x = PDBReader.safeParseFloat(line, 0, 10);
			atom.y = PDBReader.safeParseFloat(line, 10, 10);
			atom.z = PDBReader.safeParseFloat(line, 20, 10);
			atom.hetflag = true;
			atom.atom = atom.elem = PDBReader.safeParseString(line, 30, 3);
			atom.bonds = new ArrayList<Integer>();
			atom.bondOrder = new ArrayList<Integer>();
			atoms[i] = atom;
		}
		for (int i = 1; i <= bondCount; i++) {
			String line = lines[offset++];
			int from = PDBReader.safeParseInt(line, 0, 3);
			int to =  PDBReader.safeParseInt(line, 3, 3);
			int order =  PDBReader.safeParseInt(line, 6, 3);
			atoms[from].bonds.add(to);
			atoms[from].bondOrder.add(order);
			atoms[to].bonds.add(from);
			atoms[to].bondOrder.add(order);
		}
	}
}
