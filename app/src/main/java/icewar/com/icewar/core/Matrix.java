/* Shahar Kosti			021639968
   Maor Shliefer		305206898 */

 package icewar.com.icewar.core;

/**Matrix class. Responsible for creating and multiplying matrices.*/
public class Matrix {
    // Dimensions
    public static final int N = 4;

    private float[][] data;

    private Matrix(int n) {
        data = new float[n][n];
    }

    /**Get value at i,j*/
    public float getValue(int i, int j) {
        return data[i][j];
    }
    
    /**Set value at i,j*/
    public void setValue(int i, int j, float value) {
        data[i][j] = value;
	}
    
    /**Returns (this_matrix) * (other_matrix)*/
    public Matrix mul(Matrix other) {
        Matrix result = new Matrix(N);

        for (int i=0; i<N; i++) {
            for (int j=0; j<N; j++) {
                float value = 0;
                for (int k=0; k<N; k++) {
                    value += this.getValue(i,k)*other.getValue(k, j);
                }
                result.setValue(i, j, value);
            }
        }

        return result;
	}

    /**Multiply Matrix by vector. Return vector*/
    public Vector mul(Vector vec) {
        float x,y,z;
        
        x = this.getValue(0, 0)*vec.x() + this.getValue(0, 1)*vec.y() + this.getValue(0, 2)*vec.z() + this.getValue(0,3);
        y = this.getValue(1, 0)*vec.x() + this.getValue(1, 1)*vec.y() + this.getValue(1, 2)*vec.z() + this.getValue(1,3);
        z = this.getValue(2, 0)*vec.x() + this.getValue(2, 1)*vec.y() + this.getValue(2, 2)*vec.z() + this.getValue(2,3);
        
        return new Vector(x,y,z);
    }

    /**Create identity matrix*/
    public static Matrix createIdentity() {
		Matrix matrix = new Matrix(N);
        for (int i=0; i<N; i++) {
            matrix.setValue(i, i, 1);
        }
        return matrix;
	}

    /**Create translation matrix*/
    public static Matrix createAxisTranslation(Vector yAxis, Vector zAxis) {
		Matrix matrix = createIdentity();
        
        Vector xAxis = yAxis.cross(zAxis);
		
		matrix.setValue(0, 0, xAxis.x());
        matrix.setValue(0, 1, xAxis.y());
        matrix.setValue(0, 2, xAxis.z());
        matrix.setValue(1, 0, yAxis.x());
        matrix.setValue(1, 1, yAxis.y());
        matrix.setValue(1, 2, yAxis.z());
        matrix.setValue(2, 0, zAxis.x());
        matrix.setValue(2, 1, zAxis.y());
        matrix.setValue(2, 2, zAxis.z());
		
        return matrix;
	}
    
    @Override 
    public String toString() {
    	String s = "";
    	for (int i = 0; i < N; i++) {
    		for (int j = 0; j < N; j++)
    			s += data[i][j] + " ";
    		s += "\n";
    	}
    	
    	return s;
    }
}
