/**
 * 
 */
package v9t9.engine.speech;

import java.util.Arrays;

import ejs.base.settings.Logging;
import ejs.base.utils.ListenerList;
import ejs.base.utils.ListenerList.IFire;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.hardware.ISpeechChip;
import v9t9.common.speech.ISpeechDataSender;
import v9t9.common.speech.ILPCParametersListener;


/**
 * LPC decoder and synthesis engine
 * @author ejs
 *
 */
public class LPCSpeech {
	LPCParameters params = new LPCParameters();
	LPCParameters oldParams = new LPCParameters();
	int		decode;			/* speech flags */
final static int FL_unvoiced = 1;		/* unvoiced? */
final static int FL_nointerp = 2;		/* no interpolation */
final static int FL_first	= 4;		/* first frame? */
final static int FL_last	= 8;		/* stop frame seen */

	int		b[],y[];		/* lattice filter */
	int		ns1,ns2;		/* unvoiced hiss registers */
	int		ppctr;			/* pitch counter */

	private final ISettingsHandler settings;

	private ListenerList<ISpeechDataSender> senderList;
	private ListenerList<ILPCParametersListener> paramListeners;
	
	public LPCSpeech(ISettingsHandler settings) {
		this.settings = settings;
		
		b = new int[12];
		y = new int[12];
	}
	
	public void setSenderList(ListenerList<ISpeechDataSender> senderList) {
		this.senderList = senderList;
	}
	public void setParamListeners(ListenerList<ILPCParametersListener> paramListeners) {
		this.paramListeners = paramListeners;
	}
	
	public synchronized void init() {
		decode = FL_first;
		ns1 = 0xaaaaaaaa;
		ns2 = 0x1;
		params.init();
		oldParams.init();
		Arrays.fill(b, 0);
		Arrays.fill(y, 0);
		ppctr = 0;
	}
	
	public synchronized void stop() {
		decode = FL_first;
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
//		params.pitch = 12;
		params.energyParam = 0;
		params.energy = 0;
		Arrays.fill(params.kVal, 0);
		
		// if the previous frame was unvoiced,
		// it would sound bad to interpolate.
		// just clear it all out.
//		if ((decode & FL_unvoiced) != 0) {
//			oldParams.pitch = RomTables.pitchtable[12] >> 8;
//			oldParams.energyParam = 0;
//			Arrays.fill(oldParams.kVal, 0);
//			
//			decode &= ~FL_unvoiced;
//		}
	}
	
	/**
	 *	Read an equation from the bit source.
	 */
	private void readEquation(ILPCDataFetcher fetcher, boolean forceUnvoiced)
	{
		StringBuilder builder = new StringBuilder();

		/* 	Copy now-old 'new' values into 'buffer' values */
		oldParams.copyFrom(params);

		/*  Read energy  */
		params.energy = fetcher.fetch(4);
		builder.append("E: " +  params.energy + " ");
		
		if (params.energy == 15) {
			decode |= FL_last;
			clearToSilence();	/* clear params */
		} else if (params.energy == 0) {	/* silent frame */
			if ((decode & FL_unvoiced) != 0)	/* unvoiced before? */ 
				decode |= FL_nointerp;
			else
				decode &= ~FL_nointerp;
			clearToSilence();	/* clear params */
		} else {
			/*  Repeat bit  */
			params.repeat = fetcher.fetch(1) != 0;
			builder.append("R: " + params.repeat + " ");

			/*  Pitch code  */
			params.pitch = fetcher.fetch(6);
			builder.append("P: " + params.pitch + " ");

			if (params.pitch == 0) {		/* unvoiced */
				if ((decode & FL_unvoiced) != 0)	/* voiced before? */
					decode |= FL_nointerp;	/* don't interpolate */
				else
					decode &= ~FL_nointerp;
				decode |= FL_unvoiced;
				params.pitch = 12;		/* set some pitch */

				if (oldParams.energy == 0)	/* previous frame silent? */
					decode |= FL_nointerp;
				
				/* reset pitch on voiced->unvoiced transition*/
				ppctr = 0;

			} else {				/* voiced */

				params.pitch = RomTables.pitchtable[params.pitch] >> 8;

				if ((decode & FL_unvoiced) != 0)	/* unvoiced before? */
					decode |= FL_nointerp;	/* don't interpolate */
				else
					decode &= ~FL_nointerp;

				decode &= ~FL_unvoiced;
			}

			/* translate energy */
			//env = KTRANS(energytable[env]);
			params.energy = RomTables.energytable[params.energy] >> 6;		// 15-bit to 9-bit

			/*  Get K parameters  */

			if (!params.repeat) {			
				/* don't repeat previous frame */
				int         tmp;

				tmp = fetcher.fetch(5);
				params.kVal[0] = RomTables.k1table[tmp];
				builder.append("K0: " + tmp + " [" + params.kVal[0] + "] ");

				tmp = fetcher.fetch(5);
				params.kVal[1] = RomTables.k2table[tmp];
				builder.append("K1: " + tmp + " [" + params.kVal[1] + "] ");

				tmp = fetcher.fetch(4);
				params.kVal[2] = RomTables.k3table[tmp];
				builder.append("K2: " + tmp +" [" + params.kVal[2] + "] ");

				tmp = fetcher.fetch(4);
				
				//params.kVal[3] = RomTables.k4table[tmp];
				params.kVal[3] = RomTables.k3table[tmp];	// bug in pre-TMS5220, according to MAME... 
				builder.append("K3: " + tmp + " [" + params.kVal[3] + "] ");


				if (0 == (decode & FL_unvoiced)) {	/* unvoiced? */
					tmp = fetcher.fetch(4);
					params.kVal[4] = RomTables.k5table[tmp];
					builder.append("K4: " + tmp + " [" + params.kVal[4] + "] ");


					tmp = fetcher.fetch(4);
					params.kVal[5] = RomTables.k6table[tmp];
					builder.append("K5: " + tmp + " [" + params.kVal[5] + "] ");

					tmp = fetcher.fetch(4);
					params.kVal[6] = RomTables.k7table[tmp];
					builder.append("K6: " + tmp + " [" + params.kVal[6] + "] ");

					tmp = fetcher.fetch(3);
					params.kVal[7] = RomTables.k8table[tmp];
					builder.append("K7: " + tmp + " [" + params.kVal[7] + "] ");

					tmp = fetcher.fetch(3);
					params.kVal[8] = RomTables.k9table[tmp];
					builder.append("K8: " + tmp + " [" + params.kVal[8] + "] ");

					tmp = fetcher.fetch(3);
					params.kVal[9] = RomTables.k10table[tmp];
					builder.append("K9: " + tmp + " [" + params.kVal[9] + "] ");
				} else {
					params.kVal[4] = 0;
					params.kVal[5] = 0;
					params.kVal[6] = 0;
					params.kVal[7] = 0;
					params.kVal[8] = 0;
					params.kVal[9] = 0;
				}
			}
		}

		if (forceUnvoiced) {
			decode |= FL_unvoiced;
			params.kVal[4] = 0;
			params.kVal[5] = 0;
			params.kVal[6] = 0;
			params.kVal[7] = 0;
			params.kVal[8] = 0;
			params.kVal[9] = 0;
		}

		Logging.writeLogLine(2, settings.get(ISpeechChip.settingLogSpeech),
				"Equation: " + builder);

		Logging.writeLogLine(3, settings.get(ISpeechChip.settingLogSpeech),
				"energy: "+oldParams.energy+" => "+params.energy+", pitch: "+oldParams.pitch+" => "+params.pitch);
	}

