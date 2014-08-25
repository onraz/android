package org.raz.pdb.graphics;

import java.util.ArrayList;

public class PDBReader {
	public Protein protein = new Protein();
	public Atom [] atoms = protein.atoms; 
	public ArrayList<int[]> sheets = new ArrayList<int[]>(); // chainID(in ASCII), startResi, endResi
	public ArrayList<int[]> helices = new ArrayList<int[]>();
	
	static int safeParseInt(String s, int from, int len) {
//		if (s.length() < from - 1) return 0;
//		if (s.length() < from + len - 1) len = s.length() - from;
//		s = s.substring(from, from + len);
		try {
			return Integer.parseInt(s.substring(from, from + len).trim());
		} catch (Exception e) {
			return 0;
		}
	}
	
	static float safeParseFloat(String s, int from, int len) {
//		if (s.length() < from - 1) return 0;
//		if (s.length() < from + len - 1) len = s.length() - from;
//		s = s.substring(from, from + len);
		try {
			return Float.parseFloat(s.substring(from, from + len).trim());
		} catch (Exception e) {
			return 0;
		}
	}
	
	static String safeParseString(String s, int from, int len) {
		if (s.length() < from - 1) return "";
		if (s.length() < from + len - 1) len = s.length() - from;
		s = s.substring(from, from + len);
		return s.trim();
	}
	
	public void parseOneLine(String line) {
		if (line.length() < 6) return;
		
		String recordName = line.substring(0, 6);
		if (recordName.equals("ATOM  ") || recordName.equals("HETATM")) {
			String atomName = safeParseString(line, 12,4);

			Atom atom = new Atom();
			atom.serial = safeParseInt(line, 6, 5);
			atom.atom = atomName;
			String altLoc = safeParseString(line, 16, 1);
			if (!altLoc.equals("") && !altLoc.equals("A")) return;
			atom.resn = safeParseString(line, 17, 3);
			atom.chain = safeParseString(line, 21, 1);
			atom.resi = safeParseInt(line, 22, 5); 
			atom.x = safeParseFloat(line, 30, 8);
			atom.y = safeParseFloat(line, 38, 8);
			atom.z = safeParseFloat(line, 46, 8);
			atom.b = safeParseFloat(line, 60, 8);
			atom.elem = safeParseString(line, 76, 2);
			if (atom.elem.length() == 0) atom.elem = atom.atom;
			if (recordName.equals("HETATM")) atom.hetflag = true;
			else atom.hetflag = false;
			atoms[atom.serial] = atom;
		} else if (recordName.equals("SHEET ")) {
			int startChain = safeParseString(line, 21, 1).charAt(0);
			int startResi = safeParseInt(line, 22, 4);
			// endChain = line.substr(32, 1);
			int endResi = safeParseInt(line, 33, 4);
			sheets.add(new int[]{startChain, startResi, endResi});
		} else if (recordName.equals("CONECT")) {
			int from = safeParseInt(line, 6, 5);
			int to1 = safeParseInt(line, 11, 5);
			int to2 = safeParseInt(line, 16, 5);
			int to3 = safeParseInt(line, 21, 5);
			int to4 = safeParseInt(line, 26, 5);
			if (atoms[from] == null) return;
			if (atoms[from].bonds == null) atoms[from].bonds = new ArrayList<Integer>();
			if (atoms[from].bondOrder == null) atoms[from].bondOrder = new ArrayList<Integer>();
			if (to1 != 0) {atoms[from].bonds.add(to1); atoms[from].bondOrder.add(1);} // As atom serial starts from 0, this is valid.
			if (to2 != 0) {atoms[from].bonds.add(to2); atoms[from].bondOrder.add(1);}
			if (to3 != 0) {atoms[from].bonds.add(to3); atoms[from].bondOrder.add(1);}
			if (to4 != 0) {atoms[from].bonds.add(to4); atoms[from].bondOrder.add(1);}
		} else if (recordName.equals("HELIX ")) {
			int startChain = safeParseString(line, 19, 1).charAt(0);
			int startResi = safeParseInt(line, 21, 4);
			// endChain = line.substr(31, 1);
			int endResi = safeParseInt(line, 33, 4);
			helices.add(new int[]{startChain, startResi, endResi});
		} else if (recordName.equals("CRYST1")) {
			protein.a = safeParseFloat(line, 6, 9);
			protein.b = safeParseFloat(line, 15, 9);
			protein.c = safeParseFloat(line, 24, 9);
			protein.alpha = safeParseFloat(line, 33, 7);
			protein.beta = safeParseFloat(line, 40, 7);
			protein.gamma = safeParseFloat(line, 47, 7);
			protein.spacegroup = safeParseString(line, 55, 11);
			defineCell();
		}  else if (recordName.equals("REMARK")) {
			String type = safeParseString(line, 13, 5);
			if (type.equals("BIOMT")) {
				int n = safeParseInt(line, 18, 1);
				int m = safeParseInt(line, 21, 2);
				float mat[] = protein.biomtMatrices.get(m);
				if (mat == null) mat = new float[16];
				mat[4 * n - 4] = safeParseFloat(line, 24, 9);
				mat[4 * n - 3] = safeParseFloat(line, 34, 9);
				mat[4 * n - 2] = safeParseFloat(line, 44, 9);
				mat[4 * n - 1] = safeParseFloat(line, 54, 10);
				mat[12] = mat[13] = mat[14] = 0; mat[15] = 1; // PDB doesn't list this row
				protein.biomtMatrices.put(m, mat);
			} else if (type.equals("SMTRY")) {
				int n = safeParseInt(line, 18, 1);
				int m = safeParseInt(line, 21, 2);
				float mat[] = protein.symmetryMatrices.get(m);
				if (mat == null) mat = new float[16];
				mat[4 * n - 4] = safeParseFloat(line, 24, 9);
				mat[4 * n - 3] = safeParseFloat(line, 34, 9);
				mat[4 * n - 2] = safeParseFloat(line, 44, 9);
				mat[4 * n - 1] = safeParseFloat(line, 54, 10);
				mat[12] = mat[13] = mat[14] = 0; mat[15] = 1; // PDB doesn't list this row
				protein.symmetryMatrices.put(m, mat);
			}
		}
	}

