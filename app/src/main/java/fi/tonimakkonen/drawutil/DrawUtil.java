
package fi.tonimakkonen.drawutil;

import javax.microedition.khronos.opengles.GL10;

import java.nio.FloatBuffer;

import fi.tonimakkonen.bufferutil.*;

/**
 * General OpenGL ES 1.0 drawing utility class.
 *
 * TODO: This class requires quite a lot refactoring.
 */
public class DrawUtil {
	
	// Variables
	private static FloatBuffer fb_zero;   // just point (0 0 0)
	private static FloatBuffer fb_line;   // line (0 0 0) -> (1 0 0)
	private static FloatBuffer fb_tri;    // tri  (0 0 0) (1 0 0) (0 1 0)
	private static FloatBuffer fb_square; // sq   (0 0 0) (1 0 0) (1 1 0) (0 1 0)
	
	// Circles with various number of edges
	private static FloatBuffer fb_sphere8;
	private static FloatBuffer fb_sphere16;
	private static FloatBuffer fb_sphere32;
	private static FloatBuffer fb_sphere64;
	
	private static FloatBuffer fb_temp_6; // temp float buffer
	
	// temporary FloatBuffer
	private static FloatBuffer fb_temp_vp; // vertex position
	private static FloatBuffer fb_temp_tc; // texture coords
	private static FloatBuffer fb_temp_vc; // vertex colors
	
	// specific FloatBuffer for tex coords
	private static FloatBuffer fb_tc_square; // texture coord for a square
	
	// specific FloatBuffers for vertex colors
	private static FloatBuffer fb_vc_white; // vertex colors all white
	
	private static float [] matrix;
	
	// Initialize this class
	public static void init() {
		
		// init the temp matrix
		matrix = new float[16];
		
		// Initialize buffers for basic objects
		
		// create empty float buffer
		fb_temp_6 = BufferUtil.createFloatBuffer(6);
		
		// Temporary FloatBuffers for vertex pos, tex coord, & vertex col
		fb_temp_vp = BufferUtil.createFloatBuffer(4*3);
		fb_temp_tc = BufferUtil.createFloatBuffer(4*2);
		fb_temp_vc = BufferUtil.createFloatBuffer(4*4);
		
		// FloatBuffer for specified tex coords
		fb_tc_square = BufferUtil.createFloatBuffer(4*2);
		fb_tc_square.put(0.0f); fb_tc_square.put(0.0f); // lower left
		fb_tc_square.put(1.0f); fb_tc_square.put(0.0f); // lower right
		fb_tc_square.put(0.0f); fb_tc_square.put(1.0f); // top left
		fb_tc_square.put(1.0f); fb_tc_square.put(1.0f); // top right
		fb_tc_square.position(0);
		// TODO: Make sure this is ok!!
		
		// FloatBuffer for specific vertex colors
		fb_vc_white = BufferUtil.createFloatBuffer(4*4);
		fb_vc_white.put(1.0f); fb_vc_white.put(1.0f); fb_vc_white.put(1.0f); fb_vc_white.put(1.0f);
		fb_vc_white.put(1.0f); fb_vc_white.put(1.0f); fb_vc_white.put(1.0f); fb_vc_white.put(1.0f);
		fb_vc_white.put(1.0f); fb_vc_white.put(1.0f); fb_vc_white.put(1.0f); fb_vc_white.put(1.0f);
		fb_vc_white.put(1.0f); fb_vc_white.put(1.0f); fb_vc_white.put(1.0f); fb_vc_white.put(1.0f);
		fb_vc_white.position(0);
		
		// just zero
		fb_zero = BufferUtil.createFloatBuffer(3*1);
		float[] zero_vert = {0.0f, 0.0f, 0.0f};
		fb_zero.put(zero_vert, 0, 3*1);
		fb_zero.position(0);
		
		// Simple line
		fb_line = BufferUtil.createFloatBuffer(3*2);
		float[] line_vert = {0.0f, 0.0f, 0.0f,  1.0f, 0.0f, 0.0f};
		fb_line.put(line_vert, 0, 3*2);
		fb_line.position(0);
		
		// Simple triangle
		fb_tri = BufferUtil.createFloatBuffer(3*3);
		float[] tri_vert = {0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f};
		fb_tri.put(tri_vert, 0, 3*3);
		fb_tri.position(0);
		
		// Simple triangle
		fb_square = BufferUtil.createFloatBuffer(3*4);
		float[] square_vert = {0.0f, 0.0f, 0.0f,   1.0f, 0.0f, 0.0f,   0.0f, 1.0f, 0.0f,   1.0f, 1.0f, 0.0f};
		fb_square.put(square_vert, 0, 3*4);
		fb_square.position(0);
		
		// spheres
		fb_sphere8  = initCirc(8);
		fb_sphere16 = initCirc(16);
		fb_sphere32 = initCirc(32);
		fb_sphere64 = initCirc(64);
	}
	
