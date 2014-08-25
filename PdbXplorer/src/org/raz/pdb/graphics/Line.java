

package org.raz.pdb.graphics;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;


public class Line extends Renderable {
	public float width = 2;
	boolean discrete = false;

	public Line(ArrayList<Vector3> points) {
		vertexBuffer = Geometry.getFloatBuffer(Geometry.fromVectorArrayList(points));
	}
	
	public Line(float[] points) {
		vertexBuffer = Geometry.getFloatBuffer(points);
	}
	
	public Line(ArrayList<Vector3> points, ArrayList<Color> colors) {
		if (points.size() > 0) {
			vertexBuffer = Geometry.getFloatBuffer(Geometry.fromVectorArrayList(points));
			colorBuffer = Geometry.colorsToFloatBuffer(colors, 1);
			vertexColors = true;
		}
	}
	
	public Line() {
	}
	
	public void render(GL10 gl, GLView view) {
		gl.glPushMatrix();
		setMatrix(gl);
//		drawChildren(gl);
		gl.glDisable(GL10.GL_LIGHTING);
		
		if (vertexBuffer != null) {
			gl.glLineWidth(width);
			if (vertexColors && colorBuffer != null) { 
				gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
				gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);
			} else {
				gl.glColor4f(objectColor.r, objectColor.g, objectColor.b, objectColor.a);
			}

			gl.glVertexPointer(3,GL10.GL_FLOAT, 0, vertexBuffer);
			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			if (discrete) {
				gl.glDrawArrays(GL10.GL_LINES, 0, vertexBuffer.capacity() / 3);
			} else {
				gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, vertexBuffer.capacity() / 3);
			}
			gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
			
			if (vertexColors) {
				gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
			}
		}
		gl.glPopMatrix();
		gl.glEnable(GL10.GL_LIGHTING);
	}
}
