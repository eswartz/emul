/*
  BuiltinEquationFetcher.java

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
package v9t9.engine.speech;

import java.util.Arrays;

import v9t9.common.speech.ILPCParameters;

public class BuiltinEquationFetcher implements ILPCEquationFetcher {
	
	/* (non-Javadoc)
	 * @see v9t9.engine.speech.ILPCEquationFetcher#fetchEquation(v9t9.engine.speech.LPCParamsData)
	 */
	@Override
	public void fetchEquation(ILPCDataFetcher fetcher, ILPCParameters params_) {
		LPCParameters params = (LPCParameters) params_;
		
		//params.repeat = false;
		
		params.energyParam = fetcher.fetch(4);
		if (params.energyParam == 15) {
			/* Last frame */
			Arrays.fill(params.kParam, -1);
		} else if (params.energyParam == 0) {	
			/* Silent frame */ 
			Arrays.fill(params.kParam, -1);
		} else {
			/*  Repeat bit  */
			params.repeat = fetcher.fetch(1) != 0;

			/*  Pitch code  */
			params.pitchParam = fetcher.fetch(6);

			/*  Get K parameters  */
			if (!params.repeat) {			
				/* don't repeat previous frame */

				params.kParam[0] = fetcher.fetch(5);
				params.kParam[1] = fetcher.fetch(5);
				params.kParam[2] = fetcher.fetch(4);
				params.kParam[3] = fetcher.fetch(4);
				
				if (params.pitchParam != 0) {	/* voiced? */
					params.kParam[4] = fetcher.fetch(4);
					params.kParam[5] = fetcher.fetch(4);
					params.kParam[6] = fetcher.fetch(4);
					params.kParam[7] = fetcher.fetch(3);
					params.kParam[8] = fetcher.fetch(3);
					params.kParam[9] = fetcher.fetch(3);
				} else {
					params.kParam[4] = -1;
					params.kParam[5] = -1;
					params.kParam[6] = -1;
					params.kParam[7] = -1;
					params.kParam[8] = -1;
					params.kParam[9] = -1;
				}
			}
		}
	}

}