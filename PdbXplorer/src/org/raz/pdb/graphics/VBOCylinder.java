

package org.raz.pdb.graphics;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.util.Log;


public class VBOCylinder extends Renderable {
	static int faceVBO, vertexVBO, vertexNormalVBO, faceCount;
	Line line;

	public VBOCylinder(float x1, float y1, float z1, float x2, float y2, float z2, float radius, Color color) {
		objectColor = color;
		//		colorBuffer = SphereGeometry.getFaceNormalBuffer();
		//		vertexColors = true;

		double dist = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2));
		if (dist < 0.001) return;
		
		posx = x1; posy = y1; posz = z1;
		if (Math.abs(x1 - x2) > 0.0001 || Math.abs(y1 - y2) > 0.001){
			rot = (float) (180 / Math.PI * Math.acos((z2 - z1) / dist));
			rotx = y1 - y2;
			roty = x2 - x1;
			rotz = 0;
		} else {
			rot = (float) (180 / Math.PI * Math.acos((z2 - z1) / dist));
			rotx = 1;
			roty = 0;
			rotz = 0;
		}
		
		scalex = scaley = radius; scalez = (float) dist;
		
		line = new Line(new float[]{x1, y1, z1, x2, y2, z2});
		line.objectColor = color;
		line.width = radius * 3;
	}
	
	public static void prepareVBO(GL11 gl) {
		int [] vbo = new int[3];
		gl.glGenBuffers (3, vbo, 0);

		FloatBuffer vertexBuffer = CylinderGeometry.getVertexBuffer();
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vbo[0]);
		vertexBuffer.position(0);
		gl.glBufferData(GL11.GL_ARRAY_BUFFER, vertexBuffer.capacity() * 4, vertexBuffer, GL11.GL_STATIC_DRAW);
		vertexVBO = vbo[0];
		Log.d("VBOCylinder", "vertex size " + vertexBuffer.capacity());
		FloatBuffer vertexNormalBuffer = CylinderGeometry.getVertexNormalBuffer();
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vbo[1]);
		gl.glBufferData(GL11.GL_ARRAY_BUFFER, vertexNormalBuffer.capacity() * 4, vertexNormalBuffer, GL11.GL_STATIC_DRAW);
		vertexNormalVBO = vbo[1];
		Log.d("VBOCylinder", "vertexNormals OK " + vertexNormalVBO);

		ShortBuffer faceBuffer = CylinderGeometry.getFaceBuffer();
		gl.glBindBuffer (GL11.GL_ELEMENT_ARRAY_BUFFER, vbo[2]);
		gl.glBufferData (GL11.GL_ELEMENT_ARRAY_BUFFER, faceBuffer.capacity() * 2, faceBuffer, GL11.GL_STATIC_DRAW);
		faceVBO = vbo[2];
		Log.d("VBOCylinder", "face OK " + faceVBO);
		faceCount = faceBuffer.capacity();

		// unbind -- IMPORTANT! otherwise, GL will crash!
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
		gl.glBindBuffer (GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	public void render(GL10 _gl, GLView view) {	
//		if (view.isMoving) {
//			line.render(_gl, view);
//			return;
//			
//		}
		
		GL11 gl = (GL11)_gl;

		gl.glPushMatrix();
		setMatrix(gl);
//		drawChildren(gl);

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
		//		gl.glDrawArrays(GL11.GL_LINE_STRIP, 0, 300);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
		gl.glBindBuffer (GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
//		gl.glDisable(GL10.GL_CULL_FACE);
		
		gl.glPopMatrix();
	}
}