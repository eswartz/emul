/**
 * 
 */
package v9t9.emulator.hardware.sound;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.IDialogSettings;

import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.SoundProvider;
import v9t9.engine.SoundHandler;
import v9t9.utils.Utils;

/**
 * Controller for the TMS9919(B) sound chip.
 * <p>
 * This is an invented successor to the TMS9919 which supports additional
 * effects.
 * <p>
 * First, the minimum frequency allowed is 54 Hz, by making use of an extra
 * bit in the "hi frequency" command.
 * <p>
 * Secondly, the sound chip uses two address ports.  The first acts as it
 * did in the TMS9919.  The second allows setting envelopes and effects.
 <pre>
   General pattern:  
   
		7    6    5    4    3    2    1    0
		1    <v  #>    1    <.  command #  .>		
		1    <v  #>    0    <.  argument   .>		
		
   Commands:
   
   		0000 = reset all effects on voice
   		0001 = enable/disable envelope (argument: sustain ratio)
   		0010 = set envelope attack  (argument: attack time)
   		0011 = set envelope decay   (argument: decay time)
   		0100 = set envelope hold    (argument: hold time)
   		0101 = set envelope release (argument: release time)
   		
   		1111 = indicate note release (argument ignored)
		
		The enable envelope (1) command acts as both the enablement of envelopes 
		and the setting for the sustain level of the ADSR envelope.  A sustain
		ratio of 0 is meaningless (a hold of 0 can have the same effect). 
		
		The envelope can act as long as the voice's volume is not "off" (15).
		The attack, decay, and hold portions operate starting from the
		point where a voice's volume is set (usually, from the off to on 
		states, though modifying a playing voice will also reset the envelope).  
		Once the "note release" command is issued, the release portion of 
		the envelope takes over.   When the voice is turned off (volume 0, 
		atten 0xF), all output ceases.
		
		The sustain ratio tells how much of the voice's volume is used
		during the "hold" portion of a note.  It's taken as N/16 times the
		full volume of the channel.
		
		When set to 0, the envelope is disabled for the voice (every note
		has the full volume).
		
		attack, decay, hold, and release times are given by the table in
		{@link EffectsController#tickTimes}.

 * </pre>
 * @author ejs
 *
 */
public class SoundTMS9919B extends SoundTMS9919 {

	protected final int 
		CMD_RESET = 0,
		CMD_ENVELOPE = 1,
		CMD_ENV_ATTACK = 2,
		CMD_ENV_DECAY = 3,
		CMD_ENV_HOLD = 4,
		CMD_ENV_RELEASE = 5,
		CMD_RELEASE = 15;
	
	protected int lastCommand;
	
	public SoundTMS9919B(Machine machine, String name) {
		super(machine, name);
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
		System.out.println("Writing " + Utils.toHex2(addr & 0x2) + " := " + Utils.toHex2(val));
		if ((addr & 2) != 0) {
			// command byte
			if ((val & 0x80) != 0) {
				cvoice = getOperationVoice(val);
				EnhancedVoice voice = (EnhancedVoice) sound_voices[cvoice];
				
				if ((val & 0x10) != 0) {
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
				} else {
					switch (lastCommand) {
					case CMD_RESET:
						// in case we get here too
						voice.getEffectsController().reset();
						break;
					case CMD_ENVELOPE:
						voice.getEffectsController().setSustain(val & 0xf);
						break;
					case CMD_ENV_ATTACK:
						voice.getEffectsController().setADSR(EffectsController.OP_ATTACK, val & 0xf);
						break;
					case CMD_ENV_DECAY:
						voice.getEffectsController().setADSR(EffectsController.OP_DECAY, val & 0xf);
						break;
					case CMD_ENV_HOLD:
						voice.getEffectsController().setADSR(EffectsController.OP_HOLD, val & 0xf);
						break;
					case CMD_ENV_RELEASE:
						voice.getEffectsController().setADSR(EffectsController.OP_RELEASE, val & 0xf);
						break;
					}

				}
				
			}
		} else {
			super.writeSound(addr, val);
		}
	}
	
	@Override
	protected void updateVoice(ClockedSoundVoice v, byte val) {
		super.updateVoice(v, val);
		if ((val & 0x90) == 0x90) { 
			EnhancedVoice voice = (EnhancedVoice) sound_voices[cvoice];
			byte vol = ((ClockedSoundVoice) voice).getVolume();
			if (vol == 0)
				voice.getEffectsController().stopEnvelope();
			else
				voice.getEffectsController().startEnvelope();
		}
	}
	
	@Override
	public void tick() {
		super.tick();
		
		for (int idx = VOICE_TONE_0; idx <= VOICE_NOISE; idx++) {
			((EnhancedVoice) sound_voices[idx]).getEffectsController().tick();
		}
	}
	
}
