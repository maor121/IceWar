/* Shahar Kosti			021639968
   Maor Shliefer		305206898 */

package icewar.com.icewar.core;

/** Implements a vector in 3D space
 */
public class Vector {
    // Dimensions
    public enum Axis { X, Y, Z };
    public static final int N = 3;
    public static final Vector Zero = new Vector();

    // Internal representation
    private float[] data;

    public Vector() {
        this.data = new float[N];
    }

    public Vector(float x, float y, float z) {
        this();
        setX(x);
        setY(y);
        setZ(z);
    }

    public float getValue(Axis axis) {
        return this.data[axis.ordinal()];
    }

    public void setValue(Axis axis, float value) {
        this.data[axis.ordinal()] = value;
    }

    public float[] getData() {
        return this.data;
    }

    public float x() {
        return getValue(Axis.X);
    }

    public float y() {
        return getValue(Axis.Y);
    }

    public float z() {
        return getValue(Axis.Z);
    }

    public void setX(float value) {
        setValue(Axis.X, value);
    }

    public void setY(float value) {
        setValue(Axis.Y, value);
    }

    public void setZ(float value) {
        setValue(Axis.Z, value);
    }

    public String toString() {
        return String.format("(%f, %f, %f)", x(), y(), z());
    }

    // Returns the norm of this vector
    public float size() {
        return (float)Math.sqrt(sizeSqr());
    }

    public float sizeSqr() {
        return x()*x()+y()*y()+z()*z();
    }

    // Returns this vector + other vector
    public Vector add(Vector other) {
        return new Vector(x()+other.x(), y()+other.y(), z()+other.z());
    }

    // Returns this vector - other vector
    public Vector subtract(Vector other) {
        return new Vector(x()-other.x(), y()-other.y(), z()-other.z());
    }

    // Returns this vector multiplied by a scalar
    public Vector mul(float scalar) {
        return new Vector(x()*scalar, y()*scalar, z()*scalar);
    }

    public Vector mul(Vector scalars) {
        return new Vector(x()*scalars.x(), y()*scalars.y(), z()*scalars.z());
    }

    public Vector reverse() {
    	return mul(-1);
    }

    public Vector div(float scalar) {
        if (scalar == 0) {
            throw new IllegalArgumentException("Argument 'scalar' is 0");
        }
        return new Vector(x()/scalar, y()/scalar, z()/scalar);
    }

    // Returns the dot product of this and another vector
    public float dot(Vector other) {
        return x()*other.x() + y()*other.y() + z()*other.z();
    }

    // Returns the cross product of this and another vector
    public Vector cross(Vector other) {
        return new Vector (
               (y()*other.z()) - (z()*other.y()),
               (z()*other.x()) - (x()*other.z()),
               (x()*other.y()) - (y()*other.x()));
    }

    public float distance(Vector other) {
    	return (float) Math.sqrt( sqr( x()-other.x() ) + sqr( y() - other.y() ) + sqr( z() - other.z() ) );
    }

    // Normalize this vector
    public Vector normalize() {
        final float size = this.size();
        if (size == 0) {
            return new Vector();
        }

        return this.div(size);
    }

    public float angleBetween(Vector vec) {
		//find angle(degrees) between this and vec
		float angle = (float) (Math.atan2(vec.z(),vec.x()) - Math.atan2(this.z(),this.x()));

		return angle;
    }

    private static float sqr(float value) {
        return value*value;
    }
}
