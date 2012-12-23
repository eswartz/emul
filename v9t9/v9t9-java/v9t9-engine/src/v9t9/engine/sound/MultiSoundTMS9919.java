/**
 * 
 */
package v9t9.engine.sound;


import v9t9.common.hardware.ISoundChip;
import v9t9.common.machine.IMachine;
import v9t9.common.sound.ICassetteVoice;
import v9t9.common.sound.MultiSoundTMS9919Consts;

/**
 * Multiple packed TMS9919 chips.  This provides a TMS9919 at any number
 * of addresses.  It is based on the description of the FORTI sound chips at
 * <http://nouspikel.group.shef.ac.uk//ti99/forti.htm>
 * @author ejs
 *
 */
public class MultiSoundTMS9919 extends BaseMultiSound {
	public MultiSoundTMS9919(IMachine machine) {
		super(machine);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.sound.BaseMultiSound#doGenerateChips()
	 */
	@Override
	protected void doGenerateChips() {
		// 	5 chips: the original TMS9919 on the console and 4 extra on the card

		this.chips = new SoundTMS9919[5];
		int regBase = 0;
		for (int i = 0; i < chips.length; i++) {
			chips[i] = new SoundTMS9919(machine, "S" + i, "Chip #" + i, regBase);
			regBase = registerChip(chips[i], regBase);
		}
		
		regCount = regBase;		
	}
	

	public void writeSound(int addr, byte val) {
		if ((addr & 0xff) < 0xC0) {
			int mask = addr & 0x1e;

			if (mask == 0) {
				chips[0].writeSound(addr, val);
			} else {
				int chip = 1;
				// the low 4 bits of the word address tell (NAND-wise)
				// which chip to target
				for (int bit = 2; bit <= 0x10; bit += bit) {
					if ((mask & bit) == 0 && chip < chips.length) {
						chips[chip].writeSound(addr, val);
					}
					chip++;
				}
			}
		}
	}
	
	public void setAudioGate(int addr, boolean b) {
		chips[0].setAudioGate(addr, b);
	}

//	public void setCassette(int addr, boolean b) {
//		chips[0].setCassette(addr, b);
//	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.hardware.ISoundChip#getCassetteVoice()
	 */
	@Override
	public ICassetteVoice getCassetteVoice() {
		return chips[0].getCassetteVoice();
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getGroupName()
	 */
	@Override
	public String getGroupName() {
		return MultiSoundTMS9919Consts.GROUP_NAME;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.hardware.ISoundChip#reset()
	 */
	@Override
	public void reset() {
		for (ISoundChip chip : chips) {
			chip.reset();
		}
	}
}
