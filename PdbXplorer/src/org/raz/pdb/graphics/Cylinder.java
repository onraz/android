

package org.raz.pdb.graphics;

public class Cylinder extends Renderable {

	public Cylinder(float x1, float y1, float z1, float x2, float y2, float z2, float radius, Color color) {
		
		vertexBuffer = CylinderGeometry.getVertexBuffer();
		faceBuffer = CylinderGeometry.getFaceBuffer();
		vertexNormalBuffer = CylinderGeometry.getVertexNormalBuffer();
		objectColor = color;
		//		colorBuffer = SphereGeometry.getFaceNormalBuffer();
		//		vertexColors = true;

		double dist = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2));
		if (dist < 0.001) return;
		
		posx = x1; posy = y1; posz = z1;
		if (Math.abs(x1 - x2) > 0.0001 || Math.abs(y1 - y2) > 0.001){
			rot = (float) (180 / Math.PI * Math.acos((z2 - z1) / dist));
			rotx = y1 - y2;
			roty = x2 - x1;
			rotz = 0;
		} else {
			rot = (float) (180 / Math.PI * Math.acos((z2 - z1) / dist));
			rotx = 1;
			roty = 0;
			rotz = 0;
		}
		
		scalex = scaley = radius; scalez = (float) dist;
	}
}
