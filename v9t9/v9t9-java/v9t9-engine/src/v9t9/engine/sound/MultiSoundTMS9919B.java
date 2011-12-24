/**
 * 
 */
package v9t9.engine.sound;



import v9t9.common.machine.IMachine;
import v9t9.common.sound.MultiSoundTMS9919BConsts;

/**
 * Multiple packed TMS9919 chips.  This provides one traditional TMS9919
 * sound chip, followed by four TMS9919Bs, each with three ports (spaced by 2s).

 * @author ejs
 *
 */
public class MultiSoundTMS9919B extends BaseMultiSound {
	public MultiSoundTMS9919B(IMachine machine) {
		super(machine);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.sound.BaseMultiSound#doGenerateChips()
	 */
	@Override
	protected void doGenerateChips() {
		// 5 chips: the original TMS9919 on the console and 4 extra on the card
		this.chips = new SoundTMS9919[5];
		int regBase = 0;
		chips[0] = new SoundTMS9919(machine, "", "Console Chip", regBase);
		regBase = registerChip(chips[0], regBase);
		
		//chips[0] = new SoundTMS9919B(machine, "Console Chip");
		for (int i = 0; i < 4; i++) {
			chips[i + 1] = new SoundTMS9919B(machine, "C" + i + ":", "Chip #" + i, regBase);
			regBase = registerChip(chips[i + 1], regBase);
		}
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
			}
		}
	}
	
	public void setAudioGate(int addr, boolean b) {
		chips[0].setAudioGate(addr, b);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getGroupName()
	 */
	@Override
	public String getGroupName() {
		return MultiSoundTMS9919BConsts.GROUP_NAME;
	}

}
