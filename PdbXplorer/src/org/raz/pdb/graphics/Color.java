

package org.raz.pdb.graphics;

public class Color {
	public float r = 0.5f, g = 0.5f, b = 0.5f, a = 1;
	
	public Color (float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
	
	public Color() {
		
	}
	
	public Color(int c) {
		this.r = (float)(c >> 16) / 255;
		this.g = (float)((c >> 8) & 255) / 255;
		this.b = (float)(c & 255) / 255;
		this.a = 1;
	}

	public String toString() {
		return "(" + this.r + ", " + this.g + ", " + this.b + ")";
	}
	
	// Algorithm based on Three.js ( https://github.com/mrdoob/three.js/ )
	public Color setHSV(float h, float s, float v) {
		if (v == 0) return this;

		int mod = (int)(h * 6);
		float f = ( h * 6 ) - mod;
		float p = v * ( 1 - s );
		float q = v * ( 1 - ( s * f ));
		float t = v * ( 1 - ( s * ( 1 - f )));

		switch (mod) {
		case 1: r = q; g = v; b = p; break;
		case 2: r = p; g = v; b = t; break;
		case 3: r = p; g = q; b = v; break;
		case 4: r = t; g = p; b = v; break;
		case 5: r = v; g = p; b = q; break;
		case 6: // fall through
		case 0: r = v; g = t; b = p; break;
		}
		return this;
	}
}
