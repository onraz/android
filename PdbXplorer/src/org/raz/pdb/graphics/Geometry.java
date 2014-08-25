

package org.raz.pdb.graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;


public class Geometry {
	public float vertices[], colors[], vertexNormals[];
	public short faces[];
	public boolean vertexColors = false;

// This is COMPLETELY WRONG!
//	public void computeFaceNormals() {
//		vertexNormals = new float[faces.length * 3 * 3];
//		for (int i = 0, lim = faces.length / 3; i < lim; i++) {
//			int v1 = faces[3 * i];
//			int v2 = faces[3 * i + 1];
//			int v3 = faces[3 * i + 2];
//			float ax = vertices[v2] - vertices[v1];
//			float bx = vertices[v3] - vertices[v1];
//			float ay = vertices[v2 + 1] - vertices[v1 + 1];
//			float by = vertices[v3 + 1] - vertices[v1 + 1];
//			float az = vertices[v2 + 2] - vertices[v1 + 2];
//			float bz = vertices[v3 + 2] - vertices[v1 + 2];
//			
//			float nx = ay * bz - az * by;
//			float ny = az * bx - ax * bz;
//			float nz = ax * by - ay * bx;
//			
//			/*
//			float dist = (float)Math.sqrt(nx * nx + ny * ny + nz * nz);
//			if (Math.abs(dist) < 0.001) {
//				nx = 1; ny = nz = 0;
//			} else {
//				nx /= dist; ny /= dist; nz /= dist;
//			}*/
//			vertexNormals[9 * i] = nx;
//			vertexNormals[9 * i + 1] = ny;
//			vertexNormals[9 * i + 2] = nz;
//			vertexNormals[9 * i + 3] = nx;
//			vertexNormals[9 * i + 4] = ny;
//			vertexNormals[9 * i + 5] = nz;
//			vertexNormals[9 * i + 6] = nx;
//			vertexNormals[9 * i + 7] = ny;
//			vertexNormals[9 * i + 8] = nz;
//		}
//	}
	
	public static FloatBuffer getFloatBuffer(float [] array){
		FloatBuffer floatBuffer;
		ByteBuffer bb = ByteBuffer.allocateDirect(array.length * 4);
		bb.order(ByteOrder.nativeOrder());
		floatBuffer = bb.asFloatBuffer();
		floatBuffer.put(array);
		floatBuffer.position(0);
		return floatBuffer;
	}

	// Catmull-Rom subdivision
	public static float[] Subdivide(ArrayList<Vector3> points, int div) {
		float[] ret = new float[((points.size() - 1) * div + 1) * 3]; // TODO: check

		//	   points.unshift(points[0]);
		//	   points.push(points[points.length - 1]);

		int offset = 0;
		for (int i = -1, size = points.size(); i <= size - 3; i++) {
//			Log.d("Subdivide", "now dividing between " + (i + 1) + " " + (i + 2));
			Vector3 p0 = points.get((i == -1) ? 0 : i);
			Vector3 p1 = points.get(i + 1);
			Vector3 p2 = points.get(i + 2);
			Vector3 p3 = points.get((i == size - 3) ? size - 1 : i + 3);
			
			float v0x = (p2.x - p0.x) / 2;
			float v0y = (p2.y - p0.y) / 2;
			float v0z = (p2.z - p0.z) / 2;
			float v1x = (p3.x - p1.x) / 2;
			float v1y = (p3.y - p1.y) / 2;
			float v1z = (p3.z - p1.z) / 2;

			for (int j = 0; j < div; j++) {
				float t = 1.0f / div * j;
				float x = p1.x + t * v0x 
							+ t * t * (-3 * p1.x + 3 * p2.x - 2 * v0x - v1x)
							+ t * t * t * (2 * p1.x - 2 * p2.x + v0x + v1x);
				float y = p1.y + t * v0y 
							+ t * t * (-3 * p1.y + 3 * p2.y - 2 * v0y - v1y)
							+ t * t * t * (2 * p1.y - 2 * p2.y + v0y + v1y);
				float z = p1.z + t * v0z 
							+ t * t * (-3 * p1.z + 3 * p2.z - 2 * v0z - v1z)
							+ t * t * t * (2 * p1.z - 2 * p2.z + v0z + v1z);
//				Log.d("Subdivide", "offset = " + offset + points.size());
				ret[offset] = x;
				ret[offset + 1] = y;
				ret[offset + 2] = z;
				offset += 3;
			}
		}
		Vector3 last = points.get(points.size() - 1);
		ret[offset] = last.x;
		ret[offset + 1] = last.y;
		ret[offset + 2] = last.z;
		return ret;
	}

	public static ShortBuffer getShortBuffer(short [] array){
		ShortBuffer shortBuffer;
		ByteBuffer bb = ByteBuffer.allocateDirect(array.length * 2);
		bb.order(ByteOrder.nativeOrder());
		shortBuffer = bb.asShortBuffer();
		shortBuffer.put(array);
		shortBuffer.position(0);
		return shortBuffer;
	}
	
	public static FloatBuffer colorsToFloatBuffer(ArrayList<Color> colors, int duplicate) {
		FloatBuffer floatBuffer;
		ByteBuffer bb = ByteBuffer.allocateDirect(colors.size() * duplicate * 4 * 4);
		bb.order(ByteOrder.nativeOrder());
		floatBuffer = bb.asFloatBuffer();
		for (int i = 0, lim = colors.size(); i < lim; i++) { // MEMO: Or should I make float[] and put once?
			Color color = colors.get(i);
			for (int j = 0; j < duplicate; j++) {
				floatBuffer.put(color.r);
				floatBuffer.put(color.g);
				floatBuffer.put(color.b);
				floatBuffer.put(1.0f);
			}
		}
		floatBuffer.position(0);
		return floatBuffer;
	}

	public static FloatBuffer getFloatBuffer(ArrayList<Vector3> points) {
		FloatBuffer floatBuffer;
		ByteBuffer bb = ByteBuffer.allocateDirect(points.size() * 3 * 4);
		bb.order(ByteOrder.nativeOrder());
		floatBuffer = bb.asFloatBuffer();
		for (int i = 0, lim = points.size(); i < lim; i++) {
			Vector3 v = points.get(i);

			floatBuffer.put(v.x);
			floatBuffer.put(v.y);
			floatBuffer.put(v.z);
		}
		floatBuffer.position(0);
		return floatBuffer;
	}
	
	public static ShortBuffer getShortBuffer(ArrayList<Short> shorts) {
		ShortBuffer shortBuffer;
		ByteBuffer bb = ByteBuffer.allocateDirect(shorts.size() * 2);
		bb.order(ByteOrder.nativeOrder());
		shortBuffer = bb.asShortBuffer();
		for (int i = 0, lim = shorts.size(); i < lim; i++) {
			shortBuffer.put(shorts.get(i));
		}
		shortBuffer.position(0);
		return shortBuffer;
	}

	static public float[] fromVectorArrayList(ArrayList<Vector3> points) {
		float[] ret = new float[points.size() * 3];
		for (int i = 0, lim = points.size(); i < lim; i++) {
			Vector3 v = points.get(i);
			ret[3 * i] = v.x;
			ret[3 * i + 1] = v.y;
			ret[3 * i + 2] = v.z;
		}
		return ret;
	}
	
	
}
