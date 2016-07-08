/* Shahar Kosti			021639968
   Maor Shliefer		305206898 */

 package icewar.com.icewar.game;

import android.graphics.PointF;

import com.threed.jpct.Object3D;

import icewar.com.icewar.core.GameObject;
import icewar.com.icewar.core.Util;
import icewar.com.icewar.core.Vector;

/**
 * Implements a game object that is placed on the surface.
 * Maintains data that is used for physics computations (position, velocity, force).
 * @see GameObject
 */
public class Player implements GameObject {
    private final Object3D model;
    private final float mass;
    private final float radius;
    private Vector force = new Vector();
    private Vector position = new Vector();
    private Vector velocity = new Vector();
    private Vector orientation = new Vector();
    private final Vector scaling = new Vector(1,1,1);
    private boolean isOnSurface = true;

    public Player(Object3D model, float mass, float radius) {
        this.model = model;
        this.mass = mass;
        this.radius = radius;
    }

    public void setForce(Vector force) {
        this.force = force;
    }

    public void addForce(Vector force) {
    	this.force = force.add(force);
    }

    public void setPosition(Vector position) {
        this.position = position;
    }

    public void setVelocity(Vector velocity) {
        this.velocity = velocity;
    }

    public void addVelocity(Vector velocity) {
    	this.velocity = velocity.add(velocity);
    }

    public void setOrientation(float xrot, float yrot, float zrot) {
    	this.orientation.setX(xrot);
        this.orientation.setY(yrot);
        this.orientation.setZ(zrot);
    }

    public Object3D getModel() {
        return this.model;
    }

    public Vector getPosition() {
        return position;
    }

    public Vector getOrientation() {
        return orientation;
    }

    public Vector getVelocity() {
        return velocity;
    }

    public Vector getScaling() {
    	return scaling;
    }

    public Vector getForce() {
        return force;
    }

    public float getMass() {
        return mass;
    }

    public float getSpeed() {
        return velocity.size();
    }

    public boolean isOnSurface() {
        return isOnSurface;
    }

    public void setIsOnSurface(boolean isOnSurface) {
        this.isOnSurface = isOnSurface;
    }

    /** Move the player in case of a collision */
    public void retract(Vector direction, float dist) {

    	float directionX = direction.x(), directionZ = direction.z();

    	if ( (directionX == 0) && (directionZ == 0) ) //atan2(0,0) is undefined
    		return;

    	float alpha = (float) Math.atan2( directionZ , directionX );

    	Vector delta = new Vector((float)(- dist * Math.cos( alpha )), 0,(float)( -dist * Math.sin( alpha )));

    	position = position.add( delta );
    }

    public PointF getCenter()
    {
    	return new PointF(position.x(), position.z() );
    }

	public float getRadius() {
		return this.radius;
	}

	public float overlappingDistance(GameObject obj) {
        PointF center1 = getCenter();
		PointF center2 = obj.getCenter();

    	float dist = Util.distance(center1,center2);

    	float radius1 = getRadius();
    	float radius2 = obj.getRadius();
    	
    	if (dist >= radius1 + radius2 )
    		return 0;
    	
    	return radius1 + radius2 - dist;
    }
}
