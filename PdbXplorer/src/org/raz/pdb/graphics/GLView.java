package org.raz.pdb.graphics;

import java.util.ArrayList;
import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;

@SuppressWarnings("unchecked")
class GLView implements GLSurfaceView.Renderer {
	Renderable scene;

	public float objX, objY, objZ;
	public float cameraZ, slabNear, slabFar, FOV = 20, maxD;
	public Quaternion rotationQ;
	private Atom [] atoms;
	private Protein protein;
	public int width, height;
	public boolean fogEnabled = false;

	public int proteinMode = 0; // 0 - ribbon, 1 - trace, 2 - strand, 3 - tube
	public int hetatmMode = 2; // 0 - sphere, 1 - stick, 2 - line
	public int nucleicAcidMode = 0; // 0 - line, 1 - polygon
	public boolean showSidechain = false;
	public boolean showSolvent = false;
	public boolean showUnitcell = false;
	public int symmetryMode = 0; // 0 - none, 1 - biomt, 2 - packing
	public int colorMode = 0; // 0 - chainbow, 1 - chain, 2 - ss, 3 - polar/nonpolar, 4 - b factor

	public float sphereRadius = 1.5f; 
	public float cylinderRadius = 0.2f;
	public float lineWidth = 0.5f;
	public float curveWidth = 3.0f;

	public boolean isMoving = false;
	public String saveTo = null;


	public GLView() {
		objX = 0; objY = 0; objZ = 0;
		slabNear = -200; slabFar = 200;
		cameraZ = -300;
		rotationQ = new Quaternion(1, 0, 0, 0);

		scene = new Renderable();
	}

	public void setProtein(Protein p) {
		if (p == null) return;

		protein = p;
		atoms = p.atoms;

		zoomInto(getAll());
	}

	public void zoomInto(ArrayList<Integer> atomlist) {
		float extent[] = getExtent(atomlist);

		objX = -(extent[0] + extent[3]) / 2;
		objY = -(extent[1] + extent[4]) / 2;
		objZ = -(extent[2] + extent[5]) / 2;

		float x = extent[3] - extent[0], y = extent[4] - extent[1], z = extent[5] - extent[2]; 
		maxD = (float)Math.sqrt(x * x + y * y + z * z);
		if (maxD < 25) maxD = 25;

		slabNear = -maxD / 1.9f;
		slabFar = maxD / 3;

		cameraZ = -(float)(maxD * 0.5 / Math.tan(Math.PI / 180.0 * FOV / 2));
	}

	public void drawAxis(float len, float radius) {
		Renderable cylinder1 = new Cylinder(0, 0, 0, len, 0, 0, radius, new Color(1, 0, 0, 1)); // red, X
		Renderable cylinder2 = new Cylinder(0, 0, 0, 0, len, 0, radius, new Color(0, 1, 0, 1)); // green, Y
		Renderable cylinder3 = new Cylinder(0, 0, 0, 0, 0, len, radius, new Color(0, 0, 1, 1)); // blue, Z
		scene.children.add(cylinder1);
		scene.children.add(cylinder2);
		scene.children.add(cylinder3);
		Renderable sphere = new VBOSphere(0, 0, 0, radius, new Color(1, 1, 1, 1));
		scene.children.add(sphere);
	} 

	public void prepareScene() {
		scene = new Renderable();

		if (protein == null) return;

		ArrayList<Integer> all = getAll();
		ArrayList<Integer> allHet = getHetatms(all);
		ArrayList<Integer> hetatm = removeSolvents(allHet);
		colorByAtom(all, null);

		switch (colorMode) {
		case 0:
			colorChainbow(all);
			break;
		case 1:
			colorByChain(all);
			break;
		case 2:
			colorByStructure(all, new Color(0xCC00CC), new Color(0x00CCCC));
			break;
		case 3:
			colorByPolarity(all, new Color(0xcc0000), new Color(0xcccccc));
			break;
		case 4:
			colorByBFactor(all);
			break;
		}

		switch (proteinMode) {
		case 0:
			drawCartoon(scene, all, 5);
			drawNucleicAcidCartoon(scene, all, 5);
			break;
		case 1:
			drawMainchainCurve(scene, all, curveWidth, "CA");
			drawMainchainCurve(scene, all, curveWidth, "O3'");
			break;
		case 2:
			drawStrand(scene, all, 5, 5, false);
			drawNucleicAcidStrand(scene, all, 5, 5, false);
			break;
		case 3:
			drawMainchainTube(scene, all, "CA");
			drawMainchainTube(scene, all, "O3'");
			break;
		}

		switch (nucleicAcidMode) {
		case 0:
			drawNucleicAcidAsLine(scene, all);
			break;
		case 1:
			drawNucleicAcidLadder(scene, all);
			break;
		}

		if (showSidechain) {
			drawBondsAsLine(scene, getSideChain(all), lineWidth);
		}

		if (showSolvent) {
			drawAtomsAsStar(scene, getNonbonded(allHet), 0.3f);
		}

		switch (hetatmMode) {
		case 0:
			drawAtomsAsVdWSphere(scene, hetatm);
			break;
		case 1:
			drawBondsAsStick(scene, hetatm, cylinderRadius, cylinderRadius);
			break;
		case 2:
			drawBondsAsLine(scene, hetatm, lineWidth * 8);
		}

		if (showUnitcell) {
			drawUnitcell(scene, lineWidth);
		}

		switch (symmetryMode) {
		case 1:
			drawSymmetryMates(scene, all, protein.biomtMatrices);
			break;
		case 2:
			drawSymmetryMatesWithTranslation(scene, all, protein.symmetryMatrices);
		}
	}

