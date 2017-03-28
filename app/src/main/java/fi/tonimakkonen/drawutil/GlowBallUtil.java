package fi.tonimakkonen.drawutil;

import javax.microedition.khronos.opengles.GL10;

import java.nio.FloatBuffer;

import fi.tonimakkonen.bufferutil.*;

public class GlowBallUtil {
	
	private FloatBuffer vertexPos = null;
	private FloatBuffer vertexCol = null;
	
	private float [] matrix = new float[16];
	
	private int numEdges = 0;
	
	public void init(int edges, float cr, float cg, float cb, float ca, float er, float eg, float eb, float ea) {
		
		numEdges = edges;
		
		vertexPos = BufferUtil.createFloatBuffer(3*(edges + 2));
		vertexCol = BufferUtil.createFloatBuffer(4*(edges+2));
		
		// center of triangle fan
		
		vertexPos.put(0.0f);
		vertexPos.put(0.0f);
		vertexPos.put(0.0f);
		
		vertexCol.put(cr);
		vertexCol.put(cg);
		vertexCol.put(cb);
		vertexCol.put(ca);
		
		// edges 
		
		for(int i = 0; i <= edges; i++) {
			
			float angle = 2.0f*(float)Math.PI*(float)i/(float)edges;
			float x = (float)Math.cos(angle);
			float y = (float)Math.sin(angle);
			
			vertexPos.put(x);
			vertexPos.put(y);
			vertexPos.put(0.0f);
			
			vertexCol.put(er);
			vertexCol.put(eg);
			vertexCol.put(eb);
			vertexCol.put(ea);
		}
		
		vertexPos.position(0);
		vertexCol.position(0);
		
	}
	
	// Set up open gl mode needed for drawing tiles or sprites
	public void setGLStateTAM1(GL10 gl, int sfactor, int dfactor) {
			
		// pass verteces, tex coords & color arrays
  		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
  		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
  		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
	  		
  		// Disable textures
  		gl.glDisable(GL10.GL_TEXTURE_2D);
	  		
  		// Set blend
  		gl.glEnable(GL10.GL_BLEND);
  		gl.glBlendFunc(sfactor, dfactor);
	}
	
	// Draw the glow ball
	public void draw(GL10 gl, float x0, float y0, float r) {
		
		// Define tghe matrix
		gl.glPushMatrix();
		matrix[0] = r;    matrix[4] = 0.0f; matrix[8] = 0.0f; matrix[12]= x0;
		matrix[1] = 0.0f; matrix[5] = r;    matrix[9] = 0.0f; matrix[13]= y0;
		matrix[2] = 0.0f; matrix[6] = 0.0f; matrix[10]= 0.0f; matrix[14]= 0.0f;
		matrix[3] = 0.0f; matrix[7] = 0.0f; matrix[11]= 0.0f; matrix[15]= 1.0f;
		gl.glMultMatrixf(matrix, 0);
		
		// Define the vertex position FloatBuffer
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexPos);
		// vertex col FB
		gl.glColorPointer(4, GL10.GL_FLOAT, 0, vertexCol);
		
		// Draw it..
		gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, numEdges+2);
		
		// Return to previous matrix
		gl.glPopMatrix();
	}

}
