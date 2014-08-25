

package org.raz.pdb.graphics;

public class Quaternion {
	public float x = 0, y = 0, z = 0, w = 1;
	
	public static Quaternion multiply(Quaternion p, Quaternion q) {
		Quaternion ret = new Quaternion();
		
		ret.x = p.x * q.w + p.y * q.z - p.z * q.y + p.w * q.x; // 1
		ret.y = -p.x * q.z + p.y * q.w + p.z * q.x + p.w * q.y; // 2
		ret.z = p.x * q.y - p.y * q.x + p.z * q.w + p.w * q.z; //3
		ret.w = -p.x * q.x - p.y * q.y - p.z * q.z + p.w * q.w; // 0
		 
		return ret;
	}
	
	public Vector3 rotateVector(Vector3 vec) {
		Quaternion q = Quaternion.multiply(Quaternion.multiply(this.clone().invert(), new Quaternion(vec.x, vec.y, vec.z, 0)), this);
		
		return new Vector3(q.x, q.y, q.z);
	}
	
	public Quaternion clone() {
		Quaternion q = new Quaternion(x, y, z, w);
		return q;
	}
	
	public Quaternion invert() {
		this.x *= -1;
		this.y *= -1;
		this.z *= -1;
		return this;
	}
	
	public float getAngle() {
		return (float)Math.acos(this.w) * 2;
	}
	
	public Vector3 getAxis() {
		float angle = getAngle();
		float sin = (float)Math.sin(angle / 2);
		
		if (Math.abs(sin) < 0.001) return new Vector3(1, 0, 0);
		
		Vector3 ret = new Vector3();
		ret.x = this.x / sin;
		ret.y = this.y / sin;
		ret.z = this.z / sin;
		return ret;
	}
	
	public Quaternion() {
		
	}
	
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ", " + w + ")";
	}
	
	public Quaternion(float x, float y, float z, float w) {
		this.x = x; this.y = y; this.z = z; this.w = w;
	}
}
