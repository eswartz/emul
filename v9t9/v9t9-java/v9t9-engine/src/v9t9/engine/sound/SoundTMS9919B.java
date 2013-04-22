/*
  SoundTMS9919B.java

  (c) 2009-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.sound;

import v9t9.common.machine.IMachine;
import v9t9.common.sound.IVoice;
import v9t9.common.sound.TMS9919BConsts;

import static v9t9.common.sound.TMS9919BConsts.*;

/**
 * Controller for the TMS9919(B) sound chip.
 * </pre>
 * @author ejs
 *
 */
public class SoundTMS9919B extends SoundTMS9919 {
	protected int lastCommand;

	private int cmdVoice;
	
	public SoundTMS9919B(IMachine machine, String id, String name, int regBase) {
		super(machine, id, name, regBase);
		lastCommand = 0;
	}
	

	/** Initialize registers and return new regBase */
	public int initRegisters(String id, String name, int regBase) {
		int origRegBase = regBase;
		int count;
		for (int i = 0; i < 3; i++) {
			voices[i] = new EnhancedToneVoice(id + "V" + i, 
					name + " Voice " + i, 
					listeners,
					CMD_NUM_EFFECTS);
			count = ((BaseVoice) voices[i]).initRegisters(regNames, regDescs, regIds, regBase);
			mapRegisters(regBase, count, voices[i]);
			regBase += count;
		}
		
		voices[3] = new EnhancedNoiseVoice(id + "N", 
				name + " Noise", 
				listeners,
				CMD_NUM_EFFECTS);
		count = ((BaseVoice) voices[3]).initRegisters(regNames, regDescs, regIds, regBase);
		mapRegisters(regBase, count, voices[3]);
		regBase += count;
		
		audioGateVoice = new AudioGateVoice(id + "A", name + " Audio Gate", listeners, machine);
		count = ((BaseVoice) audioGateVoice).initRegisters(regNames, regDescs, regIds, regBase);
		mapRegisters(regBase, count, audioGateVoice);
		regBase += count;
//		
//		cassetteVoice = new CassetteVoice(id + "C", name + " Cassette", listeners, machine);
//		count = ((BaseVoice) cassetteVoice).initRegisters(regNames, regDescs, regIds, regBase);
//		mapRegisters(regBase, count, cassetteVoice);
//		regBase += count;
		
		return regBase - origRegBase;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.SoundHandler#writeSound(byte)
	 */
	public void writeSound(int addr, byte val) {
		//System.out.println("Writing " + Utils.toHex2(addr & 0x6) + " := " + Utils.toHex2(val));
		if ((addr & 0x6) == 0x2) {
			// command byte
			if ((val & 0x80) != 0) {
				cmdVoice = getOperationVoice(val);
				
				IEnhancedVoice voice = (IEnhancedVoice) voices[cmdVoice];
				
				lastCommand = (val & 0xf);
				switch (lastCommand) {
				case CMD_RESET:
				case CMD_RELEASE:
					// these have no arguments
					voice.setEffect(lastCommand, (byte) 0);
					break;
				default:
					// others take an argument next time
				}
			}
		} else if ((addr & 0x6) == 0x4) {
			IEnhancedVoice voice = (IEnhancedVoice) voices[cmdVoice];
			voice.setEffect(lastCommand, val);
		} else {
			super.writeSound(addr, val);
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getGroupName()
	 */
	@Override
	public String getGroupName() {
		return TMS9919BConsts.GROUP_NAME;
	}
	

	/* (non-Javadoc)
	 * @see v9t9.common.hardware.ISoundChip#reset()
	 */
	@Override
	public void reset() {
		super.reset();
		
		for (IVoice v : voices) {
			IEnhancedVoice voice = (IEnhancedVoice) v;
			
			voice.setEffect(CMD_RESET, (byte) 0);
		}		
	}
}
