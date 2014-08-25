

package org.raz.pdb.graphics;

import android.view.MotionEvent;

public class MultitouchWrapper {
	public static int getPointerCount(MotionEvent e) {
		return e.getPointerCount();
	}
	
	public static float getX(MotionEvent e, int id) {
		return e.getX(id);
	}
	
	public static float getY(MotionEvent e, int id) {
		return e.getY(id);
	}
}
