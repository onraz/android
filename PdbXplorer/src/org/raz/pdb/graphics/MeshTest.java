

package org.raz.pdb.graphics;

import javax.microedition.khronos.opengles.GL10;

public class MeshTest extends Renderable {
	public MeshTest() {
		float vertices[] = {0, 0, 0,    0, 5, 0,    5, 0, 0,    5, 5, 0,    5, 0, 0,    5, 5, 0,    5, 0, -5,    5, 5, -5};
		float normals[] = {0, 0, -1,    0, 0, -1,   0, 0, -1,   0, 0, -1,   1, 0, 0,    1, 0, 0,   1,  0, 0,   1, 0, 0};
		short faces[] = {0, 1, 2,  2, 1, 3,  4, 5, 6,  6, 5, 7};
		
		vertexBuffer = Geometry.getFloatBuffer(vertices);
		faceBuffer = Geometry.getShortBuffer(faces);
		vertexNormalBuffer = Geometry.getFloatBuffer(normals);
	}
	
	public void render(GL10 gl) {
		gl.glPushMatrix();
		setMatrix(gl);
		
		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glNormalPointer(GL10.GL_FLOAT, 0, vertexNormalBuffer);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
//		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
//		gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);
		gl.glDrawElements(GL10.GL_TRIANGLES, faceBuffer.capacity(), GL10.GL_UNSIGNED_SHORT, faceBuffer);
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		/*
		if (vertexColors && colorBuffer != null) {
			gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
			gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);
		} else {
			gl.glColor4f(objectColor.r, objectColor.g, objectColor.b, objectColor.a);
		}		
		if (vertexBuffer != null && faceBuffer != null) {
			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
			if (faceNormalBuffer != null) {
				gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
				gl.glNormalPointer(GL10.GL_FLOAT, 0, faceNormalBuffer);
			}
			gl.glDrawElements(GL10.GL_TRIANGLES, faceBuffer.capacity(), GL10.GL_UNSIGNED_SHORT, faceBuffer);
			gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
		}
		if (vertexColors) {
			gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		}*/
		gl.glPopMatrix();
	}
}
