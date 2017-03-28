package fi.tonimakkonen.resutil;

import javax.microedition.khronos.opengles.GL10;

import android.content.res.Resources;

public interface LoadableResource {
	
	// Is this resource loaded?
	public boolean isLoaded();
	public boolean isDefined();
	
	// Define how to load this resource..
	// Implementation specific
	
	// Load now (give the most commonly used classes for resources..)
	public boolean loadThisResource(Resources res, GL10 gl);

}
