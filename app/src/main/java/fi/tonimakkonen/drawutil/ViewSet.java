package fi.tonimakkonen.drawutil;

import javax.microedition.khronos.opengles.GL10;

/**
 * Class used to set up a view in OpenGL ES 1.0.
 */
public class ViewSet {

    // set up gui view
    public static void setGUIView(GL10 gl, float w, float h) {
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrthof(0.0f, w, 0.0f, h, -1.0f, 1.0f);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    // set up "flyer" view
    public static void setFlyerView(GL10 gl, float viewdis, float viewfvd, float ratio, float height, float posx, float posy, float angle) {

        // do some calculations
        float left = -viewdis * ratio / 2.0f;
        float right = viewdis * ratio / 2.0f;
        float bottom = -viewdis / 2.0f + viewfvd;
        float top = viewdis / 2.0f + viewfvd;

        float mult = 0.01f;
        float near = height * mult;
        float far = height * 100.0f;


        // Set our field of view.
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustumf(left * mult, right * mult, bottom * mult, top * mult, near, far);

        // Set up camera location
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glRotatef(angle * 180.0f / (float) Math.PI, 0.0f, 0.0f, -1.0f);
        gl.glTranslatef(-posx, -posy, -height);

    }

}
