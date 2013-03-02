/*
  SimpleLPCFilter.java

  (c) 2012 Edward Swartz

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
package v9t9.engine.speech.encode;

/**
 * @author ejs
 *
 */
public class SimpleLPCFilter implements ILPCFilter {
	private float[] fa;
	private float u1;
	private float yp1;
	private float yp2;
	private int framelen;
	private int FS;

	private static float FC = 200.0f; /* Pitch analyzer filter cutoff */
	/**
	 * 
	 */
	public SimpleLPCFilter(LPCEncoderParams params) {
		fa = new float[6];
		framelen = params.getFrameSize();
		FS = params.getHertz();
		
		float r, v, w, wcT;
		wcT = (float) (2 * Math.PI * FC / FS);
		r = 0.36891079f * wcT;
		v = 0.18445539f * wcT;
		w = 0.92307712f * wcT;
		fa[1] = (float) -Math.exp(-r);
		fa[2] = 1 + fa[1];
		fa[3] = (float) (-2 * Math.exp(-v) * Math.cos(w));
		fa[4] = (float) Math.exp(-2.0f * v);
		fa[5] = 1 + fa[3] + fa[4];
		
	}


	/* (non-Javadoc)
	 * @see v9t9.engine.speech.encode.ILPCEngine#filter(float[], float[], int, int)
	 */
	@Override
	public void filter(float[] in, int offs, int len, float[] out, float[] y) {
		int i,j;
		for (i = 0, j = len - framelen; i < framelen; i++, j++) {
			out[j] = in[i + offs];
			float u = fa[2] * out[j] - fa[1] * u1;
			y[j] = fa[5] * u1 - fa[3] * yp1 - fa[4] * yp2;
			u1 = u;
			yp2 = yp1;
			yp1 = y[j];
		}
	}
	

}
