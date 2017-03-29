package fi.tonimakkonen.gomoku;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.opengl.GLSurfaceView.Renderer;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import fi.tonimakkonen.bufferutil.BufferUtil;
import fi.tonimakkonen.drawutil.DrawUtil;
import fi.tonimakkonen.drawutil.GlowBallUtil;
import fi.tonimakkonen.drawutil.SpriteUtil;
import fi.tonimakkonen.drawutil.TextureGL;
import fi.tonimakkonen.drawutil.ViewSet;
import fi.tonimakkonen.gridutil.GridIndex;
import fi.tonimakkonen.hexutil.HexCalc;

/**
 * A OpenGl Rendeded for the Gomoku game
 */
public class GoRenderer implements Renderer {

    // Screen width & height
    float screenw, screenh;
    GoTileGraph boardState[];
    int lastTile = -1;
    int boardGeometry; // geometry of board (0 = normal, 1 = hex)

    // Board options
    int boardStyle;    // style of board (0 = pen&paper, 1 = go board
    // Graphical effects
    int boardVisualState[] = new int[GoSettings.goMaxSize]; // remnances of removed tiles
    float markTimer[] = new float[GoSettings.goMaxSize]; // how long has
    float viewx, viewy, vieww, viewh;

    // Location of view
    float zoomlevel = 1.0f; // zoom level
    // App resources
    Resources appres;

    // Graphics
    // textures for crosses and circles
    TextureGL texCross = new TextureGL();
    TextureGL texCirc = new TextureGL();
    SpriteUtil sprite = new SpriteUtil();
    // Notification (new ai mark)
    float noteX, noteY, noteTimer, noteFade;
    int noteTile = -1;
    float noteTileTimer = 0.0f;
    // AI wait marker
    boolean noteAI = false;
    float noteAITimer = 0.0f;
    // Next game marker
    boolean noteNextGame = false;
    float noteNextGameTimer = 0.0f;
    // win row notification
    GlowBallUtil glowBall = new GlowBallUtil();
    // Precalculated hex grid
    FloatBuffer hexGridLines;
    int hexGridNumVertex;
    // timing
    long timerLast = 0;

    GoRenderer(Resources res) {
        appres = res;

        boardState = new GoTileGraph[GoSettings.goMaxSize];
        for (int i = 0; i < GoSettings.goMaxSize; i++) {
            boardState[i] = new GoTileGraph();
        }

        // calculate hex grid
        int numlines = 3 * GoSettings.goNormalSide * GoSettings.goNormalSide + 3 * GoSettings.goNormalSide + 2 * (GoSettings.goNormalSide - 1) / 2;
        hexGridLines = BufferUtil.createFloatBuffer(3 * 2 * numlines);
        hexGridNumVertex = numlines * 2;

        for (int ix = 0; ix < GoSettings.goNormalSide; ix++) {
            for (int iy = 0; iy < GoSettings.goNormalSide; iy++) {

                // Get center of tile
                float cx = getHexCoordX(ix, iy);
                float cy = getHexCoordY(ix, iy);

                // line 1
                hexGridLines.put(cx + 40.0f * HexCalc.shapeX[0]);
                hexGridLines.put(cy + 40.0f * HexCalc.shapeY[0]);
                hexGridLines.put(0.0f);
                hexGridLines.put(cx + 40.0f * HexCalc.shapeX[1]);
                hexGridLines.put(cy + 40.0f * HexCalc.shapeY[1]);
                hexGridLines.put(0.0f);

                // line 2
                hexGridLines.put(cx + 40.0f * HexCalc.shapeX[1]);
                hexGridLines.put(cy + 40.0f * HexCalc.shapeY[1]);
                hexGridLines.put(0.0f);
                hexGridLines.put(cx + 40.0f * HexCalc.shapeX[2]);
                hexGridLines.put(cy + 40.0f * HexCalc.shapeY[2]);
                hexGridLines.put(0.0f);

                // Line 3
                hexGridLines.put(cx + 40.0f * HexCalc.shapeX[2]);
                hexGridLines.put(cy + 40.0f * HexCalc.shapeY[2]);
                hexGridLines.put(0.0f);
                hexGridLines.put(cx + 40.0f * HexCalc.shapeX[3]);
                hexGridLines.put(cy + 40.0f * HexCalc.shapeY[3]);
                hexGridLines.put(0.0f);

                // additional lines
                if (ix == 0) {

                    hexGridLines.put(cx + 40.0f * HexCalc.shapeX[3]);
                    hexGridLines.put(cy + 40.0f * HexCalc.shapeY[3]);
                    hexGridLines.put(0.0f);
                    hexGridLines.put(cx + 40.0f * HexCalc.shapeX[4]);
                    hexGridLines.put(cy + 40.0f * HexCalc.shapeY[4]);
                    hexGridLines.put(0.0f);
                }

                if (iy == 0) {

                    hexGridLines.put(cx + 40.0f * HexCalc.shapeX[4]);
                    hexGridLines.put(cy + 40.0f * HexCalc.shapeY[4]);
                    hexGridLines.put(0.0f);
                    hexGridLines.put(cx + 40.0f * HexCalc.shapeX[5]);
                    hexGridLines.put(cy + 40.0f * HexCalc.shapeY[5]);
                    hexGridLines.put(0.0f);

                    if (ix % 2 != 0) {
                        hexGridLines.put(cx + 40.0f * HexCalc.shapeX[3]);
                        hexGridLines.put(cy + 40.0f * HexCalc.shapeY[3]);
                        hexGridLines.put(0.0f);
                        hexGridLines.put(cx + 40.0f * HexCalc.shapeX[4]);
                        hexGridLines.put(cy + 40.0f * HexCalc.shapeY[4]);
                        hexGridLines.put(0.0f);
                        hexGridLines.put(cx + 40.0f * HexCalc.shapeX[5]);
                        hexGridLines.put(cy + 40.0f * HexCalc.shapeY[5]);
                        hexGridLines.put(0.0f);
                        hexGridLines.put(cx + 40.0f * HexCalc.shapeX[0]);
                        hexGridLines.put(cy + 40.0f * HexCalc.shapeY[0]);
                        hexGridLines.put(0.0f);
                    }
                }
                if (ix == GoSettings.goNormalSide - 1) {

                    hexGridLines.put(cx + 40.0f * HexCalc.shapeX[5]);
                    hexGridLines.put(cy + 40.0f * HexCalc.shapeY[5]);
                    hexGridLines.put(0.0f);
                    hexGridLines.put(cx + 40.0f * HexCalc.shapeX[0]);
                    hexGridLines.put(cy + 40.0f * HexCalc.shapeY[0]);
                    hexGridLines.put(0.0f);
                }

            }
        }

        hexGridLines.position(0);


    }

