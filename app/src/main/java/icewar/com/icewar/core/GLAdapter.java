/* Shahar Kosti			021639968
   Maor Shliefer		305206898 */

 package icewar.com.icewar.core;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.sun.opengl.util.j2d.TextRenderer;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;
import com.threed.jpct.Loader;
import com.threed.jpct.Object3D;

import net.java.joglutils.model.ModelFactory;
import net.java.joglutils.model.ModelLoadException;
import net.java.joglutils.model.examples.DisplayListRenderer;
import net.java.joglutils.model.geometry.Model;
import net.java.joglutils.model.iModel3DRenderer;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import com.threed.jpct.Texture;

import java.io.File;
import java.io.IOException;

/** Implements Renderer using JOGL
 * @see Renderer
 */
public class GLAdapter implements GLSurfaceView.Renderer, Renderer {
    private final String TAG = getClass().getSimpleName();

    private final static double FOV = 45;
    private final static double NEAR_CLIP = 1;
    private final static double FAR_CLIP = 1000;

    private final GLU glu = new GLU();
    private final TextRenderer textRenderer = new TextRenderer(new Font("Times New Roman", Font.BOLD, 40));
    private Camera camera;
    private DrawCallback callback;
    private GL10 gl;
    private iModel3DRenderer modelRenderer;

    private int width;
    private int height;
    
    private String texturePath;
    private Texture backgroundTexture;

    public GLAdapter() {
    }

    @Override
    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    @Override
    public void setDrawCallback(DrawCallback callback) {
        this.callback = callback;
    }

    /**
     *
     * @param path
     * @return
     */
    public Object3D loadModel3DS(Context ctx, String path) {
        try {
            Object3D model =
                    Object3D.mergeAll(Loader.load3DS(ctx.getResources().getAssets().open(path, 5), 1.0f);

//            model.setUseTexture(true);
//
//            model.setUseLighting(true);
//
//            // Render the bounding box of the entire model
//            model.setRenderModelBounds(false);
//
//            // Render the bounding boxes for all of the objects of the model
//            model.setRenderObjectBounds(false);
//
//            // Make the model unit size
//            model.setUnitizeSize(true);

            return model;
        }
        catch (IOException e) {
            Log.d(TAG, "Error", e);
            return null;
        }
    }

    /**
     * Draw a game object by setting the position, orientation and scaling,
     * and then using jogl-utils render()
     * @param gameObject object to draw
     */
    @Override
    public void renderGameObject(GameObject gameObject) {
        gl.glPushMatrix();

        final float[] position = gameObject.getPosition().getData();
        final float[] orientation = gameObject.getOrientation().getData();
        final float[] scaling = gameObject.getScaling().getData();

        gl.glRotatef(orientation[2], 0, 0, 1);
        gl.glRotatef(orientation[1], 1, 0, 0);

        gl.glScalef(scaling[0], scaling[1], scaling[2]);

        gl.glTranslatef(position[0], position[1], position[2]);

        gl.glRotatef(orientation[0], 0, 1, 0);

        modelRenderer.render(gl, gameObject.getModel());

        gl.glPopMatrix();
    }

    /**
     * Draw the background texture on a quad
     */
    @Override
    public void renderBackground() {

        // Load the background texture if not loaded
        if (backgroundTexture == null && texturePath != null) {
        	try {
				backgroundTexture = TextureIO.newTexture(new File(texturePath), true);

		    	gl.glEnable(GL10.GL_TEXTURE_2D);

                gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
			    gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        	}
        	catch (IOException e) {
        		e.printStackTrace();
        	}
        }
	    //End Texture

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPushMatrix();

        // Textures are upside down
        gl.glLoadIdentity();
        gl.glScalef(1, -1, 1);

		gl.glOrthox(0, 1, 0, 1, 0, 1);
	
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		
		// No depth buffer writes for background.
		gl.glDepthMask(false);

		backgroundTexture.bind();
		gl.glBegin(GL.GL_QUADS); {
		  gl.glTexCoord2f( 0f, 1f );
		  gl.glVertex2f( 0, 1f );
		  gl.glTexCoord2f( 1f, 1f );
		  gl.glVertex2f( 1f, 1f );
		  gl.glTexCoord2f( 1f, 0f );
		  gl.glVertex2f( 1f, 0 );
		  gl.glTexCoord2f( 0f, 0f );
		  gl.glVertex2f(0, 0);
		}
		gl.glEnd();
	
		gl.glDepthMask( true );
	
		gl.glPopMatrix();
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL10.GL_MODELVIEW);
    }

    /**
     * Draw text using TextRenderer
     */
    @Override
    public void renderText(String text, Point pos) {
        textRenderer.draw(text, pos.x, this.height-44-pos.y); // OpenGL (0,0) is at down left
    }

    /**
     * GL setup, lighting
     */
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        this.modelRenderer = DisplayListRenderer.getInstance();
        //this.modelRenderer.debug(true);

        gl.glShadeModel(GL10.GL_SMOOTH);

        // Setup the depth buffer and enable the depth testing
        gl.glClearDepthf(1.0f);          // clear z-buffer to the farthest
        gl.glEnable(GL10.GL_DEPTH_TEST);  // enables depth testing
        gl.glDepthFunc(GL10.GL_LEQUAL);   // the type of depth test to do

        // Light
        final float ambient0[] = {1, 1, 1, 1.0f};
        final float diffuse0[] = {1f, 1f, 1f, 1.0f};
        final float ambient1[] = {0.8f, 0.8f, 0.8f, 1.0f};
        final float diffuse1[] = {1.0f, 1.0f, 1.0f, 1.0f};
        final float position1[] = {0.0f, 20, 0.0f, 1.0f};

        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, ambient0, 0);
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, diffuse0, 0);
        gl.glEnable(GL10.GL_LIGHT0);

        gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_AMBIENT, ambient1, 0);
        gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_DIFFUSE, diffuse1, 0);
        gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_POSITION, position1, 0);
        gl.glEnable(GL10.GL_LIGHT1);

        gl.glEnable(GL10.GL_LIGHTING);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glPushMatrix();
    }

    /**
     * Set path to background texture (image). the image will be loaded in the next update.
     * @param path string path
     */
    public void setBackgroundTexturePath(String path) {
    	texturePath = path;
        backgroundTexture = null;
    }

    /**
     * Render method, called each frame
     * Uses the internal DrawCallback to determine what to draw
     * @see DrawCallback
     */
    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        // Look at
        final Vector position = camera.getPosition();
        final Vector center = camera.getCenter();
        final Vector up = camera.getUp();
        glu.gluLookAt(
            position.x(), position.y(), position.z(),
            center.x(), center.y(), center.z(),
            up.x(), up.y(), up.z()
        );

        this.gl = gl;
        this.callback.drawObjects();
        this.gl = null;

        textRenderer.beginRendering(width, height);
        this.callback.drawText();
        textRenderer.endRendering();

        gl.glFlush();
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {

        if (height <= 0)
            height = 1;
        final float ratio = (float)width/(float)height;
        this.width = width;
        this.height = height;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(FOV, ratio, NEAR_CLIP, FAR_CLIP);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
    }
}