	/**
	Interpolate "new" values and "buffer" values.
	*/
	private void interpolate(int period)
	{
		int         x;
	
		if (0 == (decode & FL_nointerp)) {
			oldParams.energy += (params.energy - oldParams.energy) / RomTables.interp_coeff[period];
			if (oldParams.pitch != 0)
				oldParams.pitch += (params.pitch - oldParams.pitch) / RomTables.interp_coeff[period];
			for (x = 0; x < 10; x++)
				oldParams.kVal[x] += (params.kVal[x] - oldParams.kVal[x]) / RomTables.interp_coeff[period];
		}
	
		//logger(_L|L_1, "[%d] ebf=%d, pbf=%d\n", period, ebf, pbf);
	}


	/**
	 * Generate PCM data for one LPC frame.
	 * 
	 * @param length 
	 *
	 */
	private void calcFrameData(int length) {
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
				U = (ns1 & 1) != 0 ? oldParams.energy : -oldParams.energy ;

				U >>= 2;
				/* noise generator */
				ns1 = (ns1 << 1) | (ns1 >>> 31);
				ns1 ^= ns2;
				if ((ns2 += ns1) == 0)
					ns2++;
			} else {
				/* get next chirp value */
				U = ppctr < RomTables.chirptable.length ? RomTables.chirptable[ppctr] * oldParams.energy / 128 : 0;

				if (oldParams.pitch != 0) 
					ppctr = (ppctr + 1) % oldParams.pitch;
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
				y[stage] = y[stage + 1] - ((oldParams.kVal[stage] * b[stage]) / ONE);
			}
			for (stage = 9; stage >= 1; stage--) {
				b[stage] = b[stage - 1]	+ ((oldParams.kVal[stage - 1] * y[stage - 1]) / ONE);
			}

			samp = y[0];
			b[0] = samp;

			samp >>= 1;
			
			//if (samp > 511 || samp < -512)
			//	System.err.println("samp["+pos+"]="+samp);

			if (senderList != null && !senderList.isEmpty()) {
				for (Object o : senderList.toArray()) {
					((ISpeechDataSender) o).sendSample(LPC_TO_PCM(samp), pos, length);
				}
			}
			
