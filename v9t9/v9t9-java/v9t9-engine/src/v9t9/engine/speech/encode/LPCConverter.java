/*
  LPCConverter.java

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

import v9t9.common.speech.ILPCParameters;
import v9t9.engine.speech.LPCParameters;
import v9t9.engine.speech.RomTables;

/**
 * @author ejs
 *
 */
public class LPCConverter {

	private int hertz;
	private int origHz;
	
	public LPCConverter(int origHz, int hertz) {
		this.origHz = origHz;
		this.hertz = hertz;
		
	}
	public ILPCParameters apply(LPCAnalysisFrame results) {
		boolean voiced = results.invPitch != 0;
		
		LPCParameters params = new LPCParameters();
		if (voiced) {
			int pVal = (int) Math.max(0x1000, Math.min(0xA000, ((float)results.invPitch*hertz/origHz*256)));
			params.pitchParam = lookup(RomTables.pitchtable, pVal);
			params.pitch = RomTables.pitchtable[params.pitchParam];
		} else {
			params.pitch = 0;
			params.pitchParam = 0;
		}
		
		int eVal = (int) (Math.min(0x7fc0, (int) (results.power * results.powerScale * 0x7fbf)));
		params.energyParam = Math.min(14, lookup(RomTables.energytable, eVal));
		params.energy = RomTables.energytable[params.energyParam];
		
		int max = Math.min(results.coefs.length, voiced ? 10 : 4);
		for (int k = 0; k < max; k++) {
			int kVal = (int) (Math.max(-1.0f, Math.min(1.0f, results.coefs[k + results.coefsOffs])) * 32767);
			params.kParam[k] = lookup(RomTables.ktable[k], kVal);
			params.kVal[k] = RomTables.ktable[k][params.kParam[k]];
		}
		for (int k = max; k < 10; k++) {
			params.kParam[k] = 0;
			params.kVal[k] = 0;
		}
		
		return params;
	}

	private int lookup(short[] ks, int fk) {
		int diff = Integer.MAX_VALUE;
		int matchIdx = 0;
		int idx = 0;
		for (short k : ks) {
			int curdiff = Math.abs(k - fk);
			if (curdiff < diff) {
				matchIdx = idx;
				diff = curdiff;
			}
			idx++;
		}
		return matchIdx;
	}
	
	private int lookup(int[] ks, int fk) {
		int diff = Integer.MAX_VALUE;
		int matchIdx = 0;
		int idx = 0;
		for (int k : ks) {
			int curdiff = Math.abs(k - fk);
			if (curdiff < diff) {
				matchIdx = idx;
				diff = curdiff;
			}
			idx++;
		}
		return matchIdx;
	}
}
