/*
  LPCSpeech.java

  (c) 2009-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.speech;

import java.util.Arrays;

import ejs.base.properties.IProperty;
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
	/** speech flags (mask of FL_xxx) */
	int		decode;			
	/** unvoiced? */
final static int FL_unvoiced = 1;		
/** no interpolation */
final static int FL_nointerp = 2;		
/** first frame? */
final static int FL_first	= 4;		
/** stop frame seen */
final static int FL_last	= 8;		

	/** middle range of pitch (average) */
	int 	midRange;		
	/** which equation (0..n) is being handled */
	int		voicedEquationNumber;

	/** lattice filter */
	int		b[],y[];		
	/** unvoiced hiss registers */
	int		ns1,ns2;		
	/** pitch counter */
	int		ppctr;			

	private final ISettingsHandler settings;

	private ListenerList<ISpeechDataSender> senderList;
	private ListenerList<ILPCParametersListener> paramListeners;
	private IProperty generateSpeech;
	
	private IProperty pitchAdjust;
	private IProperty pitchRangeAdjust;
	private IProperty pitchMidRangeAdjustRate;
	private IProperty forceUnvoiced;
	
	public LPCSpeech(ISettingsHandler settings) {
		this.settings = settings;
	
		generateSpeech = settings.get(ISpeechChip.settingGenerateSpeech);
		
		pitchAdjust = settings.get(ISpeechChip.settingPitchAdjust);
		pitchRangeAdjust = settings.get(ISpeechChip.settingPitchRangeAdjust);
		pitchMidRangeAdjustRate = settings.get(ISpeechChip.settingPitchMidRangeAdjustRate);
		forceUnvoiced = settings.get(ISpeechChip.settingForceUnvoiced);

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
		voicedEquationNumber = 0;
		midRange = 0;
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
		params.energyParam = 0;
		params.energy = 0;
		Arrays.fill(params.kVal, 0);
		voicedEquationNumber = 0;
	}
	
	/**
	Interpolate "new" values and "buffer" values.
	*/
	private void interpolate(int period)
	{
		int         x;
	
		if (0 == (decode & FL_nointerp)) {
			oldParams.energy += (params.energy - oldParams.energy) / (RomTables.interp_coeff[period]);
			if (oldParams.pitch != 0)
				oldParams.pitch += (params.pitch - oldParams.pitch) / (RomTables.interp_coeff[period]);
			for (x = 0; x < 10; x++)
				oldParams.kVal[x] += (params.kVal[x] - oldParams.kVal[x]) / (RomTables.interp_coeff[period]);
		}
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

		boolean send = generateSpeech.getBoolean() && senderList != null && !senderList.isEmpty();
		Object[] senders = senderList.toArray();
		
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
				int ppidx = ppctr;
				U = ppidx < RomTables.chirptable.length ? (RomTables.chirptable[ppidx] & 0xff) * oldParams.energy / 256 : 0;

				if (oldParams.pitch != 0) 
					ppctr = (ppctr + 1) % (oldParams.pitch);
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
			
			if (send) {
				for (Object o : senders) {
					((ISpeechDataSender) o).sendSample(LPC_TO_PCM(samp), pos, length);
				}
			}
			
			pos++;
		}
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
			voicedEquationNumber = 0;
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
				params.pitch = 12;		/* set some pitch */

				if (oldParams.isSilent())	/* previous frame silent? */
					decode |= FL_nointerp;
				
				/* reset pitch on voiced->unvoiced transition*/
				ppctr = 0;

			} else {				/* voiced */

				params.pitch = getRangeAdjustedPitch(params.pitchParam);
				
				if ((decode & FL_unvoiced) != 0)	/* unvoiced before? */
					decode |= FL_nointerp;	/* don't interpolate */
				else
					decode &= ~FL_nointerp;

				decode &= ~FL_unvoiced;
				voicedEquationNumber++;

			}

			/* translate energy */
			params.energy = RomTables.energytable[params.energyParam] >> 6;		// 15-bit to 9-bit

			/*  Get K parameters  */

			if (!params.repeat) {			
				/* don't repeat previous frame */

				params.kVal[0] = getNumber(RomTables.k1table[params.kParam[0]]);
				params.kVal[1] = getNumber(RomTables.k2table[params.kParam[1]]);
				params.kVal[2] = getNumber(RomTables.k3table[params.kParam[2]]);
				
				// bug in pre-TMS5220, according to MAME... (helps with "BULLETIN")
//				params.kVal[3] = getNumber(RomTables.k4table[params.kParam[3]]);
				params.kVal[3] = getNumber(RomTables.k3table[params.kParam[3]]);	 

				if (!params.isUnvoiced()) {	/* unvoiced? */
					params.kVal[4] = getNumber(RomTables.k5table[params.kParam[4]]);
					params.kVal[5] = getNumber(RomTables.k6table[params.kParam[5]]);
					params.kVal[6] = getNumber(RomTables.k7table[params.kParam[6]]);
					params.kVal[7] = getNumber(RomTables.k8table[params.kParam[7]]);
					params.kVal[8] = getNumber(RomTables.k9table[params.kParam[8]]);
					params.kVal[9] = getNumber(RomTables.k10table[params.kParam[9]]);
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

		Logging.writeLogLine(0, settings.get(ISpeechChip.settingLogSpeech),
				"Equation: " + params);

		Logging.writeLogLine(4, settings.get(ISpeechChip.settingLogSpeech),
				"energy: "+oldParams.energyParam+" => "+params.energyParam+", pitch: "+oldParams.pitchParam+" => "+params.pitchParam);
	}	
	
	private short getNumber(short s) {
		return s;
	}

	/**
	 * @param pitchVal
	 * @return
	 */
	private int getRangeAdjustedPitch(int pitchParam) {
		int midRangeAdjustMax = pitchMidRangeAdjustRate.getInt();
		
		int normVal = (RomTables.pitchtable[pitchParam] >> 8) & 0xff;

		if (midRangeAdjustMax == -1)
			normVal = 43;
		
		normVal /= pitchAdjust.getDouble();
		
		double rangeAdjust = pitchRangeAdjust.getDouble();
		
		if (voicedEquationNumber == 0 || midRangeAdjustMax <= 1) {
			midRange = normVal;
		} else if (voicedEquationNumber < midRangeAdjustMax) {
			midRange = (midRange * (midRangeAdjustMax - 1) + normVal) / midRangeAdjustMax;
		}
		
		int adjustedPitch = (int) (midRange + (normVal - midRange) * rangeAdjust);
		
		
		int pitch = Math.max(0, Math.min(adjustedPitch, 0xc0));
		
		return pitch;
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
				
		
		if ((decode & FL_first) != 0) {
			midRange = 0;
			voicedEquationNumber = 0;
		}
	
		applyEquation(params, forceUnvoiced.getBoolean());
		
		calcFrameData(length);
		//return (decode & FL_last) == 0;	/* not last frame */
	}

	
}
