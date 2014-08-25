

package org.raz.pdb.graphics;

import java.util.ArrayList;

public class SmoothCurve extends Line {
	public SmoothCurve(ArrayList<Vector3> points, ArrayList<Color> colors, float curveWidth) {
		this(points, colors, curveWidth, 5);
	}
	
	public SmoothCurve(ArrayList<Vector3> points, ArrayList<Color> colors, float curveWidth, int div) {
		if (points.size() > 1) {
			vertexBuffer = Geometry.getFloatBuffer(Geometry.Subdivide(points, div));
			colorBuffer = Geometry.colorsToFloatBuffer(colors, div);
			vertexColors = true;
			width = curveWidth;
//			Log.d("SmoothCurve", "original N = " + points.size() + ", subdivided N = " + vertexBuffer.capacity());
		}
	}

}
