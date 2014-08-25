

package org.raz.pdb.graphics;


public class Sphere extends Renderable {
	public Sphere(float x, float y, float z, float radius, Color c) {
//		Log.d("Sphere", "color " + c.toString());
		vertexBuffer = SphereGeometry.getVertexBuffer();
		faceBuffer = SphereGeometry.getFaceBuffer();
		vertexNormalBuffer = SphereGeometry.getVertexNormalBuffer();
		scalex = scaley = scalez = radius;
		posx = x; posy = y; posz = z;
		objectColor = c;
	}
}
