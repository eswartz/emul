/**
 * 
 */
package v9t9.engine.sound;

import static v9t9.common.sound.TMS9919Consts.*;

/**
 * TMS9919(B) noise generator.
 * @author ejs
 *
 */
public class EnhancedNoiseGeneratorVoice extends NoiseGeneratorVoice implements EnhancedVoice {

	private EffectsController effectsController;

	public EnhancedNoiseGeneratorVoice(String name,
			ClockedSoundVoice pairedVoice2) {
		super(name, pairedVoice2);
		effectsController = new EffectsController(this);
	}

	public EffectsController getEffectsController() {
		return effectsController;
	}
	
	protected int getOperationPeriod() {
		byte mask = 0x7f;
		return ( (operation[OPERATION_FREQUENCY_LO] & 0xf) |
		( (operation[OPERATION_FREQUENCY_HI] & mask) << 4 ) );
	}
	
	@Override
	protected void updateAccumulator() {
		effectsController.updateDivisor();
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
}
