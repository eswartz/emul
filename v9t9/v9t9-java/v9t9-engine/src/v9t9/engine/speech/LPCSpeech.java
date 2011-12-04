/**
 * 
 */
package v9t9.engine.speech;

import java.util.Arrays;

import v9t9.base.properties.SettingProperty;
import v9t9.base.settings.Logging;


/**
 * decoder engine
 * @author ejs
 *
 */
public class LPCSpeech {
	public static final SettingProperty settingLogSpeech = new SettingProperty("LogSpeech",
			new Integer(0));

	boolean	rpt;				/* repeat */
	int		pnv,env;			/* pitch, energy new value */
	int		pbf,ebf;			/* pitch, energy buffer */
	int		knv[],kbf[];		/* K interp values, new values, old values */

	int		decode;			/* speech flags */
final static int FL_unvoiced = 1;		/* unvoiced? */
final static int FL_nointerp = 2;		/* no interpolation */
final static int FL_first	= 4;		/* first frame? */
final static int FL_last	= 8;		/* stop frame seen */

	int		b[],y[];		/* lattice filter */
	int		ns1,ns2;		/* unvoiced hiss registers */
	int		ppctr;			/* pitch counter */
	
	public LPCSpeech() {
		knv = new int[12];
		kbf = new int[12];
		b = new int[12];
		y = new int[12];
	}
	
	public synchronized void init() {
		decode = FL_first;
		ns1 = 0xaaaaaaaa;
		ns2 = 0x1;
		rpt = false;
		pnv = env = 0;
		pbf = ebf = 0;
		Arrays.fill(knv, 0);
		Arrays.fill(kbf, 0);
		Arrays.fill(b, 0);
		Arrays.fill(y, 0);
		ppctr = 0;
	}
	
	public void stop() {
		
	}
	


	//final static int ONE = (32768>>6);
	private final static int ONE = (32768);
	private static short LPC_TO_PCM(int ylatch) {
		if (ylatch <= -0x200)
			return -0x8000;
		if (ylatch >= 0x200)
			return 0x7fff;
	
		return (short) (ylatch << 6);
	}

	private void clearToSilence()
	{
		pnv = 12;
		env = 0;
		Arrays.fill(knv, 0);

		knv[0] = 0;
		knv[1] = 0;
		knv[2] = 0;
		knv[3] = 0;
		knv[4] = 0;
		knv[5] = 0;
		knv[6] = 0;
		knv[7] = 0;
		knv[8] = 0;
		knv[9] = 0;
		
		// if the previous frame was unvoiced,
		// it would sound bad to interpolate.
		// just clear it all out.
		if ((decode & FL_unvoiced) != 0) {
			pbf = 12;
			ebf = 0;
			Arrays.fill(kbf, 0);
			
			decode &= ~FL_unvoiced;
		}
	}
	
