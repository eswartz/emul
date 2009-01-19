/**
 * 
 */
package v9t9.emulator.hardware.sound;

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
		return ( (operation[SoundTMS9919.OPERATION_FREQUENCY_LO] & 0xf) |
		( (operation[SoundTMS9919.OPERATION_FREQUENCY_HI] & mask) << 4 ) );
	}
	
	@Override
	protected void updateAccumulator() {
		effectsController.updateDivisor();
	}
	@Override
	public int getCurrentMagnitude() {
		return effectsController.getCurrentSample();
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
