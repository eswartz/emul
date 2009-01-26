/**
 * 
 */
package v9t9.emulator.hardware.sound;

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

	protected int getOperationPeriod() {
		return ( (operation[SoundTMS9919.OPERATION_FREQUENCY_LO] & 0xf) |
		( (operation[SoundTMS9919.OPERATION_FREQUENCY_HI] & 0x7f) << 4 ) );
	}
	
	public EffectsController getEffectsController() {
		return effectsController;
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
