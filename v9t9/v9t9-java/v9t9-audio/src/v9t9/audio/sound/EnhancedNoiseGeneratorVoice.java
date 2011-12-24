/**
 * 
 */
package v9t9.audio.sound;

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
	

	public void setOperationPeriod(int period) {
		this.period = (short) (period & 0x7ff);
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