	public void parse2ndPass() {
		// Assign secondary structures
		
		for (int i = 0; i < 100001; i++) {
			Atom atom = atoms[i]; if (atom == null) continue;
			
			boolean found = false;
			for (int j = 0, lim = sheets.size(); j < lim && !found; j++) {
				int s[] = sheets.get(j);
				if (atom.resi < s[1]) continue;		
				if (atom.resi > s[2]) continue;
				if (atom.chain.charAt(0) != s[0]) continue;
				atom.ss = "s";
				found = true;
			}
			for (int j = 0, lim = helices.size(); j < lim && !found; j++) {
				int h[] = helices.get(j);
				if (atom.resi < h[1]) continue;		
				if (atom.resi > h[2]) continue;
				if (atom.chain.charAt(0) != h[0]) continue;
				atom.ss = "h";
				found = true;
			}
		} 
	}
	
	public void parsePDB2(String str) {
		String[] lines = str.split("\n");

//		Log.d("PDBReader", "Total line:" + lines.length);
		for (int i = 0; i < lines.length; i++) {
			parseOneLine(lines[i]);
		}
	}
	
	public void parsePDB(String str) {
		parsePDB2(str);
		parse2ndPass();
	}

	private void defineCell() {
		if (protein.a == 0) return;

		protein.ax = protein.a;
		protein.ay = 0;
		protein.az = 0;
		protein.bx = (float)(protein.b * Math.cos(Math.PI / 180.0 * protein.gamma));
		protein.by = (float)(protein.b * Math.sin(Math.PI / 180.0 * protein.gamma));
		protein.bz = 0;
		protein.cx = (float)(protein.c * Math.cos(Math.PI / 180.0 * protein.beta));
		protein.cy = (float)(protein.c * (Math.cos(Math.PI / 180.0 * protein.alpha) - 
				Math.cos(Math.PI / 180.0 * protein.gamma) 
				* Math.cos(Math.PI / 180.0 * protein.beta)
				/ Math.sin(Math.PI / 180.0 * protein.gamma)));
		protein.cz = (float)(Math.sqrt(protein.c * protein.c * Math.sin(Math.PI / 180.0 * protein.beta)
				* Math.sin(Math.PI / 180.0 * protein.beta) - protein.cy * protein.cy));
	}
}
