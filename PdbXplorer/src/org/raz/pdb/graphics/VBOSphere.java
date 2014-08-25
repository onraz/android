package org.raz.pdb.graphics;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

public class VBOSphere extends Renderable {
	static int faceVBO, vertexVBO, vertexNormalVBO, faceCount;

	protected VBOSphere() {
		
	}
	
	public VBOSphere(float x, float y, float z, float radius, Color c) {
		scalex = scaley = scalez = radius;
		posx = x; posy = y; posz = z;
		objectColor = c;
	}
	
	public static void prepareVBO(GL11 gl, GLView view) {
		int [] vbo = new int[3];
		gl.glGenBuffers (3, vbo, 0);

		FloatBuffer vertexBuffer = SphereGeometry.getVertexBuffer();
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vbo[0]);
		vertexBuffer.position(0);
		gl.glBufferData(GL11.GL_ARRAY_BUFFER, vertexBuffer.capacity() * 4, vertexBuffer, GL11.GL_STATIC_DRAW);
		vertexVBO = vbo[0];
//		Log.d("VBOSphere", "vertex size " + vertexBuffer.capacity());
		FloatBuffer vertexNormalBuffer = SphereGeometry.getVertexNormalBuffer();
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vbo[1]);
		gl.glBufferData(GL11.GL_ARRAY_BUFFER, vertexNormalBuffer.capacity() * 4, vertexNormalBuffer, GL11.GL_STATIC_DRAW);
		vertexNormalVBO = vbo[1];
//		Log.d("VBOSphere", "vertexNormals OK " + vertexNormalVBO);

		ShortBuffer faceBuffer = SphereGeometry.getFaceBuffer();
		gl.glBindBuffer (GL11.GL_ELEMENT_ARRAY_BUFFER, vbo[2]);
		gl.glBufferData (GL11.GL_ELEMENT_ARRAY_BUFFER, faceBuffer.capacity() * 2, faceBuffer, GL11.GL_STATIC_DRAW);
		faceVBO = vbo[2];
//		Log.d("VBOSphere", "face OK " + faceVBO);
		faceCount = faceBuffer.capacity();

		// unbind -- IMPORTANT! otherwise, GL will crash!
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
		gl.glBindBuffer (GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	public void render(GL10 _gl, GLView view) {
		GL11 gl = (GL11)_gl;

		gl.glPushMatrix();
		setMatrix(gl);
//		drawChildren(gl);

//		Log.d("VBOSphere", "Face " + faceVBO  +  ", Count " + faceCount);

//		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glColor4f(objectColor.r, objectColor.g, objectColor.b, objectColor.a);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vertexVBO);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, 0);
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vertexNormalVBO);
		gl.glNormalPointer(GL10.GL_FLOAT, 0, 0);
		gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, faceVBO);
		gl.glDrawElements(GL10.GL_TRIANGLES, faceCount, GL10.GL_UNSIGNED_SHORT, 0);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
		gl.glBindBuffer (GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
		gl.glDisable(GL10.GL_CULL_FACE);
//		gl.glDisable(GL10.GL_CULL_FACE);
		
		gl.glPopMatrix();
	}
}