	// Init circle shape with various number of edges
	private static FloatBuffer initCirc(int numedges) {
		FloatBuffer fb = BufferUtil.createFloatBuffer(3*numedges);
		for(int i = 0; i < numedges; i++) {
			float x = (float)Math.cos(2.0f*Math.PI*((float)i)/numedges);
			float y = (float)Math.sin(2.0f*Math.PI*((float)i)/numedges);
			fb.put(x);
			fb.put(y);
			fb.put(0.0f);

		}
		fb.position(0);
		return fb;
		
	}
	
	// Draw line
	public static void Line(GL10 gl, float x0, float y0, float z0, float x1, float y1, float z1) {
		
		fb_temp_6.put(x0);
		fb_temp_6.put(y0);
		fb_temp_6.put(z0);
		fb_temp_6.put(x1);
		fb_temp_6.put(y1);
		fb_temp_6.put(z1);
		fb_temp_6.position(0);
	
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, fb_temp_6);
		gl.glDrawArrays(GL10.GL_LINES, 0, 2);
		
		/*
		gl.glPushMatrix();
		
		// Get the matrix
		float[] matrix = {x1-x0, y1-y0, z1-z0, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f,  x0, y0, z0, 1.0f};
		gl.glMultMatrixf(matrix, 0);
		
		// Draw thw tri
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, fb_line);
		gl.glDrawArrays(GL10.GL_LINES, 0, 2);
		
		gl.glPopMatrix();
		*/
		
	}
	
	// Draw triangle
	public static void TriSimple(GL10 gl, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2) {
		
		gl.glPushMatrix();
		
		// Get the matrix
		float[] matrix = {x1-x0, y1-y0, z1-z0, 0.0f,  x2-x0, y2-y0, z2-z0, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f,  x0, y0, z0, 1.0f};
		gl.glMultMatrixf(matrix, 0);
		
		// Draw thw tri
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, fb_tri);
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);
		
		gl.glPopMatrix();
		
	}
	
	// Draw square
	public static void Para(GL10 gl, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2) {
		
		gl.glPushMatrix();
		
		// Get the matrix
		float[] matrix = {x1-x0, y1-y0, z1-z0, 0.0f,  x2-x0, y2-y0, z2-z0, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f,  x0, y0, z0, 1.0f};
		gl.glMultMatrixf(matrix, 0);
		
		// Draw thw parallelogram
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, fb_square);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
		
		gl.glPopMatrix();
		
	}
	
	// Set up circular matrix for drawing a 2d circle/sphere
	private static void setCircMatrix(float x0, float y0, float r) {
		matrix[0] = r;    matrix[4] = 0.0f; matrix[8] = 0.0f; matrix[12]= x0;
		matrix[1] = 0.0f; matrix[5] = r;    matrix[9] = 0.0f; matrix[13]= y0;
		matrix[2] = 0.0f; matrix[6] = 0.0f; matrix[10]= 0.0f; matrix[14]= 0.0f;
		matrix[3] = 0.0f; matrix[7] = 0.0f; matrix[11]= 0.0f; matrix[15]= 1.0f;
	}
	
	// Circle with 8 sides
	public static void Circ8(GL10 gl, float x0, float y0, float r) {
		// set up matrix
		gl.glPushMatrix();
		setCircMatrix(x0, y0, r);
		gl.glMultMatrixf(matrix, 0);
		
		// Draw the circle
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, fb_sphere8);
		gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, 8);
		
		// back to old matrix
		gl.glPopMatrix();
	}
	
	// Circle with 16 sides
	public static void Circ16(GL10 gl, float x0, float y0, float r) {
		// set up matrix
		gl.glPushMatrix();
		setCircMatrix(x0, y0, r);
		gl.glMultMatrixf(matrix, 0);
		
		// Draw the circle
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, fb_sphere16);
		gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, 16);
		
		// back to old matrix
		gl.glPopMatrix();
	}
	
	public static void Circ32(GL10 gl, float x0, float y0, float r) {
		// set up matrix
		gl.glPushMatrix();
		setCircMatrix(x0, y0, r);
		gl.glMultMatrixf(matrix, 0);
		
		// Draw the circle
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, fb_sphere32);
		gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, 32);
		
		// back to old matrix
		gl.glPopMatrix();
	}
	
	public static void Circ64(GL10 gl, float x0, float y0, float r) {
		// set up matrix
		gl.glPushMatrix();
		setCircMatrix(x0, y0, r);
		gl.glMultMatrixf(matrix, 0);
		
		// Draw the circle
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, fb_sphere64);
		gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, 64);
		
		// back to old matrix
		gl.glPopMatrix();
	}
	
	public static void Circ8Filled(GL10 gl, float x0, float y0, float r) {
		// set up matrix
		gl.glPushMatrix();
		setCircMatrix(x0, y0, r);
		gl.glMultMatrixf(matrix, 0);
		
		// draw sphere
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, fb_sphere8);
		gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, 8);
		
		// old matrix
		gl.glPopMatrix();
	}
	
	public static void Circ16Filled(GL10 gl, float x0, float y0, float r) {
		// set up matrix
		gl.glPushMatrix();
		setCircMatrix(x0, y0, r);
		gl.glMultMatrixf(matrix, 0);
		
		// draw sphere
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, fb_sphere16);
		gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, 16);
		
		// old matrix
		gl.glPopMatrix();
	}
	
	public static void Circ32Filled(GL10 gl, float x0, float y0, float r) {
		// set up matrix
		gl.glPushMatrix();
		setCircMatrix(x0, y0, r);
		gl.glMultMatrixf(matrix, 0);
		
		// draw sphere
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, fb_sphere32);
		gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, 32);
		
		// old matrix
		gl.glPopMatrix();
	}
	
	public static void Circ64Filled(GL10 gl, float x0, float y0, float r) {
		// set up matrix
		gl.glPushMatrix();
		setCircMatrix(x0, y0, r);
		gl.glMultMatrixf(matrix, 0);
		
		// draw sphere
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, fb_sphere64);
		gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, 64);
		
		// old matrix
		gl.glPopMatrix();
	}
	
	
	// Draw "sprite"
	public static void drawSprite(GL10 gl, float cx, float cy, float width, float height, float ang) {
		
		// direction vector (pointing in positive x direction when angle = 0) 
		float dx = (float)Math.cos(ang);
		float dy = (float)Math.sin(ang);
		
		// direction vector "up"
		float ux = -dy;
		float uy = dx;
		
		// lower left
		float ll_x = cx - dx*width - ux*height;
		float ll_y = cy - dy*width - uy*height;
		// lower right
		float lr_x = cx + dx*width - ux*height;
		float lr_y = cy + dy*width - uy*height;
		// top left
		float tl_x = cx - dx*width + ux*height;
		float tl_y = cy - dy*width + uy*height;
		// top right
		float tr_x = cx + dx*width + ux*height;
		float tr_y = cy + dy*width + uy*height;
		
		// put positions in vertex pos float buffer
		fb_temp_vp.put(ll_x); fb_temp_vp.put(ll_y); fb_temp_vp.put(0.0f);
		fb_temp_vp.put(lr_x); fb_temp_vp.put(lr_y); fb_temp_vp.put(0.0f);
		fb_temp_vp.put(tl_x); fb_temp_vp.put(tl_y); fb_temp_vp.put(0.0f);
		fb_temp_vp.put(tr_x); fb_temp_vp.put(tr_y); fb_temp_vp.put(0.0f);
		fb_temp_vp.position(0);
		
		// Define the vertex position FloatBuffer
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, fb_temp_vp);
		
		// Set the texture coord to basic square
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, fb_tc_square);
		
		// Set the color tex coord to one
		gl.glColorPointer(4, GL10.GL_FLOAT, 0, fb_vc_white);
		
		// Draw the sprite
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
		
		
	}
}
