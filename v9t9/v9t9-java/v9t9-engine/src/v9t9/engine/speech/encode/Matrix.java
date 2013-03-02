/*
  Matrix.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.speech.encode;

import java.util.Arrays;

/**
 * @author ejs
 *
 */
public class Matrix {
	private int rows;
	private int cols;
	private float[] data;

	public Matrix(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
		this.data = new float[rows * cols];
	}
	
	public float at(int row, int col) {
		return data[row * cols + col];
	}

	/**
	 * @param res
	 * @return 
	 */
	public int invert(Matrix res) {
		int rank = 0;
		int r,c,i;
	    float temp;

	    // initialize res to identity
	    for( r = 0; r < rows; r++ )
	        for( c = 0; c < cols; c++ )
	        {
	            if( r == c )
	                res.set(r, c, 1.0f);
	            else
	                res.set(r, c, 0.0f);
	        }

	    for( i = 0; i < rows; i++)
	    {
	        if( at(i, i) == 0.0f )
	        {
	            for( r = i; r < rows; r++ )
	                for( c = 0; c < cols; c++ )
	                {
	                    set(i, c, at(i, c) + at(r, c ));
	                    res.set(i, c, res.at(i, c) + res.at(r, c));
	                }
	        }

	        for( r = i; r < rows; r++ )
	        {
	            temp = at(r, i);
	            if( temp != 0.0f )
	                for( c = 0; c < cols; c++ )
	                {
	                    set(r, c, at(r, c) / temp);
	                    res.set(r, c, res.at(r, c) / temp);
	                }
	        }

	        if( i != rows - 1 )
	        {
	            for( r = i + 1; r < rows; r++ )
	            {
	                temp = at(r, i);
	                if( temp != 0.0f )
	                    for( c = 0; c < cols; c++ )
	                    {
	                        set(r, c, at(r, c) - at(i, c));
	                        res.set(r, c, res.at(r, c) - res.at(i, c));
	                    }
	            }
	        }
	    }

	    for( i = 1; i < rows; i++ )
	        for( r = 0; r < i; r++ )
	        {
	            temp = at(r, i);
	            for( c = 0; c < cols; c++ )
	            {
	                set(r, c, at(r, c) - (temp * at(i, c)));
	                res.set(r, c, res.at(r, c) - (temp * res.at(i, c)));
	            }
	        }

	    for( r = 0; r < rows; r++ )
	        for( c = 0; c < cols; c++ )
	            set(r, c, res.at(r, c));

	    return rank;
	}

	/**
	 * @param r
	 * @param c
	 * @param f
	 */
	public void set(int r, int c, float f) {
		data[r * cols + c] = f;
	}

	/**
	 * 
	 */
	public void clear() {
		Arrays.fill(data, 0f);
	}
}
