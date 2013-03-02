/*
  LowPassLPCFilter.java

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
public class LowPassLPCFilter implements ILPCFilter {

	private ILPCFilter chain;
	/**
	 * @param params
	 */
	public LowPassLPCFilter(LPCEncoderParams params, ILPCFilter chain) {
		this.chain = chain;
	}

	float last = 0f;
	/* (non-Javadoc)
	 * @see v9t9.engine.speech.encode.ILPCFilter#filter(float[], int, int, float[], float[])
	 */
	@Override
	public void filter(float[] in, int offs, int len, float[] out, float[] y) {
				
		float q = 0.5f;
		for (int idx = 0; idx < len; idx ++) {
			float v = in[idx + offs];

//				v = (1f * v - 0.5f * last);
//				last = v;
//				
//				content[idx] = v;
			
			out[idx] = (v * (1 - q) - last * q);
			last = out[idx];
		}

		if (chain != null)
			chain.filter(in, offs, len, out, y);
	}

}
