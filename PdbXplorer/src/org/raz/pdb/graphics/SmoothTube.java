package org.raz.pdb.graphics;

import java.util.ArrayList;

public class SmoothTube extends Renderable {
	public SmoothTube(ArrayList<Vector3> _points, ArrayList<Color> colors, ArrayList<Float> radii) {
		if (_points.size() < 2) return;

		int circleDiv = 6, axisDiv = 3;

		float[] points = Geometry.Subdivide(_points, axisDiv);
		_points.clear();
		float[] vertices = new float[points.length * circleDiv];
		float[] normals = new float[points.length * circleDiv];
		short[] faces = new short[(points.length / 3 - 1) * circleDiv * 2 * 3];
		int voffset = 0;
		int foffset = 0;

		Vector3 prevAxis1 = new Vector3(1, 0, 0), prevAxis2 = new Vector3(0, 1, 0);

		for (int i = 0, lim = points.length / 3; i < lim; i++) {
			int poffset = i * 3;
			float r;
			if (i == 0) {
				r = radii.get(i);
			} else {
				int idx = (i - 1) / axisDiv;
				if (idx * axisDiv == i - 1) {
					r = radii.get((int)idx);
				}				else {
					float tmp = i - 1 - idx * axisDiv;
					r = radii.get(idx) * tmp + radii.get(idx + 1) * (1 - tmp);
				}
			}
			Vector3 delta = new Vector3(), axis1 = new Vector3(), axis2 = null;

			if (i < lim - 1) {
				delta.x = points[poffset] - points[poffset + 3];
				delta.y = points[poffset + 1] - points[poffset + 4];
				delta.z = points[poffset + 2] - points[poffset + 5];
				axis1.x = 0; axis1.y = - delta.z; axis1.z = delta.y;
				axis1.normalize().multiplyScalar(r);
				axis2 = Vector3.cross(delta, axis1).normalize().multiplyScalar(r);
				if (Vector3.dot(prevAxis1, axis1) < 0) {
					axis1.negate(); axis2.negate(); 
				}
				prevAxis1 = axis1; prevAxis2 = axis2;
			} else {
				axis1 = prevAxis1; axis2 = prevAxis2;
			}

			for (int j = 0; j < circleDiv; j++) {
				float angle = (float)(2 * Math.PI / circleDiv * j);
				float c = (float)Math.cos(angle), s = (float)Math.sin(angle);
				normals[voffset] = c * axis1.x + s * axis2.x;
				vertices[voffset] = points[poffset] + normals[voffset];
				voffset++;
				normals[voffset] = c * axis1.y + s * axis2.y;
				vertices[voffset] = points[poffset + 1] + normals[voffset];
				voffset++;
				normals[voffset] = c * axis1.z + s * axis2.z;
				vertices[voffset] = points[poffset + 2] + normals[voffset];
				voffset++;
			}
			poffset += 3;
		}

		voffset = 0;
		for (int i = 0, lim = points.length / 3 - 1; i < lim; i++) {
			int reg = 0;
			int vo1 = voffset * 3, vo2 = vo1 + circleDiv * 3;
			float r1 = (float) Vector3.norm(vertices[vo1] - vertices[vo2],
					vertices[vo1 + 1] - vertices[vo2 + 1],
					vertices[vo1 + 2] - vertices[vo2 + 2]);
			float r2 = (float) Vector3.norm(vertices[vo1] - vertices[vo2 + 3],
					vertices[vo1 + 1] - vertices[vo2 + 4],
					vertices[vo1 + 2] - vertices[vo2 + 5]);
			if (r1 > r2) {r1 = r2; reg = 1;}
			for (int j = 0; j < circleDiv; j++) {
				faces[foffset++] = (short) (voffset + j);
				faces[foffset++] = (short) (voffset + (j + reg) % circleDiv + circleDiv);
				faces[foffset++] = (short) (voffset + (j + 1) % circleDiv);
				faces[foffset++] = (short) (voffset + (j + 1) % circleDiv);
				faces[foffset++] = (short) (voffset + (j + reg) % circleDiv + circleDiv);
				faces[foffset++] = (short) (voffset + (j + reg + 1) % circleDiv + circleDiv);
			}
			voffset += circleDiv;
		}

		vertexColors = true;
		vertexBuffer = Geometry.getFloatBuffer(vertices);
		vertexNormalBuffer = Geometry.getFloatBuffer(normals);
		faceBuffer = Geometry.getShortBuffer(faces);
		colorBuffer = Geometry.colorsToFloatBuffer(colors, axisDiv * circleDiv);
	}
}

