

package org.raz.pdb.graphics;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

public class MatRenderable extends Renderable {
	private ArrayList <float[]> matrices = new ArrayList<float[]>();

	public void addMatrix(float[] mat) {
		float matrix[] = new float[16];

		matrix[0] = mat[0];
		matrix[4] = mat[1];
		matrix[8] = mat[2];
		matrix[12] = mat[3];
		matrix[1] = mat[4];
		matrix[5] = mat[5];
		matrix[9] = mat[6];
		matrix[13] = mat[7];
		matrix[2] = mat[8];
		matrix[6] = mat[9];
		matrix[10] = mat[10];
		matrix[14] = mat[11];
		matrix[3] = mat[12];
		matrix[7] = mat[13];
		matrix[11] = mat[14];
		matrix[15] = mat[15];

		matrices.add(matrix);
	}

	public void render(GL10 gl, GLView view) {
		gl.glPushMatrix();
		setMatrix(gl);

		for (int i = 0, lim = matrices.size(); i < lim; i++) {
			gl.glPushMatrix();
			gl.glMultMatrixf(matrices.get(i), 0);
			drawChildren(gl, view);
			gl.glPopMatrix();
		}		

		gl.glPopMatrix();
	}
}
