package fi.tonimakkonen.drawutil;

import javax.microedition.khronos.opengles.GL10;

import fi.tonimakkonen.resutil.LoadableResource;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

public class TextureGL implements LoadableResource {
	
	private int openGLTex[] = null; // OpenGL texture pointer
	private int resID = -1;        // image ID in resources
	
	// Is loaded?
	@Override
	public boolean isLoaded() {
		return openGLTex != null;
	}
	
	@Override
	public boolean isDefined() {
		return resID != -1;
	}

	
	// Define how to load
	public void defineResource(int iid) {
		resID = iid;
	}
	
	// Load resources
	// This will bind the texture
	@Override
	public boolean loadThisResource(Resources res, GL10 gl) {
		load(res, resID, gl);
		return true;
	}
	
	private void load(Resources res, int idForImage, GL10 gl) {

		// Get the bitmap
		Bitmap bitmap;
  		bitmap = BitmapFactory.decodeResource(res, idForImage);
  		
  		// Create new texture
  		openGLTex = new int[1];
  		
  		// Create the texture
  		gl.glGenTextures(1, openGLTex, 0);
  	    gl.glBindTexture(GL10.GL_TEXTURE_2D, openGLTex[0]);
  	    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST_MIPMAP_LINEAR);
  	    //gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
  	    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
  	    
  	    int level = 0;
  	    while(bitmap.getWidth() >= 1) {
  	    	// Save mipmap level
  	    	GLUtils.texImage2D(GL10.GL_TEXTURE_2D, level, bitmap, 0);
  	    	// Create next mipmap level
  	    	if(bitmap.getWidth() == 1) break;
  	    	Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()/2, bitmap.getHeight()/2, true);
  	    	bitmap.recycle();
  	    	bitmap = newBitmap;
  	    	level += 1;
  	    }
  		
  	    // free memory
  		bitmap.recycle();
	}
	
	// Bind this texture
	public void bind(GL10 gl) {
		gl.glBindTexture(GL10.GL_TEXTURE_2D, openGLTex[0]);
	}

}