/**
 * 
 */
package v9t9.engine.speech.encode;

import v9t9.engine.speech.RomTables;

/**
 * @author ejs
 *
 */
public class OpenLPCEngine implements ILPCEngine {

	private LPCEncoderParams params;

	private static boolean filter = true;
	
	private static final int MAXWINDOW=	441000;	/* Max analysis window length */
	//private static final float FS	 =	8000.0f;	/* Sampling rate */

	private static int DOWN		= 10;	/* Decimation for pitch analyzer */
	private static int PITCHORDER =	4;	/* Model order for pitch analyzer */
	private static float FC		= 200.0f;	/* Pitch analyzer filter cutoff */
	private static float MINPIT		= 50.0f;	/* Minimum pitch */
	private static float MAXPIT		= 900.0f;	/* Maximum pitch */

	private static float WSCALE		= 1.5863f;	/* Energy loss due to windowing */

	private int framelen, buflen;

	float s[], y[], h[];
	float fa[], u, u1, yp1, yp2;

	int pitchctr;
	float Oldper, OldG, Oldk[];
	float b[], bp[], f[];

	private int order;

	private int FS;

	private int MINPER;

	private int MAXPER;

	private int vuv;

	private float xv1[] = new float[3];
	private float yv1[] = new float[3];
	private float xv2[] = new float[3];
	private float yv2[] = new float[3];
	private float xv3[] = new float[3];
	private float yv3[] = new float[3];
	private float xv4[] = new float[3];
	private float yv4[] = new float[3];

	/**
	 * 
	 */
	public OpenLPCEngine(LPCEncoderParams params) {
		this.params = params;
		order = params.getOrder();
		FS = params.getHertz();
		
		MINPER	=	(int)(FS/(DOWN*MAXPIT)+.5);	/* Minimum period */
		MAXPER	=	(int)(FS/(DOWN*MINPIT)+.5);	/* Maximum period */
		
		s = new float[MAXWINDOW];
		y = new float[MAXWINDOW];
		h = new float[MAXWINDOW];
		fa = new float[6];
		Oldk = new float[order + 1];
		b = new float[order + 1];
		bp = new float[order + 1];
		f = new float[order + 1];
		
		init();
	}
	
	public void init() {
	  int i;
	  float r, v, w, wcT;

	  framelen = params.getFrameSize();
	  buflen = params.getFrameSize()*3/2;
	  if (buflen > MAXWINDOW) throw new IllegalArgumentException();

	  for (i = 0; i < buflen; i++) {
	    s[i] = 0.0f;
	    h[i] = (float) (WSCALE*(0.54f - 0.46f * Math.cos(2 * Math.PI * i / (buflen-1))));
	  }

	  wcT = (float) (2 * Math.PI * FC / FS);
	  r = 0.36891079f * wcT;
	  v = 0.18445539f * wcT;
	  w = 0.92307712f * wcT;
	  fa[1] = (float) -Math.exp(-r);
	  fa[2] = 1 + fa[1];
	  fa[3] = (float) (-2 * Math.exp(-v) * Math.cos(w));
	  fa[4] = (float) Math.exp(-2.0f * v);
	  fa[5] = 1 + fa[3] + fa[4];

	  u1 = 0;
	  yp1 = 0;
	  yp2 = 0;

	  Oldper = 0;
	  OldG = 0;
	  for (i=1; i <= order; i++) Oldk[i] = 0.0f;
	  for (i=0; i <= order; i++) b[i] = bp[i] = f[i] = 0.0f;
	  pitchctr = 0;
	}

	private void auto_correl(float[] w, int n, int p, float[] r) {
	  int i, k, nk;
	
	  p = Math.min(r.length - 1, p);
	  for (k=0; k <= p; k++) {
	    nk = n-k;
	    r[k] = 0.0f;
	    for (i=0; i < nk; i++) r[k] += w[i] * w[i+k];
	  }
	}

	private float durbin(float[] r, int p, float[] k) {
		
	  int i, j;
	  float e;
	  
	  float[] a = new float[order + 1];
	  float[] at = new float[order + 1];
	  
	  p = Math.min(order, p);
	  for (i=0; i <= p; i++) a[i] = at[i] = 0.0f;
	    
	  e = r[0];
	  for (i=1; i <= p; i++) {
	    k[i] = -r[i];
	    for (j=1; j < i; j++) {
	      at[j] = a[j];
	      k[i] -= a[j] * r[i-j];
	    }
	    k[i] /= e;
	    a[i] = k[i];
	    for (j=1; j < i; j++) a[j] = at[j] + k[i] * at[i-j];
	    e *= 1.0 - k[i]*k[i];
	  }
	
	  if (e < 0 || Float.isInfinite(e) || Float.isNaN(e))
		  return 1.0f;
		  
	  return (float) Math.min(1.0f, Math.sqrt(e));
	}

	
	private void inverse_filter(float[] w, float[] k) {
	  int i, j;
	  float b[] = new float[PITCHORDER+1];
	  float bp[] = new float[PITCHORDER+1];
	  float f[] = new float[PITCHORDER+1];
	  
	  for (i = 0; i <= PITCHORDER; i++) b[i] = f[i] = bp[i] = 0.0f;
	    
	  for (i = 0; i < buflen/DOWN; i++) {
	    f[0] = b[0] = w[i];
	    for (j = 1; j <= PITCHORDER; j++) {
	      f[j] = f[j-1] + k[j] * bp[j-1];
	      b[j] = k[j] * f[j-1] + bp[j-1];
	      bp[j-1] = b[j-1];
	    }
	    w[i] = f[PITCHORDER];
	  }
	}