	public ArrayList<Integer> getAll() {		
		ArrayList<Integer> ret = new ArrayList<Integer>();
		if (atoms == null) return ret;

		for (int i = 0; i < 100001; i++) {
			if (atoms[i] != null) ret.add(i);
		}
		return ret;   
	}

	public float[] getExtent(ArrayList<Integer> atomlist) {
		float minx = 9999, miny = 9999, minz = 9999;
		float maxx = -9999, maxy = -9999, maxz = -9999;

		for (int i = 0, lim = atomlist.size(); i < lim; i++) {
			Atom atom = atoms[atomlist.get(i)];
			if (atom == null) continue;

			if (atom.x < minx) minx = atom.x;
			if (atom.x > maxx) maxx = atom.x;
			if (atom.y < miny) miny = atom.y;
			if (atom.y > maxy) maxy = atom.y;
			if (atom.z < minz) minz = atom.z;
			if (atom.z > maxz) maxz = atom.z;
		}

		return new float[] {minx, miny, minz, maxx, maxy, maxz};
	}

	public ArrayList<Integer> getHetatms(ArrayList<Integer> atomlist) {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		for (int i = 0, lim = atomlist.size(); i < lim; i++) {
			Atom atom = atoms[atomlist.get(i)];
			if (atom == null) continue;

			if (atom.hetflag)	ret.add(atom.serial);
		}
		return ret;   
	}

	public ArrayList<Integer> getNonbonded(ArrayList<Integer> atomlist) {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		for (int i = 0, lim = atomlist.size(); i < lim; i++) {
			Atom atom = atoms[atomlist.get(i)];
			if (atom == null) continue;

			if (atom.hetflag && (atom.bonds == null || atom.bonds.size() == 0)) ret.add(atom.serial);
		}
		return ret;   
	}

	public ArrayList<Integer> removeSolvents(ArrayList<Integer> atomlist) {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		for (int i = 0, lim = atomlist.size(); i < lim; i++) {
			Atom atom = atoms[atomlist.get(i)];
			if (atom == null) continue;

			if (!atom.resn.equals("HOH")) ret.add(atom.serial);
		}
		return ret;   
	}

	public ArrayList<Integer> getResiduesById(ArrayList<Integer> atomlist, ArrayList<Integer> resi) {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		for (int i = 0, lim = atomlist.size(); i < lim; i++) {
			Atom atom = atoms[atomlist.get(i)];
			if (atom == null) continue;

			if (resi.indexOf(atom.resi) != -1) ret.add(atom.serial);
		}
		return ret;   
	}

	public ArrayList<Integer> getSideChain(ArrayList<Integer> atomlist) {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		for (int i = 0, lim = atomlist.size(); i < lim; i++) {
			Atom atom = atoms[atomlist.get(i)];
			if (atom == null) continue;

			if (atom.hetflag) continue;
			if (atom.atom.equals("O") || atom.atom.equals("N") || atom.atom.equals("C")) continue;
			ret.add(atom.serial);
		}

		return ret;   
	}

	public ArrayList<Integer> getChain(ArrayList<Integer> atomlist, ArrayList<String> chain) {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		for (int i = 0, lim = atomlist.size(); i < lim; i++) {
			Atom atom = atoms[atomlist.get(i)];
			if (atom == null) continue;

			if (chain.indexOf(atom.chain) != -1) ret.add(atom.serial);
		}
		return ret;   
	}

	public void colorByAtom(ArrayList<Integer> atomlist, HashMap<String, Color> colors) {
		for (int i = 0, lim = atomlist.size(); i < lim; i++) {
			Atom atom = atoms[atomlist.get(i)];
			if (atom == null) continue;

			Color c = null;
			if (colors != null ) c= colors.get(atom.elem);
			if (c == null) c = ChemDatabase.getColor(atom.elem);
			atom.color = c;
		}
	}

	public void colorByStructure(ArrayList<Integer> atomlist, Color helixColor, Color sheetColor) {
		for (int i = 0, lim = atomlist.size(); i < lim; i++) {
			Atom atom = atoms[atomlist.get(i)];
			if (atom == null) continue;

			if (!atom.atom.equals("CA") || atom.hetflag) continue;
			if (atom.ss.charAt(0) == 's') atom.color = sheetColor;
			else if (atom.ss.charAt(0) == 'h') atom.color = helixColor;
		}
	}

	public void colorByChain(ArrayList<Integer> atomlist) {
		for (int i = 0, lim = atomlist.size(); i < lim; i++) {
			Atom atom = atoms[atomlist.get(i)];
			if (atom == null) continue;

			if ((!atom.atom.equals("CA") && !atom.atom.equals("O3'")) || atom.hetflag) continue;
			atom.color = new Color().setHSV((float)(atom.chain.charAt(0) % 15) / 15, 1, 0.9f);
		}
	}

	public void colorByResidue(ArrayList<Integer> atomlist, HashMap<String, Color> colorMap) {
		for (int i = 0, lim = atomlist.size(); i < lim; i++) {
			Atom atom = atoms[atomlist.get(i)];
			if (atom == null) continue;

			Color c = colorMap.get(atom.resn);
			if (c != null) atom.color = c;
		}
	}

