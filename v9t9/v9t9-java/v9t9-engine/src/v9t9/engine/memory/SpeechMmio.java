/*
  SpeechMmio.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.memory;

import v9t9.common.hardware.ISpeechChip;
import v9t9.common.memory.IMemoryDomain;
import v9t9.engine.speech.BaseLpcDataFetcher;
import v9t9.engine.speech.SpeechTMS5220;
import ejs.base.utils.Check;

/** 
 * Speech chip
 * @author ejs
 */
public class SpeechMmio implements IConsoleMmioWriter, IConsoleMmioReader {


	private final ISpeechChip speech;
	private byte spchDrHi;

	public SpeechMmio(final ISpeechChip speech) {
		Check.checkArg(speech);
		this.speech = speech;
    }

    public byte read(int addrMask) {
    	spchDrHi = 0;
    	return speech.read();
    }
    
    /**
     * @see v9t9.common.memory.Memory.IConsoleMmioWriter#write 
     */
    public void write(int addr, byte val) {
    	spchDrHi = 0;
        speech.write(val);
    }

	public ISpeechChip getSpeech() {
		return speech;
	}

	/**
	 * @param addr
	 * @param val
	 */
	public void writeDirect(final IMemoryDomain domain, int addr, byte val) {
		// direct equation, writing equation
		if ((addr & 1) == 0) {
			spchDrHi = val;
		} else {
			final int caddr_ = ((spchDrHi << 8) & 0xff00) | (val & 0xff); 
			final SpeechTMS5220 speech = (SpeechTMS5220) this.speech;
			
//			final LPCParameters params = new LPCParameters();
//			
//			// up to 13 bytes
//			params.energyParam = domain.readByte(caddr++) & 0xf;
//			if (params.energyParam != 0 && params.energyParam != 0xf) {
//				params.repeat = domain.readByte(caddr++) != 0;
//				params.pitchParam = domain.readByte(caddr++) & 0x3f;
//				if (!params.repeat) {
//					params.kParam[0] = domain.readByte(caddr++) & 0x1f;
//					params.kParam[1] = domain.readByte(caddr++) & 0x1f;
//					params.kParam[2] = domain.readByte(caddr++) & 0xf;
//					params.kParam[3] = domain.readByte(caddr++) & 0xf;
//					if (params.pitchParam != 0) {
//						params.kParam[4] = domain.readByte(caddr++) & 0xf;
//						params.kParam[5] = domain.readByte(caddr++) & 0xf;
//						params.kParam[6] = domain.readByte(caddr++) & 0xf;
//						params.kParam[7] = domain.readByte(caddr++) & 0x7;
//						params.kParam[8] = domain.readByte(caddr++) & 0x7;
//						params.kParam[9] = domain.readByte(caddr++) & 0x7;
//					}
//				}
//			}
			
			//speech.getLpcSpeech().frame(params, speech.getSamplesPerFrame());
			speech.setUserFetcher(new BaseLpcDataFetcher() {
				int caddr = caddr_;

				
				@Override
				public int fetch(int bits) {
					byte byt = domain.readByte(caddr++); 
					int mask = ~(~0 << bits);
					return (byt & mask);
				}
				
				@Override
				public boolean isDone() {
					return true;
				}
			});
		}
		
	}
}
