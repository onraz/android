

package org.raz.pdb.graphics;

import javax.microedition.khronos.opengles.GL10;

public class PointSphere extends Renderable{
	public PointSphere(float x, float y, float z, float radius, Color c) {
		vertexBuffer = Geometry.getFloatBuffer(new float[] {0, 0, 0});
		scalex = scaley = scalez = radius;
		posx = x; posy = y; posz = z;
		objectColor = c;
	}
	
	public void render(GL10 gl, GLView view) {
		gl.glPushMatrix();
		setMatrix(gl);
		
		gl.glEnable(GL10.GL_ALPHA_TEST);
		gl.glPointSize(scalex * 15);
		gl.glColor4f(objectColor.r, objectColor.g, objectColor.b, objectColor.a);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glDrawArrays(GL10.GL_POINTS, 0, vertexBuffer.capacity());
		
		gl.glDisable(GL10.GL_ALPHA_TEST);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glPopMatrix();
	}
	
}
