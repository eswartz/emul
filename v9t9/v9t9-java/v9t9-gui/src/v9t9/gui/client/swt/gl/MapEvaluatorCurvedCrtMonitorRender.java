/*
  MapEvaluatorCurvedCrtMonitorRender.java

  (c) 2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.gui.client.swt.gl;

import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_MAP2_TEXTURE_COORD_2;
import static org.lwjgl.opengl.GL11.GL_MAP2_VERTEX_3;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEvalMesh2;
import static org.lwjgl.opengl.GL11.glMap2f;
import static org.lwjgl.opengl.GL11.glMapGrid2f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Argh!!!  Can't support multitexturing... blah.
 * 
 * @author Ed
 * @deprecated
 *
 */
public class MapEvaluatorCurvedCrtMonitorRender extends BaseMonitorEffect {
	/**
	 * 
	 */
	private static final int SPLITS = 8;
	public static final MapEvaluatorCurvedCrtMonitorRender INSTANCE = new MapEvaluatorCurvedCrtMonitorRender();
	private FloatBuffer points;
	private FloatBuffer texcoords;
	
	public MapEvaluatorCurvedCrtMonitorRender() {
	}

	private float[][][] pts = {
			{
				{ 0, 0, 0 },
				{ 1, 0, 0 },
			},
			{
				{ 0, 1, 0 },
				{ 1, 1, 0 },
			}
	};
	
	@Override
	public void init() {
		
		points = ByteBuffer.allocateDirect(4 * pts.length * pts[0].length * pts[0][0].length
				).order(ByteOrder.nativeOrder()).asFloatBuffer(); 
		for (float[][] p1 : pts)
			for (float[] p2 : p1)
				points.put(p2);
		points.rewind();
		
		float[] txpts = {
				0, 0,
				0, 1,
				1, 0,
				1, 1,
		};
		texcoords = ByteBuffer.allocateDirect(4 * txpts.length
				).order(ByteOrder.nativeOrder()).asFloatBuffer(); 
		texcoords.put(txpts);
		texcoords.rewind();
	}
	@Override
	public void render() {

		glMap2f(GL_MAP2_VERTEX_3, 
				0, 1, // u1, u2  
				pts[0].length * pts[0][0].length, pts.length, // stride, order
		        0, 1, // v1, v2
		        3, pts[0].length, // stride, order 
		        points);
		
		glMap2f(GL_MAP2_TEXTURE_COORD_2, 
				0, 1, // u1, u2  
				texcoords.limit() / 4, 2, // stride, order
		        0, 1, // v1, v2
		        4, 2, // stride, order 
		        texcoords);

		glMapGrid2f(SPLITS, 0f, 1f, SPLITS, 0f, 1f);
	    
	    glEnable(GL_MAP2_VERTEX_3);
	    glEnable(GL_MAP2_TEXTURE_COORD_2);
	    
		glEvalMesh2(GL_FILL, 0, SPLITS, 0, SPLITS);
	}

}
