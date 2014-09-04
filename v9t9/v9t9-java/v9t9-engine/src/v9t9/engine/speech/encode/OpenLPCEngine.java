/*
  OpenLPCEngine.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.speech.encode;

import v9t9.engine.speech.RomTables;

/**
 * from http://read.pudn.com/downloads116/sourcecode/zip/491999/voice_compress/5213216/src/openlpc/openlpc.c__.htm
 * @author ejs
 * 
 */
public class OpenLPCEngine implements ILPCEngine {

	private LPCEncoderParams params;

	private static final int MAXWINDOW = 1000*44/8; /* Max analysis window length */
	// private static final float FS = 8000.0f; /* Sampling rate */

	private static int DOWN = 5; /* Decimation for pitch analyzer */
	private static int PITCHORDER = 4; /* Model order for pitch analyzer */

	private static float MINPIT = 50.0f; /* Minimum pitch */
	private static float MAXPIT = 1200.0f; /* Maximum pitch */

	private static float WSCALE = 1.5863f; /* Energy loss due to windowing */
	private int framelen, buflen;

	float y[], h[], s[];
	float u, u1;
	float yp1, yp2;

	int pitchctr;
	float Oldper, OldG, Oldk[];
	float b[], bp[], f[];

	private int order;

	private int FS;

	private int MINPER;

	private int MAXPER;

	private int vuv;

	public OpenLPCEngine(LPCEncoderParams params) {
		this.params = params;
		order = params.getOrder();
		FS = params.getHertz();

		MINPER = (int) (FS / (DOWN * MAXPIT) + .5); /* Minimum period */
		MAXPER = (int) (FS / (DOWN * MINPIT) + .5); /* Maximum period */

		y = new float[MAXWINDOW];
		h = new float[MAXWINDOW];
		s = new float[MAXWINDOW];
		
		Oldk = new float[order + 1];
		b = new float[order + 1];
		bp = new float[order + 1];
		f = new float[order + 1];

		init();
	}

	public void init() {
		int i;
		

		framelen = params.getFrameSize();
		buflen = params.getFrameSize() * 3 / 2;
		if (buflen > MAXWINDOW)
			throw new IllegalArgumentException();

		for (i = 0; i < buflen; i++) {
			h[i] = (float) (WSCALE * (0.54f - 0.46f * Math.cos(2 * Math.PI * i
					/ (buflen - 1))));
		}
		
		Oldper = 0;
		OldG = 0;
		for (i = 1; i <= order; i++)
			Oldk[i] = 0.0f;
		for (i = 0; i <= order; i++)
			b[i] = bp[i] = f[i] = 0.0f;
		pitchctr = 0;
	}
	private void auto_correll(float[] w, int n, int p, float[] r) {
		int i, k;

		p = Math.min(r.length - 1, p);
		for (k = 0; k <= MAXPER; k++, n--) {
			r[k] = 0.0f;
			for (i = 0; i < n; i++)
				r[k] += w[i] * w[i + k];
		}
	}
	private void auto_correl2(float[] w, int n, int p, float[] r) {
		int i, k;

		p = Math.min(r.length - 1, p);
		for (k = 0; k <= p; k++, n--) {
			r[k] = 0.0f;
			for (i = 0; i < n; i++)
				r[k] += w[i] * w[i + k];
		}
	}

	private float durbin(float[] r, int p, float[] k) {

		int i, j;
		float e;

		float[] a = new float[order + 1];
		float[] at = new float[order + 1];

		p = Math.min(order, p);
		for (i = 0; i <= p; i++)
			a[i] = at[i] = 0.0f;

		e = r[0];
		for (i = 1; i <= p; i++) {
			k[i] = -r[i];
			for (j = 1; j < i; j++) {
				at[j] = a[j];
				k[i] -= a[j] * r[i - j];
			}
			if (e == 0)
				return 0;

			k[i] /= e;
			a[i] = k[i];
			for (j = 1; j < i; j++)
				a[j] = at[j] + k[i] * at[i - j];
			e *= 1.0 - k[i] * k[i];
		}

		if (e < 0)
			return 0;

//		return (float) Math.max(1.0f, Math.sqrt(e));
		return (float) Math.sqrt(e);
	}

	private void inverse_filter(float[] w, float[] k) {
		int i, j;
		float b[] = new float[PITCHORDER + 1];
		float bp[] = new float[PITCHORDER + 1];
		float f[] = new float[PITCHORDER + 1];

		for (i = 0; i <= PITCHORDER; i++)
			b[i] = f[i] = bp[i] = 0.0f;

		for (i = 0; i < buflen / DOWN; i++) {
			f[0] = b[0] = w[i];
			for (j = 1; j <= PITCHORDER; j++) {
				f[j] = f[j - 1] + k[j] * bp[j - 1];
				b[j] = k[j] * f[j - 1] + bp[j - 1];
				bp[j - 1] = b[j - 1];
			}
			w[i] = f[PITCHORDER];
		}
	}