	/**
	 *	Read an equation from the bit source.
	 */
	private void readEquation(ILPCDataFetcher fetcher, boolean forceUnvoiced)
	{
		StringBuilder builder = new StringBuilder();

		/* 	Copy now-old 'new' values into 'buffer' values */
		ebf = env;
		pbf = pnv;
		System.arraycopy(knv, 0, kbf, 0, kbf.length);

		/*  Read energy  */
		env = fetcher.fetch(4);
		builder.append("E: " +  env + " ");
		if (env == 15) {
			decode |= FL_last;
			clearToSilence();	/* clear params */
		} else if (env == 0) {	/* silent frame */
			if ((decode & FL_unvoiced) != 0)	/* unvoiced before? */ 
				decode |= FL_nointerp;
			else
				decode &= ~FL_nointerp;
			clearToSilence();	/* clear params */
		} else {
			/*  Repeat bit  */
			rpt = fetcher.fetch(1) != 0;
			builder.append("R: " + rpt + " ");

			/*  Pitch code  */
			pnv = fetcher.fetch(6);
			builder.append("P: " + pnv + " ");

			if (pnv == 0) {		/* unvoiced */
				if ((decode & FL_unvoiced) != 0)	/* voiced before? */
					decode |= FL_nointerp;	/* don't interpolate */
				else
					decode &= ~FL_nointerp;
				decode |= FL_unvoiced;
				pnv = 12;		/* set some pitch */

				if (ebf == 0)	/* previous frame silent? */
					decode |= FL_nointerp;
				
				/* reset pitch on voiced->unvoiced transition*/
				ppctr = 0;

			} else {				/* voiced */

				pnv = RomTables.pitchtable[pnv] >> 8;

				if ((decode & FL_unvoiced) != 0)	/* unvoiced before? */
					decode |= FL_nointerp;	/* don't interpolate */
				else
					decode &= ~FL_nointerp;

				decode &= ~FL_unvoiced;
			}

			/* translate energy */
			//env = KTRANS(energytable[env]);
			env = RomTables.energytable[env] >> 6;		// 15-bit to 9-bit

			/*  Get K parameters  */

			if (!rpt) {			/* don't repeat previous frame? */
				int         tmp;

				tmp = fetcher.fetch(5);
				knv[0] = RomTables.k1table[tmp];
				builder.append("K0: " + tmp + " [" + knv[0] + "] ");

				tmp = fetcher.fetch(5);
				knv[1] = RomTables.k2table[tmp];
				builder.append("K1: " + tmp + " [" + knv[1] + "] ");

				tmp = fetcher.fetch(4);
				knv[2] = RomTables.k3table[tmp];
				builder.append("K2: " + tmp +" [" + knv[2] + "] ");

				tmp = fetcher.fetch(4);
				
				knv[3] = RomTables.k4table[tmp];
				//knv[3] = RomTables.k3table[tmp];	// bug in pre-TMS5220, according to MAME... 
				builder.append("K3: " + tmp + " [" + knv[3] + "] ");


				if (0 == (decode & FL_unvoiced)) {	/* unvoiced? */
					tmp = fetcher.fetch(4);
					knv[4] = RomTables.k5table[tmp];
					builder.append("K4: " + tmp + " [" + knv[4] + "] ");


					tmp = fetcher.fetch(4);
					knv[5] = RomTables.k6table[tmp];
					builder.append("K5: " + tmp + " [" + knv[5] + "] ");

					tmp = fetcher.fetch(4);
					knv[6] = RomTables.k7table[tmp];
					builder.append("K6: " + tmp + " [" + knv[6] + "] ");

					tmp = fetcher.fetch(3);
					knv[7] = RomTables.k8table[tmp];
					builder.append("K7: " + tmp + " [" + knv[7] + "] ");

					tmp = fetcher.fetch(3);
					knv[8] = RomTables.k9table[tmp];
					builder.append("K8: " + tmp + " [" + knv[8] + "] ");

					tmp = fetcher.fetch(3);
					knv[9] = RomTables.k10table[tmp];
					builder.append("K9: " + tmp + " [" + knv[9] + "] ");
				} else {
					knv[4] = 0;
					knv[5] = 0;
					knv[6] = 0;
					knv[7] = 0;
					knv[8] = 0;
					knv[9] = 0;
				}
			}
		}

		if (forceUnvoiced) {
			decode |= FL_unvoiced;
			knv[4] = 0;
			knv[5] = 0;
			knv[6] = 0;
			knv[7] = 0;
			knv[8] = 0;
			knv[9] = 0;
		}

		Logging.writeLogLine(2, LPCSpeech.settingLogSpeech,
				"Equation: " + builder);

		Logging.writeLogLine(3, LPCSpeech.settingLogSpeech,
				"ebf="+ebf+", pbf="+pbf+", env="+env+", pnv="+pnv);
	}

