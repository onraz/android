

package org.raz.pdb.graphics;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class CylinderGeometry extends Geometry {
	private static FloatBuffer vertexBuffer, faceNormalBuffer;
	private static ShortBuffer faceBuffer;
	private static CylinderGeometry geo;
	private static int cylinderQuality = 8;
	
	public static FloatBuffer getVertexBuffer() {
		if (geo == null)  geo = new CylinderGeometry(cylinderQuality);
		return vertexBuffer;
	}
	
	public static FloatBuffer getVertexNormalBuffer() {
		if (geo == null)  geo = new CylinderGeometry(cylinderQuality);
		return faceNormalBuffer;
	}
		
	public static ShortBuffer getFaceBuffer() {
		if (geo == null)  geo = new CylinderGeometry(cylinderQuality);
		return faceBuffer;
	}
	

	private CylinderGeometry (int div) {
		vertices = new float[(div + 1) * 3 * 2];
		vertexNormals = new float[(div + 1) * 3 * 2];
		
		float cos = (float)Math.cos(2 * Math.PI / div);
		float sin = (float)Math.sin(2 * Math.PI / div);
		
		vertices[0] = 1; vertices[1] = 0;vertices[2] = 0;
		vertices[3] = 1; vertices[4] = 0;vertices[5] = 1;
		vertexNormals[0] = 1; vertexNormals[1] = 0;vertexNormals[2] = 0;
		vertexNormals[3] = 1; vertexNormals[4] = 0;vertexNormals[5] = 1;
		
		int offset = 6;
		for (int i = 1; i <= div; i++) {
			float x = vertices[offset - 3];
			float y = vertices[offset - 2];
			float nx = x * cos - y * sin;
			float ny = x * sin + y * cos;
			vertices[offset] = vertices[offset + 3] = vertexNormals[offset] = vertexNormals[offset + 3] = nx;
			vertices[offset + 1] = vertices[offset + 4] = vertexNormals[offset + 1] = vertexNormals[offset + 4] = ny;
			vertices[offset + 2] = vertexNormals[offset + 2] = vertexNormals[offset + 5] = 0;
			vertices[offset + 5] = 1; 
			offset += 6;
		}
		
		
		faces = new short[(div + 0) * 2 * 3];
		offset = 0;
		for (int i = 0; i < div; i++) {
			faces[6 * i] = (short) (offset);
			faces[6 * i + 1] = (short) (offset + 1);
			faces[6 * i + 2] = (short) (offset + 2);
			faces[6 * i + 3] = (short) (offset + 2);
			faces[6 * i + 4] = (short) (offset + 1);
			faces[6 * i + 5] = (short) (offset + 3);
			offset += 2;
		}

		vertexBuffer = getFloatBuffer(vertices);
		faceNormalBuffer = getFloatBuffer(vertexNormals);
		faceBuffer = getShortBuffer(faces);
	}
	
//
//	private CylinderGeometry (int div) {
//		vertices = new float[(div + 1) * 3 * 2 * 2];
//		
//		float cos = (float)Math.cos(2 * Math.PI / div);
//		float sin = (float)Math.sin(2 * Math.PI / div);
//		
//		vertices[0] = 1; vertices[1] = 0;vertices[2] = 0;
//		vertices[3] = 1; vertices[4] = 0;vertices[5] = 1;
//		
//		int offset = 6;
//		for (int i = 1; i <= div; i++) {
//			float x = vertices[offset - 3];
//			float y = vertices[offset - 2];
//			float nx = x * cos - y * sin;
//			float ny = x * sin + y * cos;
//			vertices[offset] = vertices[offset + 3] = vertices[offset + 6] = vertices[offset + 9] = nx;
//			vertices[offset + 1] = vertices[offset + 4] = vertices[offset + 7] = vertices[offset + 10] = ny;
//			vertices[offset + 2] = vertices[offset + 8] = 0;
//			vertices[offset + 5] = vertices[offset + 11] = 1; 
//			offset += 12;
//		}
//		
//		vertexNormals = new float[(div + 1) * 3 * 4];
//		for (int i = 0; i <= div; i++) {
//			vertexNormals[12 * i] = vertexNormals[12 * i + 3] = vertexNormals[12 * i + 6] = vertexNormals[12 * i + 9] = vertices[12 * i];
//			vertexNormals[12 * i + 1] = vertexNormals[12 * i + 4] = vertexNormals[12 * i + 7] = vertexNormals[12 * i + 10] = vertices[12 * i + 1];
//			vertexNormals[12 * i + 2] = vertexNormals[12 * i + 5] = vertexNormals[12 * i + 8] = vertexNormals[12 * i + 11] = 0;
//		}
//		
//		faces = new short[(div + 0) * 2 * 3];
//		for (int i = 0; i < div; i++) {
//			faces[6 * i] = (short) (4 * i);
//			faces[6 * i + 1] = (short) (4 * i + 1);
//			faces[6 * i + 2] = (short) (4 * i + 2);
//			faces[6 * i + 3] = (short) (4 * i + 2);
//			faces[6 * i + 4] = (short) (4 * i + 1);
//			faces[6 * i + 5] = (short) (4 * i + 3);
//		}
//		
//		for (int i = 0; i < vertices.length; i++) {
//			Log.d("Cylinder", "v" + i + " " + vertices[i]);
//		}
//
//		vertexBuffer = getFloatBuffer(vertices);
//		faceNormalBuffer = getFloatBuffer(vertexNormals);
//		faceBuffer = getShortBuffer(faces);
//	}
}
