/*
  EnhancedToneGeneratorVoice.java

  (c) 2009-2011 Edward Swartz

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
package v9t9.audio.sound;

/**
 * Tone generator 
 * @author ejs
 *
 */
public class EnhancedToneGeneratorVoice extends ToneGeneratorVoice implements EnhancedVoice {

	private EffectsController effectsController;

	public EnhancedToneGeneratorVoice(String name, int number) {
		super(name, number);
		effectsController = new EffectsController(this);
	}

	public EffectsController getEffectsController() {
		return effectsController;
	}

	@Override
	protected boolean updateAccumulator() {
		return effectsController.updateDivisor();
	}
	@Override
	public float getCurrentMagnitude() {
		return (float) effectsController.getCurrentSample() / 0x007FFFFF;
	}
	@Override
	protected void updateEffect() {
		effectsController.updateEffect();
	}
	@Override
	public boolean isActive() {
		return super.isActive() || effectsController.isActive();
	}
	

	/* (non-Javadoc)
	 * @see v9t9.audio.sound.ClockedSoundVoice#setPeriod(int)
	 */
	@Override
	public void setPeriod(int period) {
		super.setPeriod(period);
		effectsController.updateFrequency();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.audio.sound.SoundVoice#setVolume(int)
	 */
	@Override
	public void setVolume(int volume) {
		super.setVolume(volume);
		if (volume == 0)
			getEffectsController().stopEnvelope();
		else
			getEffectsController().updateVoice();

	}
	
}
