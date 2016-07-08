package icewar.com.icewar.core;

import android.graphics.PointF;

/**
 * Created by Maor on 07/07/2016.
 */
public class Util {
    public static float distance(PointF p1, PointF p2) {
        return (float) Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y- p2.y, 2));
    }
}