    //      //
    // Init //
    //      //

    // Define the board style
    public synchronized void setBoardStyle(int geo, int mat) {
        boardGeometry = geo;
        boardStyle = mat;
    }

    //                       //
    // Interaction with game //
    //                       //

    // Get the center coordinates of a tile
    public float getCoordX(int ix, int iy) {
        if (boardGeometry == 0) return ((float) ix + 0.5f) * 40.0f;
        else return 40.0f * HexCalc.getTileCenterX(ix, iy);
    }

    public float getCoordY(int ix, int iy) {
        if (boardGeometry == 0) return ((float) iy + 0.5f) * 40.0f;
        else return getHexCoordY(ix, iy);
    }

    public float getHexCoordX(int ix, int iy) {
        return 40.0f * HexCalc.getTileCenterX(ix, iy);
    }

    public float getHexCoordY(int ix, int iy) {
        return 40.0f * HexCalc.getTileCenterY(ix, iy);
    }

    // Move the view
    public synchronized void moveView(float dx, float dy) {

        // Nan and inf guard
        if (Float.isNaN(dx)) dx = 0.0f;
        if (Float.isInfinite(dx)) dx = 0.0f;
        if (Float.isNaN(dy)) dy = 0.0f;
        if (Float.isInfinite(dy)) dy = 0.0f;

        // Notice: coordinate change
        dy = -dy;

        // Normal & hex board

        // Move according to zoom level
        viewx += dx * vieww / screenw;
        viewy += dy * viewh / screenh;

        if (viewx < 0.0f) viewx = 0.0f;
        if (viewy < 0.0f) viewy = 0.0f;
        if (viewx > GoSettings.goNormalSide * 40.0f) viewx = GoSettings.goNormalSide * 40.0f;
        if (viewy > GoSettings.goNormalSide * 40.0f) viewy = GoSettings.goNormalSide * 40.0f;


    }

    // Zoom the view
    public synchronized void applyZoom(float scale) {
        // Dont worry about limits here.. just do the action
        // zoomlevel is adjusted in onDrawFRame
        zoomlevel *= scale;
        // Just make sure we have always valid numbers
        if (Float.isNaN(zoomlevel)) zoomlevel = 1.0f;
        if (Float.isInfinite(zoomlevel)) zoomlevel = 1.0f;
    }