	/*
	Interpolate "new" values and "buffer" values.
	*/
	private void interpolate(int period)
	{
		int         x;
	
		if (0 == (decode & FL_nointerp)) {
			ebf += (env - ebf) / RomTables.interp_coeff[period];
			if (pbf != 0)
				pbf += (pnv - pbf) / RomTables.interp_coeff[period];
			for (x = 0; x < 11; x++)
				kbf[x] += (knv[x] - kbf[x]) / RomTables.interp_coeff[period];
		}
	
		//logger(_L|L_1, "[%d] ebf=%d, pbf=%d\n", period, ebf, pbf);
	}


	/*
	 *	Generate PCM data for one LPC frame.
	 *
	 */
	private void calc(ISpeechDataSender sender, int length) {
		int         frame, framesize;
		int			stage;
		int			U;
		int			pos = 0; 		

		/* excitation data */
		U = 0;

		frame = 0;
		framesize = (length + 7) / 8;

		while (pos < length) {
			int         samp;

			/* interpolate parameters? */
			if (framesize != 0 && (pos % framesize) == 0) {
				interpolate(frame);
				frame++;
			}

			/*  Update excitation data in U? */
			if ((decode & FL_unvoiced) != 0) {
				U = (ns1 & 1) != 0 ? ebf : -ebf ;

				U >>= 2;
				/* noise generator */
				ns1 = (ns1 << 1) | (ns1 >>> 31);
				ns1 ^= ns2;
				if ((ns2 += ns1) == 0)
					ns2++;
			} else {
				/* get next chirp value */
				U = ppctr < RomTables.chirptable.length ? RomTables.chirptable[ppctr] * ebf / 128 : 0;

				if (pbf != 0) 
					ppctr = (ppctr + 1) % pbf;
				else	
					ppctr = 0;

			}

			/*  -----------------------------------------
				   10-stage lattice filter.

				   range 1..10 here, 0..9 in our arrays

				   Y10(i) = U(i) - K10*B10(i-1) U(i)=excitation
				   ----
				   Y9(i) = Y10(i) - K9*B9(i-1)
				   B10(i)= B9(i-1) + K9*Y9(i)
				   ----
				   ...
				   Y1(i) = Y2(i) - K1*B1(i-1)
				   B2(i) = B1(i-1) + K1*Y1(i)
				   ----
				   B1(i) = Y1(i)
				   ----------------------------------------- */

				/*  Stage 10 is different than the others.
				   Instead of calculating B11, we scale the excitation by
				   the energy.

				 */

			y[10] = U;
			for (stage = 9; stage >= 0; stage--) {
				y[stage] = y[stage + 1] - ((kbf[stage] * b[stage]) / ONE);
			}
			for (stage = 9; stage >= 1; stage--) {
				b[stage] = b[stage - 1]	+ ((kbf[stage - 1] * y[stage - 1]) / ONE);
			}

			samp = y[0];
			b[0] = samp;

			samp >>= 1;
			
			//if (samp > 511 || samp < -512)
			//	System.err.println("samp["+pos+"]="+samp);

			sender.send(LPC_TO_PCM(samp), pos, length);
			pos++;
		}
	}
	

	/*	
		One LPC frame consists of decoding one equation (or repeating,
		or stopping), and calculating a speech waveform and outputting it.
		
		This happens during an interrupt.
		
		If we're here, we have enough data to form any one equation.
		@return 1 to continue, 0 if end of frame
	*/
	public synchronized boolean frame(ILPCDataFetcher fetcher, ISpeechDataSender sender, int length)
	{
		if ((decode & FL_last) == 0) {
			readEquation(fetcher, false);
			if ((decode & (FL_nointerp | FL_first)) != 0)
				decode &= ~FL_first;
			
			ppctr = 0;
			
			Arrays.fill(y, 0);
			Arrays.fill(b, 0);
			
			calc(sender, length);
			return (decode & FL_last) == 0;	/* not last frame */
		}
		else
			return false;
	}


}