	public void colorByBFactor(ArrayList<Integer> atomlist) {
		float minB = 1000, maxB = -1000;

		for (int i = 0, lim = atomlist.size(); i < lim; i++) {
			Atom atom = atoms[atomlist.get(i)];
			if (atom == null) continue;

			if (atom.hetflag) continue;
			if (atom.atom.equals("CA") || atom.atom.equals("O3'")) {
				if (minB > atom.b) minB = atom.b;
				if (maxB < atom.b) maxB = atom.b;		
			}
		}
		float mid = (maxB + minB) / 2, range = (maxB - minB) / 2;
		if (range < 0.01f && range > -0.01f) return;

		for (int i = 0, lim = atomlist.size(); i < lim; i++) {
			Atom atom = atoms[atomlist.get(i)];
			if (atom == null) continue;

			if (atom.hetflag) continue;
			if (atom.atom.equals("CA") || atom.atom.equals("O3'")) {
				if (atom.b < mid) { // FIXME: why must I make new Color object?
					atom.color = new Color().setHSV(0.667f, (mid - atom.b) / range, 1);
				} else {
					atom.color = new Color().setHSV(0, (atom.b - mid) / range, 1);
				}
			}
		}
	}

	public void colorByPolarity (ArrayList<Integer> atomlist, Color polar, Color nonPolar) {
		String polarResidues[] = {"ARG", "HIS", "LYS", "ASP", "GLU", "SER", "THR", "ASN", "GLN", "CYS"};
		String nonPolarResidues[] = {"GLY", "PRO", "ALA", "VAL", "LEU", "ILE", "MET", "PHE", "TYR", "TRP"};
		HashMap<String, Color> colorMap = new HashMap<String, Color>();
		for (int i = 0, lim = polarResidues.length; i < lim; i++) colorMap.put(polarResidues[i], polar);
		for (int i = 0, lim = nonPolarResidues.length; i < lim; i++) colorMap.put(nonPolarResidues[i], nonPolar);
		colorByResidue(atomlist, colorMap);
	}

	public void colorChainbow(ArrayList<Integer> atomlist) {
		int cnt = 0;

		for (int i = 0, lim = atomlist.size(); i < lim; i++) {
			Atom atom = atoms[atomlist.get(i)];
			if (atom == null) continue;

			if ((!atom.atom.equals("CA") && !atom.atom.equals("O3'")) || atom.hetflag) continue;
			cnt++;
		}

		int total = cnt;
		cnt = 0;

		for (int i = 0, lim = atomlist.size(); i < lim; i++) {
			Atom atom = atoms[atomlist.get(i)];
			if (atom == null) continue;

			if ((!atom.atom.equals("CA") && !atom.atom.equals("O3'")) || atom.hetflag) continue;
			atom.color = new Color().setHSV((float)240 / 360 * cnt / total, 1, 0.9f);
			cnt++;
		}
	}


	public void drawMainchainCurve(Renderable scene, ArrayList<Integer> atomlist, float curveWidth, String atomName) {
		ArrayList<Vector3> points = new ArrayList<Vector3>();
		ArrayList<Color> colors = new ArrayList<Color>();

		String currentChain = "A";
		int currentResi = -1;

		for (int i = 0, lim = atomlist.size(); i < lim; i++) {
			Atom atom = atoms[atomlist.get(i)];
			if (atom == null) continue;

			if ((atom.atom.equals(atomName)) && !atom.hetflag) {
				if (!currentChain.equals(atom.chain) || currentResi + 1 != atom.resi) {
					scene.children.add(new SmoothCurve(points, colors, curveWidth));
					points.clear();
					colors.clear();
				}
				points.add(new Vector3(atom.x, atom.y, atom.z));
				colors.add(atom.color);
				currentChain = atom.chain;
				currentResi = atom.resi;
			}
		}
		scene.children.add(new SmoothCurve(points, colors, curveWidth));
	}

	public void drawMainchainTube(Renderable scene, ArrayList<Integer> atomlist, String atomName) {
		ArrayList<Vector3> points = new ArrayList<Vector3>();
		ArrayList<Color> colors = new ArrayList<Color>();
		ArrayList<Float> radii = new ArrayList<Float>();

		String currentChain = "A";
		int currentResi = -1;

		for (int i = 0, lim = atomlist.size(); i < lim; i++) {
			Atom atom = atoms[atomlist.get(i)];
			if (atom == null) continue;

			if ((atom.atom.equals(atomName)) && !atom.hetflag) {
				if (!currentChain.equals(atom.chain) || currentResi + 1 != atom.resi) {
					scene.children.add(new SmoothTube(points, colors, radii));
					points.clear();
					colors.clear();
				}
				points.add(new Vector3(atom.x, atom.y, atom.z));
				colors.add(atom.color);
				radii.add((atom.b > 0) ? atom.b / 100 : 0.3f);
				currentChain = atom.chain;
				currentResi = atom.resi;
			}
		}
		scene.children.add(new SmoothTube(points, colors, radii));
	}

	public void drawCartoon(Renderable scene, ArrayList<Integer> atomlist, int div) {
		this.drawStrand(scene, atomlist, 2, div, true);
	}

