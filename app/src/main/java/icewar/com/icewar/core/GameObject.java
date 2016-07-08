/* Shahar Kosti			021639968
   Maor Shliefer		305206898 */

package icewar.com.icewar.core;

import android.graphics.PointF;

import com.threed.jpct.Object3D;

/**
 * Interface for an object that has a model, position, rotation and a center
 */
public interface GameObject {
    Object3D getModel();

    Vector getPosition();
 
    Vector getOrientation();
    
    Vector getScaling();

    /** Get center 2D (x,z) */
    PointF getCenter();

    float getRadius();
    
    float overlappingDistance(GameObject obj);
}