    // Set the best possible zoom level (how many pixels should one grid cell be?)
    public synchronized void setBestZoom(float pixel) {
        // grid size is 40 pixels, how much do we need to scale it?
        zoomlevel = 40.0f / pixel;
        // Just make sure we don't get nan or inf (if there's something wrong with pixel)
        if (Float.isNaN(zoomlevel)) zoomlevel = 1.0f;
        if (Float.isInfinite(zoomlevel)) zoomlevel = 1.0f;
    }

    // Reset the view
    public synchronized void resetView() {

    }

    // Update the board state
    public synchronized void updateBoard(int ns[], boolean transition) {
        for (int i = 0; i < ns.length; i++) boardState[i].update(ns[i], transition);
    }

    // Set focus on a specific tile
    public synchronized void setNotification(int ti) {

        int tx = ti % GoSettings.goNormalSide;
        int ty = (ti - tx) / GoSettings.goNormalSide;
        noteX = getCoordX(tx, ty);
        noteY = getCoordY(ty, ty);
        noteTimer = 1.0f; // move withinh one second
        noteFade = 1.0f; // no fade
    }

    // Set the tile to highlight
    public synchronized void setLastTile(int ti) {
        if (lastTile > 0 && lastTile <= GoSettings.goMaxSize) {
            boardState[lastTile].lastTile = false;
        }
        if (ti > 0 && ti <= GoSettings.goMaxSize) {
            boardState[ti].lastTile = true;
        }
        lastTile = ti;
    }

    // Define the winning row
    public synchronized void setWinRow(int[] ti) {
        // Forget about previous
        for (int i = 0; i < boardState.length; i++) {
            boardState[i].winRow = false;
        }
        // Add new definition (if any)
        if (ti != null) {
            for (int i = 0; i < ti.length; i++) {
                int index = ti[i];
                if (index > 0 || index < boardState.length) {
                    boardState[index].winRow = true;
                }
            }
        }
    }

    // return tile index of a screen coordinate
    public synchronized int getTileIndex(float px, float py) {

        // Notice: coordinate change
        py = screenh - py;

        // map to grid coords
        float x = viewx + vieww * (px / screenw - 0.5f);
        float y = viewy + viewh * (py / screenh - 0.5f);

        // Normal square grid
        if (boardGeometry == 0) {

            if (x < 0.0f) return -1;
            if (y < 0.0f) return -1;
            if (x > GoSettings.goNormalSide * 40.0f) return -1;
            if (y > GoSettings.goNormalSide * 40.0f) return -1;

            int ix = (int) Math.floor(x / 40.0f);
            int iy = (int) Math.floor(y / 40.0f);
            return ix + iy * GoSettings.goNormalSide;
        }
        // Hex grid
        else if (boardGeometry == 1) {

            GridIndex ind = HexCalc.getTileIndex(x / 40.0f, y / 40.0f);

            if (ind.ix < 0) return -1;
            if (ind.iy < 0) return -1;
            if (ind.ix >= GoSettings.goNormalSide) return -1;
            if (ind.iy >= GoSettings.goNormalSide) return -1;

            return ind.ix + ind.iy * GoSettings.goNormalSide;
        }

        return -1;
    }

    public void setNextGameInfo(boolean state) {
        noteNextGame = state;
        noteNextGameTimer = 0.0f;
    }

    // Define next game note

