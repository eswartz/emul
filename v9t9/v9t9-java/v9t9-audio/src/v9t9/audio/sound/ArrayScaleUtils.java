/*
  ArrayScaleUtils.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.audio.sound;

import ejs.base.sound.IArrayAccess;
import ejs.base.sound.IWriteArrayAccess;

/**
 * @author ejs
 *
 */
public class ArrayScaleUtils {

	/**
	 * @param iSoundView
	 * @param view
	 * @param ch 
	 * @param chStep 
	 * @return
	 */
	public static void slowDown(IArrayAccess input, IWriteArrayAccess view, int ch, int chStep,
			int viewLength, float perc) {
		
	
		// step: rate through 'input' to move per sample of 'view' (less than 1)
		long step = (long) (65536 * perc);
		
		int stepFrac = (int) (step & 65535);
		
		int oldIdx = 0;
		int oldFrac = 0;
		
		int newIdx = 0;
		
		int scnt = viewLength;
		float prevSamp = input.at(oldIdx + ch);
		float curSamp = input.at(oldIdx + ch);
		
		while (newIdx < scnt) {
			float samp = ((curSamp * oldFrac) + (prevSamp * (65536 - oldFrac))) / 65536f;
			view.set(newIdx + ch, samp);
			
			newIdx += chStep;
			
			oldFrac += stepFrac;
			if (oldFrac >= 65536) {
				oldFrac -= 65536;
				oldIdx += chStep;
				
				prevSamp = curSamp;
				curSamp = input.at(oldIdx + ch);
			}
		}
	}

	/**
	 * @param iSoundView
	 * @param view
	 * @param chStep 
	 * @param ch 
	 * @return
	 */
	public static void speedUp(IArrayAccess input, IWriteArrayAccess view, int ch, int chStep,
			int inputLength, float scale) {
	
	
		// step: rate through 'view' as samples in 'input' are combined (less than 1)
		long step = (long) (scale * 65536);
		int stepFrac = (int) (step & 65535);
		
		int newIdx = 0;
		int newFrac = 0;
		
		int oldIdx = 0;
		
		float samp = 0f;
		int avgCount = 0;
	
		float prevSamp = 0f;
		float curSamp = input.at(oldIdx + ch);
		
		int scnt = inputLength;
		while (oldIdx < scnt) {
			samp += ((prevSamp * newFrac) + (curSamp * (65536 - newFrac))) / 65536;
			avgCount++;
			
			oldIdx += chStep;
			
			prevSamp = curSamp;
			curSamp = input.at(oldIdx + ch);
			
			newFrac += stepFrac;
			if (newFrac >= 65536 || oldIdx >= scnt) {
				if (newIdx + ch < view.size())
					view.set(newIdx + ch, samp / avgCount);
				avgCount = 0;
				samp = 0f;
				
				newFrac -= 65536;
				newIdx += chStep;
			}
		}
	}

	/**
	 * @param iSoundView
	 * @param view
	 * @param ch 
	 * @param chStep 
	 * @return
	 */
	public static void rotate(IArrayAccess input, IWriteArrayAccess view, int ch, int chStep,
			int viewLength, float perc) {
		
		int newIdx = 0;
		
		int scnt = viewLength;
		
		while (newIdx < scnt) {
			float samp = input.at(newIdx + ch) + (float)(perc * 2 * Math.PI * newIdx / scnt);
			view.set(newIdx + ch, samp);
			
			newIdx += chStep;
		}
	}

}
