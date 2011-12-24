/**
 * 
 */
package v9t9.engine.sound;

import v9t9.common.machine.IMachine;

import static v9t9.common.sound.TMS9919BConsts.*;

/**
 * Controller for the TMS9919(B) sound chip.
 * </pre>
 * @author ejs
 *
 */
public class SoundTMS9919B extends SoundTMS9919 {

	/** Control, Audio Gate, Effect Control, Effect Value */
	private static final int REG_COUNT = 4;

	static {
		register(regNames, regDescs, regIds, 
				2, "FX", "Effects");
		
		register(regNames, regDescs, regIds, 
				3, 
				"FXVal",
				"Effects Value");
	}
	
	protected int lastCommand;

	private int cmdVoice;
	
	public SoundTMS9919B(IMachine machine, String name, int regBase) {
		super(machine, name, REG_COUNT, regBase);
	}
	
	protected void init(String name) {
		for (int i = 0; i < 3; i++) {
			sound_voices[i] = new EnhancedToneGeneratorVoice(name, i);
		}
		sound_voices[VOICE_NOISE] = new EnhancedNoiseGeneratorVoice(name, (ClockedSoundVoice) sound_voices[VOICE_TONE_2]);
		sound_voices[VOICE_AUDIO] = new AudioGateVoice(name);
		
		lastCommand = 0;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.SoundHandler#writeSound(byte)
	 */
	public void writeSound(int addr, byte val) {
		//System.out.println("Writing " + Utils.toHex2(addr & 0x6) + " := " + Utils.toHex2(val));
		if ((addr & 0x6) == 0x2) {
			setRegister(regBase + 2, val);
			
			// command byte
			if ((val & 0x80) != 0) {
				cmdVoice = getOperationVoice(val);
				if (!(sound_voices[cmdVoice] instanceof EnhancedVoice))
					return;
				EnhancedVoice voice = (EnhancedVoice) sound_voices[cmdVoice];
				
				lastCommand = (val & 0xf);
				switch (lastCommand) {
				case CMD_RESET:
					voice.getEffectsController().reset();
					break;
				case CMD_RELEASE:
					voice.getEffectsController().startRelease();
					break;
				default:
					// others take an argument next time
				}
			}
		} else if ((addr & 0x6) == 0x4) {
			setRegister(regBase + 3, val);
			
			// data 
			if (!(sound_voices[cmdVoice] instanceof EnhancedVoice))
				return;
			EnhancedVoice voice = (EnhancedVoice) sound_voices[cmdVoice];
			switch (lastCommand) {
			case CMD_ENVELOPE:
				voice.getEffectsController().setSustain(val & 0xf);
				break;
			case CMD_ENV_ATTACK_DECAY:
				voice.getEffectsController().setADSR(
						EffectsController.OP_ATTACK, (val >> 4) & 0xf);
				voice.getEffectsController().setADSR(
						EffectsController.OP_DECAY, val & 0xf);
				break;
			case CMD_ENV_HOLD_RELEASE:
				voice.getEffectsController().setADSR(
						EffectsController.OP_HOLD, (val >> 4) & 0xf);
				voice.getEffectsController().setADSR(
						EffectsController.OP_RELEASE, val & 0xf);
				break;
			case CMD_VIBRATO:
				voice.getEffectsController().setVibrato(
						(val >> 4) & 0xf, val & 0xf);
				break;
			case CMD_TREMOLO:
				voice.getEffectsController().setTremolo(
						(val >> 4) & 0xf, val & 0xf);
				break;
			case CMD_WAVEFORM:
				voice.getEffectsController().setWaveform(val & 0xf);
				break;
			case CMD_SWEEP_PROPORTION: {
				int target = ((ClockedSoundVoice) sound_voices[cmdVoice]).getClock();
				if (val > 0)
					target += (target * val / 127);
				else
					target -= (target * -val / 128);
				voice.getEffectsController().setSweepTarget(target);
				break;
			}
			case CMD_SWEEP_TIME: {
				int clocks = (val & 0xff) * ((ClockedSoundVoice)voice).soundClock * 64 / 255;
				voice.getEffectsController().setSweepTime(clocks);
				break;
			}
			case CMD_BALANCE:
				((SoundVoice)voice).setBalance(val);
				break;
			}
		} else {
			super.writeSound(addr, val);
		}
	}
	
	@Override
	protected void updateVoice(ClockedSoundVoice v, byte val) {
		super.updateVoice(v, val);
		if (sound_voices[cmdVoice] instanceof EnhancedVoice && (val & 0x80) == 0x80) { 
			EnhancedVoice voice = (EnhancedVoice) sound_voices[cmdVoice];
			byte vol = ((ClockedSoundVoice) voice).getVolume();
			if (vol == 0 || (val & 0x90) == 0x80)
				voice.getEffectsController().stopEnvelope();
			else
				voice.getEffectsController().updateVoice();
		}
	}
	
	@Override
	protected void updateNoiseVoice(ClockedSoundVoice v) {
		super.updateNoiseVoice(v);
		
	}
	
	@Override
	public void tick() {
		super.tick();
	}


	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getGroupName()
	 */
	@Override
	public String getGroupName() {
		return "TMS 9919B";
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.sound.SoundTMS9919#getRegisterCount()
	 */
	@Override
	public int getRegisterCount() {
		return REG_COUNT;
	}
}
