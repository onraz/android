

package org.raz.pdb.graphics;

public class Vector3 {
	public float x, y, z;
	
	public Vector3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void negate() {
		this.x *= -1;
		this.y *= -1;
		this.z *= -1;
	}
	
	public static float dot(Vector3 p, Vector3 q) {
		return p.x * q.x + p.y * q.y + p.z * q.z;
	}
	
	public static Vector3 cross(Vector3 p, Vector3 q) {
		return new Vector3(p.y * q.z - p.z * q.y, p.z * q.x - p.x * q.z, p.x * q.y - p.y * q.x);
	}
	
	public Vector3() {
		// TODO Auto-generated constructor stub
	}

	public static double norm(float x, float y, float z) {
		return Math.sqrt(x * x + y * y + z * z);
	}
	
	public Vector3 normalize() {
		float norm = (float)norm(this.x, this.y, this.z);
		this.x /= norm;
		this.y /= norm;
		this.z /= norm;
		return this;
	}
	
	public Vector3 multiplyScalar(float s) {
		this.x *= s;
		this.y *= s;
		this.z *= s;
		return this;
	}
}
