/*
  LowPassLPCFilter.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