    @Override
    public void onDrawFrame(GL10 gl) {

        // Handle timing
        float timeInS = 0.0f;
        long timerCur = System.nanoTime();
        if (timerLast == 0) {
            timeInS = 0.01f;
        } else {
            timeInS = (float) (timerCur - timerLast) / 1.0e9f; // get time in ms
        }
        timerLast = timerCur;

        // Pass time to tiles
        for (int i = 0; i < boardState.length; i++) boardState[i].passTime(timeInS);

        // Note tile timer
        noteTileTimer += timeInS;
        if (noteTileTimer > 2.0f * (float) Math.PI) noteTileTimer -= 2.0f * (float) Math.PI;

        // Make NaN and Inf checks
        if (Float.isNaN(viewx)) viewx = 0.0f;
        if (Float.isInfinite(viewx)) viewx = 0.0f;
        if (Float.isNaN(viewy)) viewy = 0.0f;
        if (Float.isInfinite(viewy)) viewy = 0.0f;

        // Move view according to notification
        if (noteTimer > 0.0f) {
            float deltaX = noteX - viewx;
            float deltaY = noteY - viewy;
            float vel = 5.0f * (1.0f - noteTimer);
            viewx += timeInS * deltaX * vel * noteFade;
            viewy += timeInS * deltaY * vel * noteFade;
            noteTimer -= timeInS;
        }


        // Clear
        gl.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        // Limit zoomlevel
        // min zoomlevel so that a tile is 20x20 pixels
        if (zoomlevel > 2.0f) zoomlevel = 2.0f;
        // max zoomlevel so that at least two tiles are visible on the screen
        float mindis = screenw;
        if (screenh < screenw) mindis = screenh;
        if (zoomlevel < 80.0f / mindis) zoomlevel = 80.0f / mindis;

        // calculate the size of the screen
        float mult = zoomlevel;
        vieww = screenw * mult;
        viewh = screenh * mult;


        // Set view
        ViewSet.setFlyerView(gl, viewh, 0.0f, screenw / screenh, viewh * 0.5f, viewx, viewy, 0.0f);

        // draw the board lines
        float left = viewx - vieww * 0.5f;
        float right = viewx + vieww * 0.5f;
        float bottom = viewy - viewh * 0.5f;
        float top = viewy + viewh * 0.5f;
        int ind0, ind1;

        if (left < 0.0f) left = 0.0f;
        if (right > GoSettings.goNormalSide * 40.0f) right = GoSettings.goNormalSide * 40.0f;
        if (bottom < 0.0f) bottom = 0.0f;
        if (top > GoSettings.goNormalSide * 40.0f) top = GoSettings.goNormalSide * 40.0f;

        // Draw the board
        // TODO: Add limits

        // Faded or non-faded tiles?
        for (int blend = 0; blend <= 1; blend++) {

            if (blend == 0) {
                sprite.setGLStateTAM1(gl, false);
                sprite.setColorUniform(1.0f, 1.0f, 1.0f, 1.0f);
            } else sprite.setGLStateTAM1(gl, true);

            // X or O?
            for (int mark = 1; mark <= 2; mark++) {

                if (mark == 1) texCross.bind(gl);
                if (mark == 2) texCirc.bind(gl);

                for (int ix = 0; ix < GoSettings.goNormalSide; ix++) {
                    for (int iy = 0; iy < GoSettings.goNormalSide; iy++) {

                        int index = ix + iy * GoSettings.goNormalSide;

                        float cx = getCoordX(ix, iy);
                        float cy = getCoordY(ix, iy);

                        float size = 35.0f;
                        // If last mark, modify the size
                        if (boardState[index].lastTileAlpha > 0.0f) {
                            size += 4.0f * boardState[index].lastTileAlpha * (float) Math.sin(boardState[index].lastTileTimer * Math.PI * 2.0f);
                        }

                        if (boardState[index].visualState == mark) {
                            if (blend == 0) {
                                if (boardState[index].alpha >= 1.0f) {
                                    sprite.draw(gl, cx, cy, size, size);
                                }
                            } else {
                                if (boardState[index].alpha < 1.0f) {
                                    sprite.setColorUniform(1.0f, 1.0f, 1.0f, boardState[index].alpha);
                                    sprite.draw(gl, cx, cy, size, size);
                                }

                            }

                        }

                    }
                }
            }
        }

        // DRAW LINES //

        // pass verteces, tex coords & color arrays
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        gl.glDisable(GL10.GL_TEXTURE_2D);
        gl.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);

        // NORMAL
        if (boardGeometry == 0) {

            // horizontal lines
            ind0 = (int) Math.ceil(left / 40.0f);
            ind1 = (int) Math.floor(right / 40.0f);
            if (ind0 < 0) ind0 = 0;
            if (ind1 > GoSettings.goNormalSide + 1) ind1 = GoSettings.goNormalSide + 1;

            for (int i = ind0; i <= ind1; i++) {
                DrawUtil.Line(gl, i * 40.0f, bottom, 0.0f, i * 40.0f, top, 0.0f);
            }

            // draw horizontal lines
            ind0 = (int) Math.ceil(bottom / 40.0f);
            ind1 = (int) Math.floor(top / 40.0f);
            if (ind0 < 0) ind0 = 0;
            if (ind1 > GoSettings.goNormalSide + 1) ind1 = GoSettings.goNormalSide + 1;

            for (int i = ind0; i <= ind1; i++) {
                DrawUtil.Line(gl, left, i * 40.0f, 0.0f, right, i * 40.0f, 0.0f);
            }
        }