	private float calc_pitch(float[] w, int offs, int len) {
		int i, j, rpos;
		float rmax;
		float d[] = new float[MAXWINDOW / DOWN];
		float k[] = new float[PITCHORDER + 1];
		float r[] = new float[MAXPER + 1];
		float rval, rm, rp;
		float a, b, c, x, y;

		/* decimation */
		for (i = 0, j = 0; i < len; i += DOWN)
			d[j++] = w[i+offs];

		auto_correll(d, len / DOWN, MAXPER, r);

		if (true) {
			float vthresh;

			/* find peak between MINPER and MAXPER */
			x = 1f;
			rpos = 0;
			rmax = 0;

			for (i = 1; i < MAXPER; i++) {
				rm = r[i - 1];
				rp = r[i + 1];
				y = rm + r[i] + rp; /* find max of integral from i-1 to i+1 */
				if (y > rmax && r[i] > rm && r[i] > rp && i > MINPER) {
					rmax = y;
					rpos = i;
				}
			}

			if (rpos == 0)
				return 0;

			/* consider adjacent values */
			rm = r[rpos - 1];
			rp = r[rpos + 1];

			if (rpos > 0) {
				x = (((rpos - 1) * rm + rpos * r[rpos] + (rpos + 1) * rp) / (rm
						+ r[rpos] + rp));
			}
			/* normalize, so that 0. < rval < 1. */
			rval = (r[0] == 0 ? 0 : (r[rpos] / r[0]));

			/*
			 * periods near the low boundary and at low volumes are usually
			 * spurious and manifest themselves as annoying mosquito buzzes
			 */

			float per = 0; /* default: unvoiced */
			if (x > MINPER && /* x could be < MINPER or even < 0 if rpos == MINPER */
			x < MAXPER + 1 /* same story */
			) {

				vthresh = 0.6f;
				if (r[0] > 0.002f) /* at low volumes (< 0.002), prefer unvoiced */
					vthresh = 0.25f; /* drop threshold at high volumes */

				if (rval > vthresh)
					per = x * DOWN;

			}
			return per;
		} else {
			durbin(r, PITCHORDER, k);
			inverse_filter(d, k);
			auto_correll(d, len / DOWN, MAXPER + 1, r);
			rpos = 0;
			rmax = 0.0f;
			for (i = MINPER; i < MAXPER; i++) {
				if (r[i] > rmax) {
					rmax = r[i];
					rpos = i;
				}
			}

			if (rpos == 0)
				return 0;

			rm = r[rpos - 1];
			rp = r[rpos + 1];
			rval = rmax / r[0];

			a = 0.5f * rm - rmax + 0.5f * rp;
			b = -0.5f * rm * (2 * rpos + 1) + 2 * rpos * rmax + 0.5f * rp
					* (1 - 2 * rpos);
			c = 0.5f * rm * (rpos * rpos + rpos) + rmax * (1 - rpos * rpos)
					+ 0.5f * rp * (rpos * rpos - rpos);

			x = -b / (2.0f * a);
			y = a * x * x + b * x + c;
			x *= DOWN;

			rmax = y;
			rval = rmax / r[0];
			if (rval >= 0.2) { // || (vuv == 3 && rval >= 0.2)) {
				vuv = (vuv & 1) * 2 + 1;
				return Math.abs(x);
			} else {
				vuv = (vuv & 1) * 2;
				return 0;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see v9t9.audio.speech.encode.ILPCEngine#analyze(float[], int, int)
	 */
	public LPCAnalysisFrame analyze_(float[] x, int offs, int len) {
		LPCAnalysisFrame frame = new LPCAnalysisFrame(params.getOrder());
		int i;
		float w[] = new float[MAXWINDOW];
		float r[] = new float[order + 1];
		float per, G;
		float k[] = new float[order + 1];

		per = calc_pitch(x, offs, len);

		for (i = 0; i < len; i++)
			w[i] = x[i + offs] * h[i];
		auto_correl2(w, buflen, order, r);
		G = durbin(r, order, k);

		frame.invPitch = per;
		frame.pitch = per != 0 ? (frame.invPitch * params.getPlaybackHz() / params.getHertz() ) : 0;
		frame.power = G;
		frame.powerScale = 1.0f;
		for (i = 0; i < order; i++) {
			// frame.coefs[i] = Math.max(-1f, Math.min(1f, k[i+1]));
			frame.coefs[i] = k[i + 1];
		}

		//System.arraycopy(s, framelen, s, 0, buflen - framelen);
		System.arraycopy(y, framelen, y, 0, buflen - framelen);

		return frame;
	}

	@Override
	public LPCAnalysisFrame analyze(float[] x, int offs, int len) {
		LPCAnalysisFrame frame = new LPCAnalysisFrame(params.getOrder());
		int i;
		float w[] = new float[MAXWINDOW];
		float r[] = new float[order + 1];
		float per, gain;
		float k[] = new float[order + 1];

	    System.arraycopy(x, offs, s, 0, len);
	       
	    /* operate windowing s[] -> w[] */   
	       
	    for (i=0; i < buflen; i++)   
	        w[i] = s[i] * h[i];   
	    
	    auto_correl2(w, buflen, order, r);
		gain = durbin(r, order, k);
	    
	    ///
		
	    
	    /* calculate pitch */   
	    float per1 = calc_pitch(y, 0, framelen);                 /* first 2/3 of buffer */   
	    float per2 = calc_pitch(y, buflen - framelen, framelen); /* last 2/3 of buffer */   
	    if(per1 > 0 && per2 >0)   
	        per = (per1+per2)/2;   
	    else if(per1 > 0)   
	        per = per1;   
	    else if(per2 > 0)   
	        per = per2;   
	    else   
	        per = 0;   
	       
//		per = calc_pitch(y);
//
//		for (i = 0; i < len; i++)
//			w[i] = x[i + offs] * h[i];
//		auto_correl2(w, buflen, order, r);
//		gain = durbin(r, order, k);

		frame.invPitch = per;
		frame.pitch = per != 0 ? (frame.invPitch * params.getPlaybackHz() / params.getHertz() ) : 0;
		frame.power = gain;
		frame.powerScale = 1.0f;
		for (i = 0; i < order; i++) {
			// frame.coefs[i] = Math.max(-1f, Math.min(1f, k[i+1]));
			frame.coefs[i] = k[i + 1];
		}

		//System.arraycopy(s, framelen, s, 0, buflen - framelen);
		System.arraycopy(y, framelen, y, 0, buflen - framelen);

		return frame;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.speech.encode.ILPCEngine#getY()
	 */
	@Override
	public float[] getY() {
		return y;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see v9t9.audio.speech.encode.ILPCEngine#synthesize(float[], int, int,
	 * v9t9.audio.speech.encode.RtLPCEngine.LPCAnalysisFrame)
	 */
	@Override
	public void synthesize(float[] y, int offs, int len, int playbackHz,
			LPCAnalysisFrame frame) {
		int i, j, flen = len;
		float pitchMul = (float) playbackHz / params.getHertz();
		float per, G;
		float k[] = new float[order + 1];
		float u, NewG, Ginc, Newper, perinc;
		float Newk[] = new float[order + 1];
		float kinc[] = new float[order + 1];

		per = frame.invPitch * pitchMul;
		G = frame.power;
		k[0] = 0.0f;
		for (i = 0; i < order; i++)
			k[i + 1] = frame.coefs[i];

		if (per == 0.0) {
			G /= Math.sqrt(buflen / 3.0f);
		} else {
			i = (int) (buflen / per);
			if (i == 0)
				i = 1;
			G /= Math.sqrt((float) i);
		}

		Newper = Oldper;
		NewG = OldG;
		for (i = 1; i <= order; i++)
			Newk[i] = Oldk[i];

		if (Oldper != 0 && per != 0) {
			perinc = (per - Oldper) / flen;
			Ginc = (G - OldG) / flen;
			for (i = 1; i <= order; i++)
				kinc[i] = (k[i] - Oldk[i]) / flen;
		} else {
			perinc = 0.0f;
			Ginc = 0.0f;
			for (i = 1; i <= order; i++)
				kinc[i] = 0.0f;
			// Arrays.fill(b, 0f);
			// Arrays.fill(bp, 0f);
		}

		if (Newper == 0)
			pitchctr = 0;

		for (i = 0; i < flen; i++) {
			if (Newper == 0) {
				u = (float) (Math.random() * NewG);
			} else {

				if (true) {
					if (pitchctr == 0) {
						u = NewG;
						pitchctr = (int) Newper;
					} else {
						u = 0.0f;
						pitchctr--;
					}
				} else {
					u = pitchctr < RomTables.chirptable.length ? RomTables.chirptable[pitchctr]
							* NewG / 128
							: 0;
					pitchctr = ((pitchctr + 1) % (int) Newper);
				}
			}

			f[order] = u;
			for (j = order; j >= 1; j--) {
				f[j - 1] = f[j] - Newk[j] * bp[j - 1];
				if (Float.isNaN(f[j - 1]) || Float.isInfinite(f[j - 1]))
					f[j - 1] = 0.f;
				b[j] = Newk[j] * f[j - 1] + bp[j - 1];
				if (Float.isNaN(b[j]) || Float.isInfinite(b[j]))
					bp[j] = b[j] = 0f;
				else
					bp[j] = b[j];
			}
			b[0] = bp[0] = f[0];

			y[i] = b[0];

			Newper += perinc;
			NewG += Ginc;
			for (j = 1; j <= order; j++)
				Newk[j] += kinc[j];
		}

		Oldper = per;
		OldG = G;
		for (i = 1; i <= order; i++)
			Oldk[i] = k[i];
	}

}
