/*
  TI99SoundSmoother.java

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
package v9t9.gui.sound;

import java.util.List;

import ejs.base.sound.IEditableSoundView;
import ejs.base.sound.ISoundMutator;
import ejs.base.sound.ISoundView;
import ejs.base.sound.SoundChunk;

/**
 * @author ejs
 *
 */
public class TI99SoundSmoother implements ISoundMutator {

	private float[] last = new float[2];
	
	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundMutator#editSoundChunk(ejs.base.sound.ISoundView, java.util.List)
	 */
	@Override
	public void editSoundChunk(ISoundView chunk, List<ISoundView> outViews) {
		
		ISoundView outView = chunk;
		if (!chunk.isSilent()) {
			int numChans = chunk.getFormat().getChannels();
			int processedChans = Math.min(last.length, numChans);
			
			int length = chunk.getSampleCount();
			
			IEditableSoundView outChunk = null;
			if (chunk instanceof IEditableSoundView) {
				outChunk = (IEditableSoundView) chunk;
			} else {
				outChunk = new SoundChunk(new float[length], chunk.getFormat());
			}
			
			// watch out for errors from bad sounds
			for (int c = 0; c < processedChans; c++) {
				if (Float.isNaN(last[c]))
					last[c] = 0f;
			}
			
			for (int idx = 0; idx < length; idx += numChans) {
				for (int c = 0; c < processedChans; c++) {
					float v = chunk.at(idx + c);

					v = (1f * v - 0.5f * last[c]);
					last[c] = v;
					
					outChunk.set(idx + c, v);
				}
			}
			
			outView = outChunk;
		}
		outViews.add(outView);
	}
}
