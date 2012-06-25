/**
 * 
 */
package v9t9.gui.sound;

import java.util.List;

import ejs.base.sound.ISoundMutator;
import ejs.base.sound.SoundChunk;

/**
 * @author ejs
 *
 */
public class TI99SoundSmoother implements ISoundMutator {

	private float[] last = new float[2];
	
	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundMutator#editSoundChunk(ejs.base.sound.SoundChunk, java.util.List)
	 */
	@Override
	public void editSoundChunk(SoundChunk chunk, List<SoundChunk> outChunks) {
		
		if (!chunk.isSilent()) {
			int numChans = chunk.getFormat().getChannels();
			int processedChans = Math.min(last.length, numChans);
			
			int length = chunk.getLength();
			for (int idx = 0; idx < length; idx += numChans) {
				for (int c = 0; c < processedChans; c++) {
					float v = chunk.at(idx + c);

					v = (1f * v - 0.5f * last[c]);
					last[c] = v;
					
					chunk.set(idx + c, v);
				}
			}
		}
		outChunks.add(chunk);
	}
}