        // HEXAGONAL LATTICE
        else if (boardGeometry == 1) {

            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, hexGridLines);
            gl.glDrawArrays(GL10.GL_LINES, 0, hexGridNumVertex);


        }

        // Draw the winning row
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glDisable(GL10.GL_TEXTURE_2D);
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        for (int ix = 0; ix < GoSettings.goNormalSide; ix++) {
            for (int iy = 0; iy < GoSettings.goNormalSide; iy++) {

                int index = ix + iy * GoSettings.goNormalSide;
                float cx = getCoordX(ix, iy);
                float cy = getCoordY(ix, iy);

                if (boardState[index].winRow) {
                    glowBall.draw(gl, cx, cy, 50.0f);
                }
            }
        }

        // GUI //

        ViewSet.setGUIView(gl, screenw, screenh);

        // Draw different notifications

        // Next game
        if (noteNextGame) {

            noteNextGameTimer += timeInS;

            // Draw arrows
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
            gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
            gl.glDisable(GL10.GL_TEXTURE_2D);
            gl.glEnable(GL10.GL_BLEND);
            gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

            gl.glColor4f(1.0f, 1.0f, 0.0f, 0.5f);

            float triHeight = screenh / 20.0f;

            //DrawUtil.TriSimple(gl, , y0, z0, x1, y1, z1, x2, y2, z2);
        }


    }


    //              //
    // Render frame //
    //              //

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        // Record screen size
        screenw = (float) width;
        screenh = (float) height;

        // Set desired GL state
        gl.glDisable(GL10.GL_LIGHTING);
        gl.glDisable(GL10.GL_CULL_FACE);
        gl.glDisable(GL10.GL_DEPTH_TEST);
        gl.glClearColor(.5f, .5f, .8f, 1.f);
        gl.glViewport(0, 0, width, height);

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig conf) {

        texCross.defineResource(R.raw.cross);
        texCirc.defineResource(R.raw.circ);

        texCross.loadThisResource(appres, gl);
        texCirc.loadThisResource(appres, gl);

        glowBall.init(16, 1.0f, 1.0f, 0.0f, 0.25f, 1.0f, 1.0f, 0.0f, 0.0f);

    }

    public void loadState(SharedPreferences prefs) {
        viewx = prefs.getFloat("gorender_viewx", 0.0f);
        viewy = prefs.getFloat("gorender_viewy", 0.0f);
        zoomlevel = prefs.getFloat("gorender_zoomlevel", 1.0f);
    }

    //                           //
    // Saving & loading settings //
    //                           //

    public void saveState(SharedPreferences.Editor edit) {
        // Only save the locatin of the screen, everything else is calculated
        edit.putFloat("gorender_viewx", viewx);
        edit.putFloat("gorender_viewy", viewy);
        edit.putFloat("gorender_zoomlevel", zoomlevel);
    }

    // State of board
    class GoTileGraph {

        int actualState; // actual state (0, 1 or 2) (is there a X or O there?)
        int visualState; // visual state, should we display something (faded)
        float alpha;     // how much should we fade?

        boolean winRow; // winning row?

        // Last tile marker
        boolean lastTile = false;    // Is this the last tile marked?
        float lastTileAlpha = 0.0f;  // how strong is the last tile effecxt?
        float lastTileTimer = 0.0f;  // Timer for last mark animation

        void update(int ns, boolean fade) {
            if (!fade) {
                actualState = ns;
                visualState = ns;
                alpha = 1.0f;
            } else {
                if (ns == 0) {
                    visualState = actualState;
                    actualState = ns;
                    alpha = 1.0f;
                } else {
                    if (ns != actualState) alpha = 0.0f;
                    visualState = ns;
                    actualState = ns;
                }

                actualState = ns;
                visualState = ns;
            }
        }

        void passTime(float timeInS) {
            if (actualState == 0 && visualState != 0) {
                alpha -= timeInS;
                if (alpha < 0.0f) {
                    alpha = 1.0f;
                    visualState = 0;
                }
            }
            if (actualState != 0 && alpha < 1.0f) {
                alpha += timeInS;
            }

            // Last tile animation

            lastTileTimer += timeInS;
            if (lastTileTimer > 1.0f) lastTileTimer -= 1.0f;

            if (lastTile) {
                if (lastTileAlpha < 1.0f) {
                    lastTileAlpha += timeInS;
                    if (lastTileAlpha > 1.0f) lastTileAlpha = 1.0f;
                }
            } else {
                if (lastTileAlpha > 0.0f) {
                    lastTileAlpha -= timeInS;
                    if (lastTileAlpha < 0.0f) lastTileAlpha = 0.0f;
                }

            }
        }

    }
}
