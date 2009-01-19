/**
 * 
 */
package v9t9.emulator.hardware.sound;

import org.eclipse.jface.dialogs.IDialogSettings;

import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.SoundProvider;
import v9t9.emulator.runtime.Cpu;
import v9t9.engine.SoundHandler;

/**
 * Multiple packed TMS9919 chips.  This provides a TMS9919 at any number
 * of addresses.  It is based on the description of the FORTI sound chips at
 * <http://nouspikel.group.shef.ac.uk//ti99/forti.htm>
 * @author ejs
 *
 */
public class MultiSoundTMS9919 implements SoundProvider {

	private SoundTMS9919[] chips;
	private SoundVoice[] voices;
	private SoundHandler soundHandler;
	private final Machine machine;
	
	public MultiSoundTMS9919(Machine machine) {
		// 5 chips: the original TMS9919 on the console and 4 extra on the card
		
		this.machine = machine;
		// Prolly in the card, the console chip is ignored, because it
		// borks the intended use of stereo on the other chips.
		// So we ignore it.
		this.chips = new SoundTMS9919[5];
		for (int i = 0; i < 5; i++) {
			chips[i] = new SoundTMS9919(machine, "Chip #" + i);
			
			byte balance = (byte) (i == 0 ? 0 : ((i & 1) != 0 ? -128 : 128)); 
			for (SoundVoice voice : chips[i].getSoundVoices()) {
				voice.setBalance(balance);
			}
		}
		voices = null;
	}

	public synchronized SoundVoice[] getSoundVoices() {
		if (voices == null) {
			voices = new SoundVoice[4 * 4 + 1];
			int idx = 0;
			for (int i = 0; i < 4; i++) {
				SoundTMS9919 chip = chips[i + 1];
				SoundVoice[] chipVoices = chip.getSoundVoices();
				System.arraycopy(chipVoices, 0, voices, idx, 4);
				idx += 4;
			}
			voices[idx++] = chips[0].getSoundVoices()[SoundTMS9919.VOICE_AUDIO];
		}
		return voices;
	}
	
	public void writeSound(int addr, byte val) {
		if ((addr & 0xff) < 0xC0) {
			int mask = addr & 0x1e;
			
			// Ignore main chip.  It only provides audio gate -- if that.
			//chips[0].writeSound(addr, val);
			
			int chip = 1;
			// the low 4 bits of the word address tell (NAND-wise)
			// which chip to target
			for (int bit = 2; bit <= 0x10; bit += bit) {
				if ((mask & bit) == 0 && chip < chips.length) {
					chips[chip].writeSound(addr, val);
				}
				chip++;
			}
			soundHandler.updateVoice(machine.getCpu().getCurrentCycleCount(), machine.getCpu().getCurrentTargetCycleCount());
		}
	}
	
	public void setAudioGate(int addr, boolean b) {
		// assume it's the first one... I don't think the audio gates are wired to the chips
		chips[0].setAudioGate(addr, b);
	}

	public SoundHandler getSoundHandler() {
		return soundHandler;
	}
	
	public void setSoundHandler(SoundHandler soundHandler) {
		this.soundHandler = soundHandler;
		for (SoundTMS9919 chip : chips) {
			chip.setSoundHandler(null);
		}
	}

	public void loadState(IDialogSettings section) {
		if (section == null)
			return;
		int idx = 0;
		for (SoundTMS9919 chip : chips) {
			chip.loadState(section.getSection("" + idx));
			idx++;
		}
	}
	
	public void saveState(IDialogSettings section) {
		int idx = 0;
		for (SoundTMS9919 chip : chips) {
			chip.saveState(section.addNewSection("" + idx));
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
		SoundHandler handler = getSoundHandler();
		if (handler != null) {
			Cpu cpu = machine.getCpu();
			handler.flushAudio(cpu.getCurrentCycleCount(), 
					cpu.getCurrentTargetCycleCount());
		}
	}
}
