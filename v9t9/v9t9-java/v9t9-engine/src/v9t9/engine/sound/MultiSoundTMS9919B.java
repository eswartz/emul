/**
 * 
 */
package v9t9.engine.sound;


import ejs.base.settings.ISettingSection;
import ejs.base.sound.ISoundVoice;
import v9t9.common.client.ISoundHandler;
import v9t9.common.hardware.ISoundChip;
import v9t9.common.machine.IMachine;

/**
 * Multiple packed TMS9919 chips.  This provides four TMS9919Bs, 
 * each with four addresses, after the traditional TMS9919 sound chip.
 * <p>
 * BASE = console chip
 * BASE + 2 = #1 std
 * BASE + 4 = #1 effects command
 * BASE + 6 = #1 effects data
 * BASE + 8 = #2 std
 * BASE + 10 = #2 effects command
 * BASE + 12 = #2 effects data
 * BASE + 14 = #3 std
 * BASE + 16 = #3 effects command
 * BASE + 18 = #3 effects data
 * BASE + 20 = #4 std
 * BASE + 22 = #4 effects
 * BASE + 24 = #4 effects data
 * @author ejs
 *
 */
public class MultiSoundTMS9919B implements ISoundChip {

	private SoundTMS9919[] chips;
	private ISoundVoice[] voices;
	private ISoundHandler soundHandler;
	private AudioGateVoice audioGateVoice;
	private final IMachine machine;
	
	public MultiSoundTMS9919B(IMachine machine) {
		// 5 chips: the original TMS9919 on the console and 4 extra on the card
		
		this.machine = machine;
		this.chips = new SoundTMS9919[5];
		chips[0] = new SoundTMS9919(machine, "Console Chip");
		//chips[0] = new SoundTMS9919B(machine, "Console Chip");
		for (int i = 0; i < 4; i++) {
			chips[i + 1] = new SoundTMS9919B(machine, "Chip #" + i);
			
			/*byte balance = (byte) ((i & 1) == 0 ? -128 : 127); 
			for (SoundVoice voice : chips[i + 1].getSoundVoices()) {
				voice.setBalance(balance);
			}*/
		}
		voices = null;
	}

	public synchronized ISoundVoice[] getSoundVoices() {
		if (voices == null) {
			voices = new SoundVoice[chips.length * 4 + 1];
			int idx = 0;
			for (int i = 0; i < chips.length; i++) {
				SoundTMS9919 chip = chips[i];
				ISoundVoice[] chipVoices = chip.getSoundVoices();
				System.arraycopy(chipVoices, 0, voices, idx, 4);
				idx += 4;
			}
			voices[idx++] = audioGateVoice = new AudioGateVoice(null);
		}
		return voices;
	}
	
	public void writeSound(int addr, byte val) {
		if ((addr & 0xff) < 0xC0) {
			int mask = addr & 0x1e;
			
			if (mask == 0) {
				// Main chip.
				chips[0].writeSound(addr, val);
				return;
			}
			
			int chip = 1 + ((mask - 2) / 6);
			int offs = mask + 6 - 2 - (chip * 6);
			
			if (chip < chips.length) {
				chips[chip].writeSound(offs, val);
			
				if (soundHandler != null)
					soundHandler.generateSound();
			}
		}
	}
	
	public void setAudioGate(int addr, boolean b) {
		if (audioGateVoice != null) {
			audioGateVoice.setState(machine, b);
			audioGateVoice.setupVoice();
			if (soundHandler != null)
				soundHandler.generateSound();
		}
	}

	public ISoundHandler getSoundHandler() {
		return soundHandler;
	}
	
	public void setSoundHandler(ISoundHandler soundHandler) {
		this.soundHandler = soundHandler;
		for (SoundTMS9919 chip : chips) {
			chip.setSoundHandler(null);
		}
	}

	public void loadState(ISettingSection section) {
		if (section == null)
			return;
		int idx = 0;
		for (SoundTMS9919 chip : chips) {
			chip.loadState(section.getSection("" + idx));
			idx++;
		}
	}
	
	public void saveState(ISettingSection section) {
		int idx = 0;
		for (SoundTMS9919 chip : chips) {
			chip.saveState(section.addSection("" + idx));
			idx++;
		}
	}
	
	public boolean isStereo() {
		return true;
	}
	
	public void tick() {
		for (SoundTMS9919 chip : chips) {
			chip.tick();
		}
		if (soundHandler != null) {
			soundHandler.flushAudio();
		}
	}
}
