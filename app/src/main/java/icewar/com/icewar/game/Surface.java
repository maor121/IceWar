/* Shahar Kosti			021639968
   Maor Shliefer		305206898 */

 package icewar.com.icewar.game;

import android.graphics.PointF;

import com.threed.jpct.Object3D;

import java.util.Random;

import icewar.com.icewar.core.GameObject;
import icewar.com.icewar.core.Util;
import icewar.com.icewar.core.Vector;

/**
 * Implements the auto-rotating surface game object.
 * The rotation change is randomized.
 */
public class Surface implements GameObject {
    private static final float MAX_ROTATION = 15;
    private static final float CHANGE_FACTOR = 30;
    private static final int CHANGE_AFTER_SECS = 5;

	private final Object3D model;
    private final float radius;
    private final Random random = new Random();
    
    final Vector position = new Vector();
    Vector orientation = new Vector();
    final Vector scaling = new Vector(10,5,10);

    private Vector changeDirection = new Vector(0, 1, 1);
    private Vector changeSize = new Vector(0,0, 0);

    public Surface(Object3D model, float radius) {
        this.model = model;
        this.radius = radius;
    }

    public void setOrientation(Vector orientation) {
        this.orientation = orientation;
    }

    public Object3D getModel() {
        return this.model;
    }

    public Vector getPosition() {
        return this.position;
    }

    public Vector getOrientation() {
        return this.orientation;
    }
    
    public Vector getScaling() {
    	return scaling;
    }

	public PointF getCenter() {
		return new PointF( (float)position.x(), (float)position.z() );
	}

	public float getRadius() {
		return radius;
	}
	
	public float overlappingDistance(GameObject obj) {
		PointF center1 = getCenter();
		PointF center2 = obj.getCenter();
    	
    	float dist = Util.distance(center1, center2);
    	
    	float radius1 = getRadius();
    	float radius2 = obj.getRadius();
    	
    	if (dist > radius1 + radius2 )
    		return 0;
    	
    	return radius1 + radius2 - dist;
    }

    /**
     * Sets the current surface rotation by considering elapsed time
     */
    public void update(double timePassedSecs) {
        final float changeMax = (float)( CHANGE_FACTOR * timePassedSecs );
        this.changeSize.setY(random.nextFloat() * changeMax);
        this.changeSize.setZ(random.nextFloat() * changeMax);

        updateChangeAxis(Vector.Axis.Y, timePassedSecs);
        updateChangeAxis(Vector.Axis.Z, timePassedSecs);

        final Vector change = this.changeDirection.mul(this.changeSize);
        this.orientation = this.orientation.add(change);
    }

    /**
     * Update rotation on one axis
     */
    private void updateChangeAxis(Vector.Axis axis, double timePassedSecs) {
        boolean shouldInvert = false;

        if (Math.abs(this.orientation.getValue(axis)) > MAX_ROTATION) {
            shouldInvert = true;
        }
        else if (this.random.nextDouble() < timePassedSecs/CHANGE_AFTER_SECS) {
            shouldInvert = true;
        }

        if (shouldInvert) {
            changeDirection.setValue(axis, -changeDirection.getValue(axis));
        }
    }
}
