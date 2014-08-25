

package org.raz.pdb.graphics;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

public class VBOSpheres extends VBOSphere {
	Vector3 points[];
	Color colors[];
	Float radii[];
	
	public VBOSpheres(ArrayList<Vector3> points, ArrayList<Color> colors, ArrayList<Float> radii) {
		this.points = (Vector3[]) points.toArray(new Vector3[]{});
		this.colors = (Color[]) colors.toArray(new Color[]{});
		this.radii = (Float[])radii.toArray(new Float[]{});		
	}
	
	public void render(GL10 _gl, GLView view) {
		if (points == null) return;
		
		GL11 gl = (GL11)_gl;

		gl.glPushMatrix();
//		setMatrix(gl);

//		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vertexVBO);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, 0);
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vertexNormalVBO);
		gl.glNormalPointer(GL10.GL_FLOAT, 0, 0);
		gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, faceVBO);
		for (int i = 0, lim = points.length; i < lim; i++) {
			Color c = colors[i];
			Vector3 p = points[i];
			gl.glColor4f(c.r, c.g, c.b, c.a);
			float r = radii[i];
			gl.glPushMatrix();
			gl.glTranslatef(p.x, p.y, p.z);
			gl.glScalef(r, r, r);
			gl.glDrawElements(GL10.GL_TRIANGLES, faceCount, GL10.GL_UNSIGNED_SHORT, 0);
			gl.glPopMatrix();
		}
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
		gl.glBindBuffer (GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
//		gl.glDisable(GL10.GL_CULL_FACE);
		
		gl.glPopMatrix();
	}
}