			pos++;
		}
	}
	
	/**
	 * One LPC frame consists of decoding one equation (or repeating, or
	 * stopping), and calculating a speech waveform and outputting it.
	 * 
	 * This happens during an interrupt.
	 * 
	 * If we're here, we have enough data to form any one equation.
	 * 
	 * @param fetcher the mechanism to fetch the next parameter from an equation
	 * @param length number of samples
	 * @return true to continue, false if end of frame
	 */
	public synchronized boolean frame(ILPCDataFetcher fetcher, int length)
	{
		if ((decode & FL_last) != 0) 
			return false;
					
		readEquation(fetcher, false);
		if ((decode & FL_nointerp + FL_first) != 0)
			decode &= ~FL_first;
		
		calcFrameData(length);
		return (decode & FL_last) == 0;	/* not last frame */
	}


	

	/**
	 *	Apply an equation from the equation fetcher.
	 */
	private void applyEquation(LPCParameters newParams, boolean forceUnvoiced)
	{
		/* 	Copy now-old 'new' values into 'buffer' values */
		oldParams.copyFrom(params);

		/*  Get newly provided params */
		params.copyFrom(newParams);
		
		if (paramListeners != null && !paramListeners.isEmpty()) {
			paramListeners.fire(new IFire<ILPCParametersListener>() {

				@Override
				public void fire(ILPCParametersListener listener) {
					listener.parametersAdded(params);
				}
			});
		}
		
		if (params.isLast()) {
			decode |= FL_last;
			clearToSilence();	/* clear params */
		} else if (params.isSilent()) {	/* silent frame */
			if ((decode & FL_unvoiced) != 0)	/* unvoiced before? */ 
				decode |= FL_nointerp;
			else
				decode &= ~FL_nointerp;
			clearToSilence();	/* clear params */
		} else {
			if (params.isUnvoiced()) {		/* unvoiced */
				if ((decode & FL_unvoiced) != 0)	/* voiced before? */
					decode |= FL_nointerp;	/* don't interpolate */
				else
					decode &= ~FL_nointerp;
				decode |= FL_unvoiced;
				//params.pitch = 12;		/* set some pitch */
				params.pitch = RomTables.pitchtable[12] >> 8;

				if (oldParams.isSilent())	/* previous frame silent? */
					decode |= FL_nointerp;
				
				/* reset pitch on voiced->unvoiced transition*/
				ppctr = 0;

			} else {				/* voiced */

				params.pitch = RomTables.pitchtable[params.pitchParam] >> 8;

				if ((decode & FL_unvoiced) != 0)	/* unvoiced before? */
					decode |= FL_nointerp;	/* don't interpolate */
				else
					decode &= ~FL_nointerp;

				decode &= ~FL_unvoiced;
			}

			/* translate energy */
			//env = KTRANS(energytable[env]);
			params.energy = RomTables.energytable[params.energyParam] >> 6;		// 15-bit to 9-bit

			/*  Get K parameters  */

			if (!params.repeat) {			
				/* don't repeat previous frame */

				params.kVal[0] = RomTables.k1table[params.kParam[0]];
				params.kVal[1] = RomTables.k2table[params.kParam[1]];
				params.kVal[2] = RomTables.k3table[params.kParam[2]];
//				params.kVal[3] = RomTables.k4table[params.kParam[3]];
				params.kVal[3] = RomTables.k3table[params.kParam[3]];	// bug in pre-TMS5220, according to MAME... 

				if (!params.isUnvoiced()) {	/* unvoiced? */
					params.kVal[4] = RomTables.k5table[params.kParam[4]];
					params.kVal[5] = RomTables.k6table[params.kParam[5]];
					params.kVal[6] = RomTables.k7table[params.kParam[6]];
					params.kVal[7] = RomTables.k8table[params.kParam[7]];
					params.kVal[8] = RomTables.k9table[params.kParam[8]];
					params.kVal[9] = RomTables.k10table[params.kParam[9]];
				} else {
					params.kVal[4] = 0;
					params.kVal[5] = 0;
					params.kVal[6] = 0;
					params.kVal[7] = 0;
					params.kVal[8] = 0;
					params.kVal[9] = 0;
				}
			} else {
				System.arraycopy(oldParams.kVal, 0, params.kVal, 0, params.kVal.length);
			}
		}

		if (forceUnvoiced) {
			decode |= FL_unvoiced;
			params.kVal[4] = 0;
			params.kVal[5] = 0;
			params.kVal[6] = 0;
			params.kVal[7] = 0;
			params.kVal[8] = 0;
			params.kVal[9] = 0;
		}

		// no longer first frame
		if ((decode & FL_nointerp + FL_first) != 0)
			decode &= ~FL_first;

		Logging.writeLogLine(2, settings.get(ISpeechChip.settingLogSpeech),
				"Equation: " + params);

		Logging.writeLogLine(3, settings.get(ISpeechChip.settingLogSpeech),
				"energy: "+oldParams.energyParam+" => "+params.energyParam+", pitch: "+oldParams.pitchParam+" => "+params.pitchParam);
	}	
	
	/**
	 * One LPC frame consists of calculating a speech waveform and outputting it
	 * for a set of parameters.
	 * 
	 * @param params the parameters to use (copied) 
	 * @param length number of samples
	 * @return true to continue, false if end of frame
	 */
	public synchronized void frame(LPCParameters params, int length)
	{
		//if ((decode & FL_last) != 0) 
		//	return false;
					
		applyEquation(params, false);
		
		calcFrameData(length);
		//return (decode & FL_last) == 0;	/* not last frame */
	}

	
}