	private float calc_pitch(float[] w) {
	  int i, j, rpos;
	  float rmax;
	  float d[] = new float[MAXWINDOW/DOWN];
	  float k[] = new float[PITCHORDER+1];
	  float r[] = new float[MAXPER+1];
	  float rval, rm, rp;
	  float a, b, c, x, y;
	  
	  for (i=0, j=0; i < buflen; i+=DOWN) d[j++] = w[i];
	  auto_correl(d, buflen/DOWN, PITCHORDER, r);
	  durbin(r, PITCHORDER, k);
	  inverse_filter(d, k);
	  auto_correl(d, buflen/DOWN, MAXPER+1, r);
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
	  
	  rm = r[rpos-1];
	  rp = r[rpos+1];
	  rval = rmax / r[0];
	
	  a = 0.5f * rm - rmax + 0.5f * rp;
	  b = -0.5f*rm*(2*rpos+1) + 2*rpos*rmax + 0.5f*rp*(1-2*rpos);
	  c = 0.5f*rm*(rpos*rpos+rpos) + rmax*(1-rpos*rpos) + 0.5f*rp*(rpos*rpos-rpos);
	
	  x = -b / (2.0f * a);
	  y = a*x*x + b*x + c;
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

	/* (non-Javadoc)
	 * @see v9t9.audio.speech.encode.ILPCEngine#analyze(float[], int, int)
	 */
	@Override
	public LPCAnalysisFrame analyze(float[] x, int offs, int len)
	{
		LPCAnalysisFrame frame = new LPCAnalysisFrame(params.getOrder());
	  int i, j;
	  float w[] = new float[MAXWINDOW];
	  float r[] = new float[order+1];
	  float per, G;
	  float k[] = new float[order+1];

	  if (filter) {
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
		    /* convert short data in buf[] to signed lin. data in s[] and prefilter */
		    for (i=0, j=buflen - framelen; i < framelen; i++, j++) {
		        
		        /* special handling here for the intitial conversion */
		        float u = x[i];
		        
		        /* Anti-hum 2nd order Butterworth high-pass, 100 Hz corner frequency */
		        /* Digital filter designed by mkfilter/mkshape/gencode   A.J. Fisher
		        mkfilter -Bu -Hp -o 2 -a 0.0125 -l -z */
		        
		        xv10 = xv11;
		        xv11 = xv12; 
		        xv12 = u * 0.94597831f; /* /GAIN */
		        yv10 = yv11;
		        yv11 = yv12;
		        yv12 = ((xv10 + xv12) - (xv11 + xv11)
		            + -0.8948742499f * yv10 + 1.8890389823f * yv11);
		        u = s[j] = yv12;	/* also affects input of next stage, to the LPC filter synth */
		        
		        /* low-pass filter s[] -> y[] before computing pitch */
		        /* second-order Butterworth low-pass filter, corner at 300 Hz */
		        /* Digital filter designed by mkfilter/mkshape/gencode   A.J. Fisher
		        MKFILTER.EXE -Bu -Lp -o 2 -a 0.0375 -l -z */
		        xv30 = u * 0.04699658f; /* GAIN */
		        yv30 = yv31;
		        yv31 = yv32; 
		        yv32 = xv30 + -0.7166152306f * yv30 + 1.6696186545f * yv31;
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
			    /* operate optional preemphasis s[] -> s[] on the newly arrived frame */
			    xv20 = xv2[0];
			    xv21 = xv2[1];
			    yv20 = yv2[0];
			    yv21 = yv2[1];
			    xv40 = xv4[0];
			    xv41 = xv4[1];
			    yv40 = yv4[0];
			    yv41 = yv4[1];
			    for (j=buflen - framelen; j < buflen; j++) {
			        float u = s[j];
			        
			        /* handcoded filter: 1 zero at 640 Hz, 1 pole at 3200 */
			        float TAU = (FS / 3200.f);
			        float RHO = (0.1f);
			        xv20 = xv21; 	/* e(n-1) */
			        xv21 = u * 1.584f;		/* e(n)	, add 4 dB to compensate attenuation */
			        yv20 = yv21; 
			        yv21 = (TAU/(1.0f+RHO+TAU)) * yv20 	 /* u(n) */
			            + ((RHO+TAU)/(1.0f+RHO+TAU)) * xv21
			            - (TAU/(1.0f+RHO+TAU)) * xv20;
			        u = yv21;
			        
			        /* cascaded copy of handcoded filter: 1 zero at 640 Hz, 1 pole at 3200 */
			        xv40 = xv41; 	
			        xv41 = u * 1.584f;
			        yv40 = yv41; 
			        yv41 = (TAU/(1.0f+RHO+TAU)) * yv40 
			            + ((RHO+TAU)/(1.0f+RHO+TAU)) * xv41
			            - (TAU/(1.0f+RHO+TAU)) * xv40;
			        u = yv41;
			        
			        s[j] = u;
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
		    
	  } else {
		  // original
		  for (i=0, j=buflen-framelen; i < framelen; i++, j++) {
			  s[j] = x[i];
			  u = fa[2] * s[j] - fa[1] * u1;
			  y[j] = fa[5] * u1 - fa[3] * yp1 - fa[4] * yp2;
			  u1 = u;
			  yp2 = yp1;
			  yp1 = y[j];
		  }
	  }
	  
	  per = calc_pitch(y);
	  
	  for (i=0; i < buflen; i++) w[i] = s[i] * h[i];
	  auto_correl(w, buflen, order, r);
	  G = durbin(r, order, k);

	  frame.invPitch = per;
	  frame.power = G;
	  frame.powerScale = 1.0f;
	  for (i=0; i < order; i++) { 
		  //frame.coefs[i] = Math.max(-1f, Math.min(1f, k[i+1]));
		  frame.coefs[i] =  k[i+1];
	  }

	  System.arraycopy(s, framelen, s, 0, buflen - framelen);
	  System.arraycopy(y, framelen, y, 0, buflen - framelen);
	  
	  return frame;
	}

	
	/* (non-Javadoc)
	 * @see v9t9.audio.speech.encode.ILPCEngine#synthesize(float[], int, int, v9t9.audio.speech.encode.RtLPCEngine.LPCAnalysisFrame)
	 */
	@Override
	public void synthesize(float[] y, int offs, int len, int playbackHz, LPCAnalysisFrame frame) {
	  int i, j, flen=len;
	  float pitchMul = (float )playbackHz / params.getHertz();
	  float per, G;
	  float k[] = new float[order+1];
	  float u, NewG, Ginc, Newper, perinc;
	  float Newk[] = new float[order+1];
	  float kinc[] = new float[order+1];

	  per = frame.invPitch * pitchMul;
	  G = frame.power;
	  k[0] = 0.0f;
	  for (i=0; i < order; i++) k[i+1] = frame.coefs[i];

	  if (per == 0.0) {
	    G /= Math.sqrt(buflen/3.0f);
	  } else {
	    i = (int) (buflen / per);
	    if (i == 0) i = 1;
	    G /= Math.sqrt((float)i);
	  }

	  Newper = Oldper;
	  NewG = OldG;
	  for (i=1; i <= order; i++) Newk[i] = Oldk[i];
	    
	  if (Oldper != 0 && per != 0) {
	    perinc = (per-Oldper)  / flen;
	    Ginc = (G-OldG) / flen;
	    for (i=1; i <= order; i++) kinc[i] = (k[i]-Oldk[i]) / flen;
	  } else {
	    perinc = 0.0f;
	    Ginc = 0.0f;
	    for (i=1; i <= order; i++) kinc[i] = 0.0f;
//	    Arrays.fill(b, 0f);
//	    Arrays.fill(bp, 0f);
	  }
	    
	  if (Newper == 0) pitchctr = 0;
	    
	  for (i=0; i < flen; i++) {
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
	    		u = pitchctr < RomTables.chirptable.length ? RomTables.chirptable[pitchctr] * NewG / 128 : 0;
	    		pitchctr =  ((pitchctr + 1) % (int) Newper);
	    	}
	    }
	      
	    f[order] = u;
	    for (j=order; j >= 1; j--) {
	      f[j-1] = f[j] - Newk[j] * bp[j-1];
	      if (Float.isNaN(f[j-1]) || Float.isInfinite(f[j-1]))
	    	  f[j-1] = 0.f;
	      b[j] = Newk[j] * f[j-1] + bp[j-1];
	      if (Float.isNaN(b[j]) || Float.isInfinite(b[j]))
	    	  bp[j] = b[j] = 0f;
	      else
	    	  bp[j] = b[j];
	    }
	    b[0] = bp[0] = f[0];
	    
	    y[i] = b[0];
	    
	    Newper += perinc;
	    NewG += Ginc;
	    for (j=1; j <= order; j++) Newk[j] += kinc[j];
	  }
	  
	  Oldper = per;
	  OldG = G;
	  for (i=1; i <= order; i++) Oldk[i] = k[i];
	}

}
