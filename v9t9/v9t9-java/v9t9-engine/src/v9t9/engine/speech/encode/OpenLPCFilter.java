/*
  OpenLPCFilter.java

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
public class OpenLPCFilter implements ILPCFilter {
	private float xv1[] = new float[3];
	private float yv1[] = new float[3];
	private float xv2[] = new float[3];
	private float yv2[] = new float[3];
	private float xv3[] = new float[3];
	private float yv3[] = new float[3];
	private float xv4[] = new float[3];
	private float yv4[] = new float[3];
	private float FS;
	private int framelen;
	
	/**
	 * 
	 */
	public OpenLPCFilter(LPCEncoderParams params) {
		framelen = params.getFrameSize();
		FS = params.getHertz();
	}


	/* (non-Javadoc)
	 * @see v9t9.engine.speech.encode.ILPCEngine#filter(float[], float[], int, int)
	 */
	@Override
	public void filter(float[] in, int offs, int len, float[] out, float[] y) {
		int i,j;
		float xv10, xv11, xv12, yv10, yv11, yv12, xv30, yv30, yv31, yv32;
		float xv20, xv21, yv20, yv21, xv40, xv41, yv40, yv41;

		xv10 = xv1[0];
		xv11 = xv1[1];
		xv12 = xv1[2];
		yv10 = yv1[0];
		yv11 = yv1[1];
		yv12 = yv1[2];
		xv30 = xv3[0];
		yv30 = yv3[0];
		yv31 = yv3[1];
		yv32 = yv3[2];
		/*
		 * convert short data in buf[] to signed lin. data in s[] and
		 * prefilter
		 */
		for (i = 0, j = len - framelen; i < framelen; i++, j++) {

			/* special handling here for the intitial conversion */
			float u = in[i + offs];

			/*
			 * Anti-hum 2nd order Butterworth high-pass, 100 Hz corner
			 * frequency
			 */
			/*
			 * Digital filter designed by mkfilter/mkshape/gencode A.J.
			 * Fisher mkfilter -Bu -Hp -o 2 -a 0.0125 -l -z
			 */

			xv10 = xv11;
			xv11 = xv12;
			xv12 = u * 0.94597831f; /* /GAIN */
			//xv12 = u * 1.0101251f; /* /GAIN */
			yv10 = yv11;
			yv11 = yv12;
			yv12 = ((xv10 + xv12) - (xv11 + xv11) + -0.8948742499f * yv10 + 1.8890389823f * yv11);
			//yv12 = ((xv10 + xv12) - (xv11 + xv11) + -0.9800f * yv10 + 1.9798533f * yv11);
			u = out[j] = yv12; /*
							 * also affects input of next stage, to the LPC
							 * filter synth
							 */

			/* low-pass filter s[] -> y[] before computing pitch */
			/* second-order Butterworth low-pass filter, corner at 300 Hz */
			/*
			 * Digital filter designed by mkfilter/mkshape/gencode A.J.
			 * Fisher MKFILTER.EXE -Bu -Lp -o 2 -a 0.0375 -l -z
			 */
			
			// ejs: redone for 44100 hz
			xv30 = u * 0.04699658f; /* GAIN */
			//xv30 = u * 0.1772f; /* GAIN */
			yv30 = yv31;
			yv31 = yv32;
			yv32 = xv30 + -0.7166152306f * yv30 + 1.6696186545f * yv31;
			//yv32 = xv30 + -0.941347f * yv30 + 1.939575f * yv31;
			y[j] = yv32;
		}
		xv1[0] = xv10;
		xv1[1] = xv11;
		xv1[2] = xv12;
		yv1[0] = yv10;
		yv1[1] = yv11;
		yv1[2] = yv12;
		xv3[0] = xv30;
		yv3[0] = yv30;
		yv3[1] = yv31;
		yv3[2] = yv32;
		if (true) {
			/*
			 * operate optional preemphasis s[] -> s[] on the newly arrived
			 * frame
			 */
			xv20 = xv2[0];
			xv21 = xv2[1];
			yv20 = yv2[0];
			yv21 = yv2[1];
			xv40 = xv4[0];
			xv41 = xv4[1];
			yv40 = yv4[0];
			yv41 = yv4[1];
			for (j = len - framelen; j < len; j++) {
				float u = out[j + offs];

				/* handcoded filter: 1 zero at 640 Hz, 1 pole at 3200 */
				float TAU = (FS / 3200.f);
				float RHO = (0.1f);
				xv20 = xv21; /* e(n-1) */
				xv21 = u * 1.584f; /*
									 * e(n) , add 4 dB to compensate
									 * attenuation
									 */
				yv20 = yv21;
				yv21 = (TAU / (1.0f + RHO + TAU)) * yv20 /* u(n) */
						+ ((RHO + TAU) / (1.0f + RHO + TAU)) * xv21
						- (TAU / (1.0f + RHO + TAU)) * xv20;
				u = yv21;

				/*
				 * cascaded copy of handcoded filter: 1 zero at 640 Hz, 1
				 * pole at 3200
				 */
				xv40 = xv41;
				xv41 = u * 1.584f;
				yv40 = yv41;
				yv41 = (TAU / (1.0f + RHO + TAU)) * yv40
						+ ((RHO + TAU) / (1.0f + RHO + TAU)) * xv41
						- (TAU / (1.0f + RHO + TAU)) * xv40;
				u = yv41;

				out[j] = u;
			}
			xv2[0] = xv20;
			xv2[1] = xv21;
			yv2[0] = yv20;
			yv2[1] = yv21;
			xv4[0] = xv40;
			xv4[1] = xv41;
			yv4[0] = yv40;
			yv4[1] = yv41;
		} // preemph


	}
	

}