	public boolean isIdentity(float[] mat) {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				if (i == j && Math.abs(mat[i * 4 + j] - 1) > 0.001) return false;
				if (i != j && Math.abs(mat[i * 4 + j]) > 0.001) return false;
			}
		}
		return true;
	}

	public void drawBondsAsStick(Renderable scene, ArrayList<Integer> atomlist, float bondR, float atomR) {
		for (int i = 0, lim = atomlist.size(); i < lim; i++) {
			Atom atom1 = atoms[atomlist.get(i)];
			if (atom1 == null) continue;
			boolean connected = false;

			for (int j = i + 1; j < i + 20 && j < lim; j++) { // FIXME
				Atom atom2 = atoms[atomlist.get(j)];
				if (atom2 == null) continue;
				if (atom1.isConnected(atom2) == 0) continue;
				connected = true;

				Renderable cylinder = new VBOCylinder(atom1.x, atom1.y, atom1.z,
						(atom1.x + atom2.x) / 2, (atom1.y + atom2.y) / 2, (atom1.z + atom2.z) / 2, bondR, atom1.color);
				scene.children.add(cylinder);

				cylinder = new VBOCylinder((atom1.x + atom2.x) / 2, (atom1.y + atom2.y) / 2, (atom1.z + atom2.z) / 2,
						atom2.x, atom2.y, atom2.z, bondR, atom2.color);
				scene.children.add(cylinder);
			}
			if (atom1.bonds == null) continue;
			for (int _j = 0, jlim = atom1.bonds.size(); _j < jlim; _j++) {
				int j = atom1.bonds.get(_j);
				if (j < i + 20) continue; // be conservative. drawing lines twice doesn't harm us.
				if (atomlist.indexOf(j) == -1) continue;
				Atom atom2 = atoms[j];
				if (atom2 == null) continue;
				Renderable cylinder = new VBOCylinder(atom1.x, atom1.y, atom1.z,
						(atom1.x + atom2.x) / 2, (atom1.y + atom2.y) / 2, (atom1.z + atom2.z) / 2, bondR, atom1.color);
				scene.children.add(cylinder);

				cylinder = new VBOCylinder((atom1.x + atom2.x) / 2, (atom1.y + atom2.y) / 2, (atom1.z + atom2.z) / 2,
						atom2.x, atom2.y, atom2.z, bondR, atom2.color);
				scene.children.add(cylinder);
			}

			if (!connected) continue;

			Renderable sphere = new VBOSphere(atom1.x, atom1.y, atom1.z, atomR, atom1.color);
			scene.children.add(sphere);
		}
	}

	protected void drawNucleicAcidLadderSub(ArrayList<Vector3> vertices, ArrayList<Vector3> normals, ArrayList<Short> faces, ArrayList<Color> colors, Color color, Vector3[] atoms) {
		//      color.r *= 0.9; color.g *= 0.9; color.b *= 0.9;
		if (atoms[0] != null && atoms[1] != null && atoms[2] != null &&
				atoms[3] != null && atoms[4] != null && atoms[5] != null) {
			Vector3 normal = Vector3.cross(new Vector3(atoms[1].x - atoms[0].x, atoms[1].y - atoms[0].y, atoms[1].z - atoms[0].z),
					new Vector3(atoms[2].x - atoms[0].x, atoms[2].y - atoms[0].y, atoms[2].z - atoms[0].z)).normalize();

			short baseFaceId = (short)vertices.size();
			for (int i = 0; i <= 5; i++) {
				vertices.add(atoms[i]);
				normals.add(normal);
				colors.add(color);
			}

			faces.add(baseFaceId); faces.add((short) (baseFaceId + 1)); faces.add((short) (baseFaceId + 2));
			faces.add(baseFaceId); faces.add((short) (baseFaceId + 2)); faces.add((short) (baseFaceId + 3));
			faces.add(baseFaceId); faces.add((short) (baseFaceId + 3)); faces.add((short) (baseFaceId + 4));
			faces.add(baseFaceId); faces.add((short) (baseFaceId + 4)); faces.add((short) (baseFaceId + 5));
		}
		if (atoms[4] != null && atoms[3] != null && atoms[6] != null &&
				atoms[7] != null && atoms[8] != null) {
			Vector3 normal = Vector3.cross(new Vector3(atoms[4].x - atoms[3].x, atoms[4].y - atoms[3].y, atoms[4].z - atoms[3].z),
					new Vector3(atoms[6].x - atoms[3].x, atoms[6].y - atoms[3].y, atoms[6].z - atoms[3].z)).normalize();

			short baseFaceId = (short)vertices.size();
			vertices.add(atoms[4]); 
			vertices.add(atoms[3]);
			vertices.add(atoms[6]);
			vertices.add(atoms[7]);
			vertices.add(atoms[8]);
			for (int i = 0; i <= 4; i++) {
				colors.add(color);
				normals.add(normal);
			}

			faces.add(baseFaceId); faces.add((short) (baseFaceId + 1)); faces.add((short) (baseFaceId + 2));
			faces.add(baseFaceId); faces.add((short) (baseFaceId + 2)); faces.add((short) (baseFaceId + 3));
			faces.add(baseFaceId); faces.add((short) (baseFaceId + 3)); faces.add((short) (baseFaceId + 4));
		}
	}

	public void drawNucleicAcidLadder(Renderable scene, ArrayList<Integer> atomlist) {
		ArrayList<Vector3> vertices = new ArrayList<Vector3>();
		ArrayList<Vector3> normals = new ArrayList<Vector3>();
		ArrayList<Short> faces = new ArrayList<Short>();
		ArrayList<Color> colors = new ArrayList<Color>();
		Color color = null;
		Vector3[] currentComponent = new Vector3[9];

		String[] baseAtoms = {"N1", "C2", "N3", "C4", "C5", "C6", "N9", "C8", "N7"};
		String currentChain = "";
		int currentResi = -1;

		for (int i = 1, lim = atomlist.size(); i < lim; i++) {
			Atom atom = atoms[atomlist.get(i)];
			if (atom == null || atom.hetflag) continue;

			if (atom.resi != currentResi || !atom.chain.equals(currentChain)) {
				drawNucleicAcidLadderSub(vertices, normals, faces, colors, color, currentComponent);
				for (int j = 0; j < 8; j++) {currentComponent[j] = null;}
			}
			int pos = -1;
			for (int j = 0; j < 8; j++) {
				if (baseAtoms[j].equals(atom.atom)) {
					pos = j;
					break;
				}
			}
			if (pos != -1) currentComponent[pos] = new Vector3(atom.x, atom.y, atom.z);
			if (atom.atom.equals("O3'")) color = atom.color;
			currentResi = atom.resi; currentChain = atom.chain;
		}
		drawNucleicAcidLadderSub(vertices, normals, faces, colors, color, currentComponent);

		Renderable renderable = new Renderable();
		renderable.colorBuffer = Geometry.colorsToFloatBuffer(colors, 1);
		renderable.vertexColors = true;
		renderable.faceBuffer = Geometry.getShortBuffer(faces);
		renderable.vertexBuffer = Geometry.getFloatBuffer(vertices);
		renderable.vertexNormalBuffer = Geometry.getFloatBuffer(normals);
		scene.children.add(renderable);
	}

	protected void drawBondsAsLineSub(ArrayList<Vector3> points, ArrayList<Color> colors, Atom atom1, Atom atom2, int order) {
		Vector3 midpoint = new Vector3((atom1.x + atom2.x) / 2,
				(atom1.y + atom2.y) / 2, (atom1.z + atom2.z) / 2);
		float dot = 0, dx = 0, dy = 0, dz = 0;
		if (order > 1) { // Find the bond plane. TODO: Find inner side of a ring
			Vector3 axis = new Vector3(atom1.x - atom2.x, atom1.y - atom2.y, atom1.z - atom2.z);
			Atom found = null;
			for (int i = 0, lim = atom1.bonds.size(); i < lim && found != null; i++) {
				Atom atom = atoms[atom1.bonds.get(i)]; if (atom == null) continue;
				if (atom.serial != atom2.serial && !atom.elem.equals("H")) found = atom;
			}
			for (int i = 0, lim = atom2.bonds.size(); i < lim && found != null; i++) {
				Atom atom = atoms[atom2.bonds.get(i)]; if (atom == null) continue;
				if (atom.serial != atom1.serial && !atom.elem.equals("H")) found = atom;
			}
			if (found != null) {
				Vector3 tmp = new Vector3(atom1.x - found.x, atom1.y - found.y, atom1.z - found.z);
				dot = Vector3.dot(tmp, axis);
				dx = tmp.x - axis.x * dot; dy = tmp.y - axis.y * dot; dz = tmp.z - axis.z * dot;
			}
			if (found == null || Math.abs(dot - 1) < 0.001) {
				if (axis.x < 0.01 && axis.y < 0.01) {
					dx = 0; dy = - axis.z; dz = axis.y;
				} else {
					dx = - axis.y; dy =  axis.x; dz = 0;
				}
			}
			float norm = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
			dx /= norm; dy /= norm; dz /= norm;
			dx *= 0.15; dy *= 0.15; dz *= 0.15;  
		}

		Color c = atom1.color;
		float ax = atom1.x, ay = atom1.y, az = atom1.z;
		points.add(new Vector3(ax, ay, az));
		colors.add(c);
		points.add(midpoint);
		colors.add(c);
		if (order > 1) {
			points.add(new Vector3(ax + dx, ay + dy, az + dz));
			colors.add(c);
			points.add(new Vector3(midpoint.x + dx, midpoint.y + dy, midpoint.z + dz));
			colors.add(c);
		}
		if (order > 2) {
			points.add(new Vector3(ax + dx * 2, ay + dy * 2, az + dz * 2));
			colors.add(c);
			points.add(new Vector3(midpoint.x + dx * 2, midpoint.y + dy * 2, midpoint.z + dz * 2));
			colors.add(c);
		}

		ax = atom2.x; ay = atom2.y; az = atom2.z;
		c = atom2.color;
		points.add(new Vector3(ax, ay, az));
		colors.add(c);
		points.add(midpoint);
		colors.add(c);
		if (order > 1) {
			points.add(new Vector3(ax + dx, ay + dy, az + dz));
			colors.add(c);
			points.add(new Vector3(midpoint.x + dx, midpoint.y + dy, midpoint.z + dz));
			colors.add(c);
		}
		if (order > 2) {
			points.add(new Vector3(ax + dx * 2, ay + dy * 2, az + dz * 2));
			colors.add(c);
			points.add(new Vector3(midpoint.x + dx * 2, midpoint.y + dy * 2, midpoint.z + dz * 2));
			colors.add(c);
		}
	}

	public void drawBondsAsLine(Renderable scene, ArrayList<Integer> atomlist, float lineWidth) {
		ArrayList<Vector3> points = new ArrayList<Vector3>();
		ArrayList<Color> colors = new ArrayList<Color>();

		for (int i = 0, lim = atomlist.size(); i < lim; i++) {
			Atom atom1 = atoms[atomlist.get(i)];
			if (atom1 == null) continue;
			for (int j = i + 1; j < i + 20 && j < lim; j++) {
				Atom atom2 = atoms[atomlist.get(j)];
				if (atom2 == null) continue;
				int order = atom1.isConnected(atom2); 
				if (order == 0) continue;

				drawBondsAsLineSub(points, colors, atom1, atom2, order);
			}

			if (atom1.bonds == null) continue;
			for (int _j = 0, jlim = atom1.bonds.size(); _j < jlim; _j++) {
				int j = atom1.bonds.get(_j);
				if (j < i + 20) continue; // be conservative. drawing lines twice doesn't harm us.
				if (atomlist.indexOf(j) == -1) continue;
				Atom atom2 = atoms[j];
				if (atom2 == null) continue;
				drawBondsAsLineSub(points, colors, atom1, atom2, atom1.bondOrder.get(_j));
			}
		}

		Line line = new Line(points, colors);
		line.width = lineWidth;
		line.discrete = true;
		scene.children.add(line);
	}

	public void drawAtomsAsVdWSphere(Renderable scene, ArrayList<Integer> atomlist) {
		ArrayList<Vector3> points = new ArrayList<Vector3>();
		ArrayList<Color> colors = new ArrayList<Color>();
		ArrayList<Float> radii = new ArrayList<Float>();
		for (int i = 1, lim = atomlist.size(); i < lim; i++) {
			Atom atom = atoms[atomlist.get(i)];
			if (atom == null) continue;

			points.add(new Vector3(atom.x, atom.y, atom.z));
			colors.add(atom.color);
			radii.add(ChemDatabase.getVdwRadius(atom.elem));
		}
		Renderable sphere = new VBOSpheres(points, colors, radii);
		scene.children.add(sphere);
	}

	public void drawNucleicAcidAsLine(Renderable scene, ArrayList<Integer> atomlist) {
		ArrayList<Vector3> points = new ArrayList<Vector3>();
		ArrayList<Color> colors = new ArrayList<Color>();
		String currentChain = "";
		int currentResi = -1;
		Atom start = null, end = null;

		for (int i = 1, lim = atomlist.size(); i < lim; i++) {
			Atom atom = atoms[atomlist.get(i)];
			if (atom == null || atom.hetflag) continue;

			if (atom.resi != currentResi || !atom.chain.equals(currentChain)) {
				if (start != null && end != null) {
					points.add(new Vector3(start.x, start.y, start.z));
					colors.add(start.color);
					points.add(new Vector3(end.x, end.y, end.z));
					colors.add(start.color);
				}
				start = null; end = null;
			}
			if (atom.atom.equals("O3'")) start = atom;
			if (atom.resn.equals("  A") || atom.resn.equals("  G") || atom.resn.equals(" DA") || atom.resn.equals(" DG")) {
				if (atom.atom.equals("N1")) end = atom; //  N1(AG), N3(CTU)
			} else if (atom.atom.equals("N3")) {
				end = atom;
			}
			currentResi = atom.resi; currentChain = atom.chain;
		}
		if (start != null && end != null) {
			points.add(new Vector3(start.x, start.y, start.z));
			colors.add(start.color);
			points.add(new Vector3(end.x, end.y, end.z));
			colors.add(start.color);
		}
		Line line = new Line(points, colors);
		line.vertexColors = true;
		line.discrete = true;
		scene.children.add(line);
	}

	public void drawNucleicAcidCartoon(Renderable scene, ArrayList<Integer> atomlist, int div) {
		drawNucleicAcidStrand(scene, atomlist, 2, div, true);
	}

	public void drawNucleicAcidStrand(Renderable scene, ArrayList<Integer> atomlist, int num, int div, boolean fill) {
		ArrayList<Vector3> points[] = new ArrayList[num];
		for (int k = 0; k < num; k++) points[k] = new ArrayList<Vector3>();
		ArrayList<Color> colors = new ArrayList<Color>();
		String currentChain = "";
		int currentResi = -1;
		Vector3 currentO3 = null;
		Vector3 prevOO = null;

		for (int i = 1, lim = atomlist.size(); i < lim; i++) {
			Atom atom = atoms[atomlist.get(i)];
			if (atom == null || atom.hetflag) continue;

			if ((atom.atom.equals("O3'") || atom.atom.equals("OP2")) && !atom.hetflag) {
				if (atom.atom.equals("O3'")) { // to connect 3' end. FIXME: better way to do?
					if (!atom.chain.equals(currentChain) || currentResi + 1 != atom.resi) {               
						if (currentO3 != null) {
							for (int j = 0; j < num; j++) {
								float delta = -1 + 2.0f / (num - 1) * j;
								points[j].add(new Vector3(currentO3.x + prevOO.x * delta, 
										currentO3.y + prevOO.y * delta, currentO3.z + prevOO.z * delta));
							}
						}
						if (fill) scene.children.add(new RibbonStrip(points[0], points[1], colors));
						for (int j = 0; j < num; j++)
							scene.children.add(new SmoothCurve(points[j], colors, 1.0f, div));
						for (int k = 0; k < num; k++) points[k].clear();
						colors.clear();
						prevOO = null;
					}
					currentO3 = new Vector3(atom.x, atom.y, atom.z);
					currentChain = atom.chain;
					currentResi = atom.resi;
					colors.add(atom.color);
				} else { // OP2
					if (currentO3 == null) {
						prevOO = null;
						continue;
					}
					Vector3 O = new Vector3(atom.x - currentO3.x, atom.y - currentO3.y, atom.z - currentO3.z);
					O.normalize().multiplyScalar(0.8f);
					if (prevOO != null && Vector3.dot(O, prevOO) < 0) {
						O.negate();
					}
					prevOO = O;
					for (int j = 0; j < num; j++) {
						float delta = -1 + 2.0f / (num - 1) * j;
						points[j].add(new Vector3(currentO3.x + prevOO.x * delta, 
								currentO3.y + prevOO.y * delta, currentO3.z + prevOO.z * delta));
					}
					currentO3 = null;
				}
			}
		}
		if (currentO3 != null) {
			for (int j = 0; j < num; j++) {
				float delta = -1 + 2.0f / (num - 1) * j;
				points[j].add(new Vector3(currentO3.x + prevOO.x * delta, 
						currentO3.y + prevOO.y * delta, currentO3.z + prevOO.z * delta));
			}
		}
		if (fill) scene.children.add(new RibbonStrip(points[0], points[1], colors));
		for (int j = 0; j < num; j++)
			scene.children.add(new SmoothCurve(points[j], colors, 1.0f, div));
	}

	public void drawStrand(Renderable scene, ArrayList<Integer> atomlist, int num, int div, boolean fill) {
		ArrayList<Vector3> points[] = new ArrayList[num];
		for (int k = 0; k < num; k++) points[k] = new ArrayList<Vector3>();
		ArrayList<Color> colors = new ArrayList<Color>();
		String currentChain = "";
		int currentResi = -1;
		Vector3 currentCA = null, prevCO = null;
		String ss = "";

		for (int i = 1, lim = atomlist.size(); i < lim; i++) {
			Atom atom = atoms[atomlist.get(i)];
			if (atom == null || atom.hetflag) continue;

			if ((atom.atom.equals("O") || atom.atom.equals("CA")) && !atom.hetflag) {
				if (atom.atom.equals("CA")) {
					if (!currentChain.equals(atom.chain) || currentResi + 1 != atom.resi) {
						if (fill) scene.children.add(new RibbonStrip(points[0], points[1], colors));
						for (int j = 0; j < num; j++)
							scene.children.add(new SmoothCurve(points[j], colors, 1.0f, div));
						for (int k = 0; k < num; k++) points[k].clear();
						colors.clear();
						prevCO = null; ss = "";
					}
					currentCA = new Vector3(atom.x, atom.y, atom.z);
					currentChain = atom.chain;
					currentResi = atom.resi;
					ss = atom.ss;
					colors.add(atom.color);
				} else { // O
					Vector3 O = new Vector3(atom.x, atom.y, atom.z);
					O.x -= currentCA.x; O.y -= currentCA.y;  O.z -= currentCA.z;  
					O.normalize(); // can be omitted for performance
					if (ss.equals("c")) {
						O.x *= 0.3f; O.y *= 0.3f; O.z *= 0.3f;  
					} else {
						O.x *= 1.3f; O.y *= 1.3f; O.z *= 1.3f;
					}
					if (prevCO != null && Vector3.dot(O, prevCO) < 0) {
						O.negate();
					}
					prevCO = O;
					for (int j = 0; j < num; j++) {
						float delta = -1 + 2.0f / (num - 1) * j;
						points[j].add(new Vector3(currentCA.x + prevCO.x * delta, 
								currentCA.y + prevCO.y * delta, currentCA.z + prevCO.z * delta));
					}                         
				}
			}
		}
		if (fill) scene.children.add(new RibbonStrip(points[0], points[1], colors));
		for (int j = 0; j < num; j++)
			scene.children.add(new SmoothCurve(points[j], colors, 1.0f, div));
	}

	public void drawAtomsAsStar(Renderable scene, ArrayList<Integer> atomlist, float delta) {
		ArrayList<Vector3> points = new ArrayList<Vector3>();
		ArrayList<Color> colors = new ArrayList<Color>();
		float pointsBase[] = {delta, 0, 0, -delta, 0, 0, 0, delta, 0, 0, -delta, 0, 0, 0, delta, 0, 0, -delta};

		for (int i = 1, lim = atomlist.size(); i < lim; i++) {
			Atom atom = atoms[atomlist.get(i)];
			if (atom == null) continue;

			int offset = 0;
			float x = atom.x, y = atom.y, z = atom.z;
			for (int j = 0; j < 6; j++) {
				points.add(new Vector3(x + pointsBase[offset++], y + pointsBase[offset++], z + pointsBase[offset++]));
				colors.add(atom.color);
			}
		}
		Line line = new Line(points, colors);
		line.discrete = true;
		line.width = 1;
		scene.children.add(line);
	}

	public void drawSymmetryMates(Renderable scene, ArrayList<Integer> atomlist, HashMap<Integer, float[]> biomtMatrices) {
		if (biomtMatrices == null) return;

		MatRenderable symmetryMates = new MatRenderable();
		switch (proteinMode) { // TODO: refactor
		case 0:
			drawCartoon(symmetryMates, atomlist, 5);
			drawNucleicAcidCartoon(symmetryMates, atomlist, 5);
			break;
		case 1:
			drawMainchainCurve(symmetryMates, atomlist, curveWidth, "CA");
			drawMainchainCurve(symmetryMates, atomlist, curveWidth, "O3'");
			break;
		case 2:
			drawStrand(symmetryMates, atomlist, 5, 5, false);
			drawNucleicAcidStrand(symmetryMates, atomlist, 5, 5, false);
			break;
		case 3:
			drawMainchainTube(symmetryMates, atomlist, "CA");
			drawMainchainTube(symmetryMates, atomlist, "O3'");
			break;
		}
		scene.children.add(symmetryMates);

		for (float mat[]: biomtMatrices.values()) {
			if (mat == null || isIdentity(mat)) continue;

			symmetryMates.addMatrix(mat);
		}
	}

	public void drawSymmetryMatesWithTranslation(Renderable scene, ArrayList<Integer> atomlist, HashMap<Integer, float[]> matrices) {
		if (matrices == null) return;

		MatRenderable symmetryMates = new MatRenderable();
		drawMainchainCurve(symmetryMates, atomlist, curveWidth, "CA");
		drawMainchainCurve(symmetryMates, atomlist, curveWidth, "P");
		scene.children.add(symmetryMates);

		for (float mat[]: matrices.values()) {
			if (mat == null) continue;

			for (int a = -1; a <= 0; a++) {
				for (int b = -1; b <= 0; b++) {
					for (int c = -1; c <= 0; c++) {

						float[] translated = mat.clone();
						translated[3] += protein.ax * a + protein.bx * b + protein.cx * c;
						translated[7] += protein.ay * a + protein.by * b + protein.cy * c;
						translated[11] += protein.az * a + protein.bz * b + protein.cz * c;
						if (isIdentity(translated)) continue;
						symmetryMates.addMatrix(translated);
					}
				}
			}
		}
	}

	public void drawUnitcell(Renderable scene, float width) {
		if (protein.a == 0) return;

		float vertices[][] = {{0, 0, 0}, 
				{protein.ax, protein.ay, protein.az}, 
				{protein.bx, protein.by, protein.bz},
				{protein.ax + protein.bx, protein.ay + protein.by, protein.az + protein.bz},
				{protein.cx, protein.cy, protein.cz},
				{protein.cx + protein.ax, protein.cy + protein.ay,  protein.cz + protein.az}, 
				{protein.cx + protein.bx, protein.cy + protein.by, protein.cz + protein.bz}, 
				{protein.cx + protein.ax + protein.bx, protein.cy + protein.ay + protein.by, protein.cz + protein.az + protein.bz}};
		int edges[] = {0, 1, 0, 2, 1, 3, 2, 3, 4, 5, 4, 6, 5, 7, 6, 7, 0, 4, 1, 5, 2, 6, 3, 7};    

		float points[] = new float[24 * 3];
		for (int i = 0; i < edges.length; i++) {
			points[i * 3] = vertices[edges[i]][0];
			points[i * 3 + 1] = vertices[edges[i]][1];
			points[i * 3 + 2] = vertices[edges[i]][2];
		}
		Line line = new Line(points);
		line.objectColor = new Color(0.8f, 0.8f, 0.8f, 1);
		line.discrete = true;
		line.width = width;
		scene.children.add(line);
	}

	public void onDrawFrame(GL10 gl){
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT|GL10.GL_DEPTH_BUFFER_BIT);

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		float cameraNear = -cameraZ + slabNear;
		if (cameraNear < 1) cameraNear = 1;
		float cameraFar = -cameraZ + slabFar;
		if (cameraNear + 1 > cameraFar) cameraFar = cameraNear + 1;
		GLU.gluPerspective(gl, FOV, (float) width / height, cameraNear, cameraFar);
		if (fogEnabled) {
			gl.glEnable(GL10.GL_FOG);
			gl.glFogf(GL10.GL_FOG_MODE, GL10.GL_LINEAR); // EXP, EXP2 is not supported?
			gl.glFogfv(GL10.GL_FOG_COLOR, Geometry.getFloatBuffer(new float[] {0, 0, 0, 1}));
			gl.glFogf(GL10.GL_FOG_DENSITY, 0.3f);
			//		gl.glHint(GL10.GL_FOG_HINT, GL10.GL_DONT_CARE);
			gl.glFogf(GL10.GL_FOG_START, cameraNear * 0.3f + cameraFar * 0.7f);
			gl.glFogf(GL10.GL_FOG_END, cameraFar);
		} else {
			gl.glDisable(GL10.GL_FOG);
		}
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glTranslatef(0, 0, cameraZ);

		Vector3 axis = rotationQ.getAxis();
		gl.glRotatef(180 * rotationQ.getAngle() / (float)Math.PI, axis.x,
				axis.y, axis.z);

		gl.glTranslatef(objX, objY, objZ);

		if (scene != null)
			scene.render(gl, this);
		if (fogEnabled) gl.glDisable(GL10.GL_FOG);
		
		// Reference: http://groups.google.com/group/android-developers/browse_thread/thread/3642d78732c45f19?pli=1
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		Log.d("ESmolView", "OnSurfaceChanged");

		this.width = width; this.height = height;

		gl.glViewport(0, 0, width, height);

		VBOCylinder.prepareVBO((GL11)gl);
		VBOSphere.prepareVBO((GL11)gl, this);
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		Log.d("GLView", "OnSurfaceCreated");
		gl.glClearColor(0, 0, 0, 1);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		//		gl.glEnable(GL10.GL_NORMALIZE);
		gl.glShadeModel(GL10.GL_SMOOTH);
		//		gl.glHint(GL10.GL_POINT_SMOOTH_HINT, GL10.GL_NICEST);
		gl.glEnable(GL10.GL_POINT_SMOOTH);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		//		gl.glEnable(GL10.GL_LINE_SMOOTH); // FIXME: Check if this is working.
		gl.glLightModelx(GL10.GL_LIGHT_MODEL_TWO_SIDE, 1); // double sided
		gl.glEnable(GL10.GL_COLOR_MATERIAL); // glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE) ?
		gl.glDepthFunc(GL10.GL_LEQUAL);
		gl.glDisable(GL10.GL_DITHER);
		gl.glEnable(GL10.GL_LIGHTING);
		gl.glEnable(GL10.GL_LIGHT0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, Geometry.getFloatBuffer(new float[] {0.4f, 0.4f, 0.4f, 1}));
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, Geometry.getFloatBuffer(new float[] {0, 0, 1, 0}));
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, Geometry.getFloatBuffer(new float[] {0.8f, 0.8f, 0.8f, 1}));
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, Geometry.getFloatBuffer(new float[] {0.8f, 0.8f, 0.8f, 1}));
		gl.glEnable(GL10.GL_LIGHT1);
		gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_POSITION, Geometry.getFloatBuffer(new float[] {0, 0, -1, 0}));
		gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_DIFFUSE, Geometry.getFloatBuffer(new float[] {0.8f, 0.8f, 0.8f, 1}));
		gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_SPECULAR, Geometry.getFloatBuffer(new float[] {0.1f, 0.1f, 0.1f, 1}));
		((GL11)gl).glPointParameterfv(GL11.GL_POINT_DISTANCE_ATTENUATION, Geometry.getFloatBuffer(new float[] {0, 0, 1}));
		gl.glDisable(GL10.GL_FOG);
	}
}