package fi.tonimakkonen.bufferutil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.DoubleBuffer;

public class BufferUtil {
	
	// float //
	
	// Create a simple float buffer with requested size
	public static FloatBuffer createFloatBuffer(int size) {
        ByteBuffer bb = ByteBuffer.allocateDirect(size * 4);
        bb.order(ByteOrder.nativeOrder());
        return bb.asFloatBuffer();
	}
	 
	// Create a float buffer from float array 
	public static FloatBuffer createFloatBuffer(float [] arr) {
		FloatBuffer fb = createFloatBuffer(arr.length);
		fb.put(arr);
		fb.position(0);
		return fb;
	}
	
	
	// double //
	
	
	// Create a simple float buffer with requested size
	public static DoubleBuffer createDoubleBuffer(int size) {
        ByteBuffer bb = ByteBuffer.allocateDirect(size * 8);
        bb.order(ByteOrder.nativeOrder());
        return bb.asDoubleBuffer();
	}
	 
	// Create a float buffer from float array 
	public static DoubleBuffer createDoubleBuffer(double [] arr) {
		DoubleBuffer fb = createDoubleBuffer(arr.length);
		fb.put(arr);
		fb.position(0);
		return fb;
	}

}
