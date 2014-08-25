package org.raz.pdb.graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class RibbonStrip extends Renderable {
	static int div = 5;
	
	public RibbonStrip(ArrayList<Vector3> _points1, ArrayList<Vector3> _points2, ArrayList<Color> colors) {
		if (_points1.size() < 1) return;
		vertexColors = true;
	
		float points1[] = Geometry.Subdivide(_points1, 5);
		float points2[] = Geometry.Subdivide(_points2, 5);
		
		ByteBuffer vertexbb = ByteBuffer.allocateDirect(points1.length * 2 * 4);
		vertexbb.order(ByteOrder.nativeOrder());
		vertexBuffer = vertexbb.asFloatBuffer();
		ByteBuffer vertexNormalbb = ByteBuffer.allocateDirect(points1.length * 2 * 4);
		vertexNormalbb.order(ByteOrder.nativeOrder());
		vertexNormalBuffer = vertexNormalbb.asFloatBuffer();
		ByteBuffer facebb = ByteBuffer.allocateDirect(points1.length * 3 * 2 * 2);
		facebb.order(ByteOrder.nativeOrder());
		faceBuffer = facebb.asShortBuffer();
		
		int vertexOffset = 0, faceOffset = 2;
		for (int i = 0, lim = points1.length / 3; i < lim; i++) {
		    vertexBuffer.put(points1[vertexOffset]); // face 2 * i
		    vertexBuffer.put(points1[vertexOffset + 1]);
		    vertexBuffer.put(points1[vertexOffset + 2]);
		    vertexBuffer.put(points2[vertexOffset]); // face 2 * i + 1
		    vertexBuffer.put(points2[vertexOffset + 1]);
		    vertexBuffer.put(points2[vertexOffset + 2]);
		    vertexOffset += 3;
		    
		    if (i == 0) continue;
		    faceBuffer.put((short)(faceOffset - 2));
		    faceBuffer.put((short)(faceOffset - 1));
		    faceBuffer.put((short) faceOffset);
		    faceBuffer.put((short) faceOffset);
		    faceBuffer.put((short) (faceOffset - 1));
		    faceBuffer.put((short) (faceOffset + 1));
		    faceOffset += 2;
		}
		

		for (int i = 0, lim = points1.length / 3; i < lim; i++) {
			vertexOffset = 3 * i;
			float ax = 0, ay = 0, az = 0; // for ribbon direction, take average
			if (i > 0) {
				ax += points1[vertexOffset] - points1[vertexOffset - 3];
				ay += points1[vertexOffset + 1] - points1[vertexOffset - 2];
				az += points1[vertexOffset + 2] - points1[vertexOffset - 1];
			}
			if (i < lim - 1) {
				ax += points1[vertexOffset + 3] - points1[vertexOffset];
				ay += points1[vertexOffset + 4] - points1[vertexOffset + 1];
				az += points1[vertexOffset + 5] - points1[vertexOffset + 2];
			}
			float bx = points2[vertexOffset] - points1[vertexOffset];
			float by = points2[vertexOffset + 1] - points1[vertexOffset + 1];
			float bz = points2[vertexOffset + 2] - points1[vertexOffset + 2];

			float nx = ay * bz - az * by;
			float ny = az * bx - ax * bz;
			float nz = ax * by - ay * bx;

			float norm = (float)Math.sqrt(nx * nx + ny * ny + nz * nz);
			nx /= norm; ny /= norm; nz /= norm;
			
			vertexNormalBuffer.put(nx);
			vertexNormalBuffer.put(ny);
			vertexNormalBuffer.put(nz);
			vertexNormalBuffer.put(nx);
			vertexNormalBuffer.put(ny);
			vertexNormalBuffer.put(nz);
		}
		
		colorBuffer = Geometry.colorsToFloatBuffer(colors, div * 2);
		vertexBuffer.position(0);
		vertexNormalBuffer.position(0);
		faceBuffer.position(0);
	}
}
