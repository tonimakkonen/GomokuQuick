package fi.tonimakkonen.drawutil;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import fi.tonimakkonen.bufferutil.BufferUtil;

/**
 * A utility class for drawing spheres in OpenGL ES 1.0.
 */
public class SpriteUtil {

    // Are static variables initialized?
    private static boolean static_initialized = false;
    // Static float buffers
    private static FloatBuffer fb_tc_square; // static float buffers for tex coord square
    private static FloatBuffer fb_vc_white;  // white (alfa = 1) color
    // Float Buffer for vertex position (always temp)S
    private FloatBuffer fb_vp; // vertex positions
    private FloatBuffer fb_tc; // tex coord
    private FloatBuffer fb_vc; // vertex col
    // FloatBuffer for current tex coord & vertex color ("pointer")
    private FloatBuffer fb_cur_tc;
    private FloatBuffer fb_cur_vc;

    // Current effects //
    // direction vectors
    private float dir_x = 1.0f, dir_y = 0.0f;
    private float up_x = 0.0f, up_y = 1.0f;

    // Initializer
    public SpriteUtil() {

        // Init static elements, if this has not been done
        if (!static_initialized) initStatic();

        // Init per instance elements
        fb_vp = BufferUtil.createFloatBuffer(4 * 3);
        fb_tc = BufferUtil.createFloatBuffer(4 * 2);
        fb_vc = BufferUtil.createFloatBuffer(4 * 4);

        // set current tex coord and vertex colors
        fb_cur_tc = fb_tc_square;
        fb_cur_vc = fb_vc_white;
    }

    // Init static members
    private static void initStatic() {

        // Set texture coords for square
        fb_tc_square = BufferUtil.createFloatBuffer(4 * 2);
        fb_tc_square.put(0.0f);
        fb_tc_square.put(0.0f); // lower left
        fb_tc_square.put(1.0f);
        fb_tc_square.put(0.0f); // lower right
        fb_tc_square.put(0.0f);
        fb_tc_square.put(1.0f); // top left
        fb_tc_square.put(1.0f);
        fb_tc_square.put(1.0f); // top right
        fb_tc_square.position(0);

        // Init white color
        fb_vc_white = BufferUtil.createFloatBuffer(4 * 4);
        fb_vc_white.put(1.0f);
        fb_vc_white.put(1.0f);
        fb_vc_white.put(1.0f);
        fb_vc_white.put(1.0f);
        fb_vc_white.put(1.0f);
        fb_vc_white.put(1.0f);
        fb_vc_white.put(1.0f);
        fb_vc_white.put(1.0f);
        fb_vc_white.put(1.0f);
        fb_vc_white.put(1.0f);
        fb_vc_white.put(1.0f);
        fb_vc_white.put(1.0f);
        fb_vc_white.put(1.0f);
        fb_vc_white.put(1.0f);
        fb_vc_white.put(1.0f);
        fb_vc_white.put(1.0f);
        fb_vc_white.position(0);

        static_initialized = true;
    }

    // TODO: Define blending better
    // Set up open gl mode needed for drawing tiles or sprites
    public void setGLStateTAM1(GL10 gl, boolean useblend) {

        // pass verteces, tex coords & color arrays
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        // enable 2D texture
        gl.glEnable(GL10.GL_TEXTURE_2D);

        // set blend
        if (useblend) {
            gl.glEnable(GL10.GL_BLEND);
            gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        } else {
            gl.glDisable(GL10.GL_BLEND);
        }

        // for safety, set color to one
        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

    }

    // draw a sprite (centered)
    public void draw(GL10 gl, float cx, float cy, float width, float height) {

        // calculate corners
        width = width * 0.5f; // from full width to half width
        height = height * 0.5f;

        // lower left
        float ll_x = cx - dir_x * width - up_x * height;
        float ll_y = cy - dir_y * width - up_y * height;
        // lower right
        float lr_x = cx + dir_x * width - up_x * height;
        float lr_y = cy + dir_y * width - up_y * height;
        // top left
        float tl_x = cx - dir_x * width + up_x * height;
        float tl_y = cy - dir_y * width + up_y * height;
        // top right
        float tr_x = cx + dir_x * width + up_x * height;
        float tr_y = cy + dir_y * width + up_y * height;

        // put positions in vertex pos float buffer
        fb_vp.put(ll_x);
        fb_vp.put(ll_y);
        fb_vp.put(0.0f);
        fb_vp.put(lr_x);
        fb_vp.put(lr_y);
        fb_vp.put(0.0f);
        fb_vp.put(tl_x);
        fb_vp.put(tl_y);
        fb_vp.put(0.0f);
        fb_vp.put(tr_x);
        fb_vp.put(tr_y);
        fb_vp.put(0.0f);
        fb_vp.position(0);

        // Define the vertex position FloatBuffer
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, fb_vp);

        // Set the texture coord to basic square
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, fb_cur_tc);

        // Set the color tex coord to one
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, fb_cur_vc);

        // Assume the texture has been set (done outside this class), and enabled (can be done with this class), etc..

        // Assume the blend mode has been set (done in this class)

        // Draw the sprite
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);


    }

    // draw a tile
    public void drawTile(GL10 gl, float cx, float cy) {

    }

    // Effects //

    // reset effects
    public void resetEffects() {

        // Direction vectors (no angle)
        dir_x = 1.0f;
        dir_y = 0.0f;
        up_x = 0.0f;
        up_y = 1.0f;

        // texture coordinates (just a square)
        fb_cur_tc = fb_tc_square;

        // color array (just normal white)
        fb_cur_vc = fb_vc_white;
    }

    // Define the angle of sprite drawing
    public void setAngle(float a) {
        dir_x = (float) Math.cos(a);
        dir_y = (float) Math.sin(a);
        up_x = -dir_y;
        up_y = dir_x;
    }

    // TODO: Tilt mechanism
    public void setTilt(float tx, float ty) {

    }

    // set non-white color
    public void setColorUniform(float sr, float sg, float sb, float sa) {

        // copy values to Float Buffer
        fb_vc.put(sr);
        fb_vc.put(sg);
        fb_vc.put(sb);
        fb_vc.put(sa);
        fb_vc.put(sr);
        fb_vc.put(sg);
        fb_vc.put(sb);
        fb_vc.put(sa);
        fb_vc.put(sr);
        fb_vc.put(sg);
        fb_vc.put(sb);
        fb_vc.put(sa);
        fb_vc.put(sr);
        fb_vc.put(sg);
        fb_vc.put(sb);
        fb_vc.put(sa);
        fb_vc.position(0);

        // use this float buffer
        fb_cur_vc = fb_vc;

    }

    // set to white color
    public void setColorWhite() {
        fb_cur_vc = fb_vc_white;
    }


}
