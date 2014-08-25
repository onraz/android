

package org.raz.pdb.graphics;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;


public class SphereGeometry extends Geometry {
	private static FloatBuffer vertexBuffer, vertexNormalBuffer;
	private static ShortBuffer faceBuffer;
	private static int sphereQuality = 12;
	
	private static SphereGeometry geo;
	
	public static FloatBuffer getVertexBuffer() {
		if (geo == null)  geo = new SphereGeometry(sphereQuality, sphereQuality);
		return vertexBuffer;
	}
	
	public static FloatBuffer getVertexNormalBuffer() {
		if (geo == null)  geo = new SphereGeometry(sphereQuality, sphereQuality);
		return vertexNormalBuffer;
	}
		
	public static ShortBuffer getFaceBuffer() {
		if (geo == null)  geo = new SphereGeometry(sphereQuality, sphereQuality);
		return faceBuffer;
	}
	
	private SphereGeometry(int div1, int div2) {
		vertices = new float[(div1 + 1) * (div2 + 1) * 3];

		int offset = 0;
		for (int i = 0; i <= div1; i++) { // z
			float z = 1 - 2.0f * i / div1;
			float r = (float)Math.sqrt(1 - z * z); 
			for (int j = 0; j <= div2; j++) {
				float x = r * (float)Math.cos(j * 2 * Math.PI / div2);
				float y = r * (float)Math.sin(j * 2 * Math.PI / div2);
				vertices[offset] = x;
				vertices[offset + 1] = y;
				vertices[offset + 2] = z;
				offset += 3;
			}
		}
		
		faces = new short[div1 * div2 * 3 * 2];
		offset = 0;
		for (int i = 0; i < div1; i++) { // z
			for (int j = 0; j < div2; j++) {
				faces[offset] = (short)((div2 + 1) * i + j); 
				faces[offset + 1] = (short) ((div2 + 1) * i + j + 1); 
				faces[offset + 2] = (short)((div2 + 1) * (i + 1) + j + 1);
				faces[offset + 3] = (short)((div2 + 1) * i + j); 
				faces[offset + 4] = (short) ((div2 + 1) * (i + 1) + j + 1); 
				faces[offset + 5] = (short)((div2 + 1) * (i + 1) + j);
				offset += 6;
			}
		} 

		vertexBuffer = getFloatBuffer(vertices);
		vertexNormalBuffer = vertexBuffer;
		faceBuffer = getShortBuffer(faces);
	}
}
