/*
  DsrManager.java

  (c) 2008-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.machine.ti99.dsr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ejs.base.properties.IPersistable;
import ejs.base.settings.ISettingSection;

import v9t9.common.asm.BaseMachineOperand;
import v9t9.common.cpu.InstructionWorkBlock;
import v9t9.common.dsr.IDsrHandler;
import v9t9.common.dsr.IDsrManager;
import v9t9.common.dsr.IMemoryTransfer;
import v9t9.common.machine.IMachine;
import v9t9.engine.dsr.ConsoleMemoryTransfer;
import v9t9.engine.hardware.ICruWriter;
import v9t9.engine.video.tms9918a.VdpTMS9918A;
import v9t9.machine.ti99.cpu.InstructionWorkBlock9900;
import v9t9.machine.ti99.machine.TI99Machine;

/**
 * @author ejs
 *
 */
public class DsrManager implements IPersistable, IDsrManager {

	protected final IMachine machine;
	protected List<IDsrHandler> dsrs;
	
	protected IDsrHandler9900 activeDsr;

	public DsrManager(TI99Machine machine) {
		this.machine = machine;
		dsrs = new ArrayList<IDsrHandler>();
	}
	
	public void dispose() {
		for (IDsrHandler dsr : dsrs) {
			dsr.dispose();
		}
	}

	public void saveState(ISettingSection section) {
		for (IDsrHandler handler : dsrs) {
			handler.saveState(section.addSection(handler.getName()));
		}
	}

	public void loadState(ISettingSection section) {
		if (section == null) return;
		for (IDsrHandler handler : dsrs) {
			handler.loadState(section.getSection(handler.getName()));
		}
	}

	public List<IDsrHandler> getDsrs() {
		return dsrs;
	}

	public void registerDsr(IDsrHandler dsr) {
		this.dsrs.add(dsr);
		if (dsr instanceof IDsrHandler9900) {
			addDeviceCRU(((IDsrHandler9900) dsr).getCruBase(), (IDsrHandler9900) dsr);
		}
	}
	
	protected void addDeviceCRU(int addr, final IDsrHandler9900 dsr) {
		((TI99Machine)machine).getCruManager().add(addr, 1, new ICruWriter() {

			public int write(int addr, int data, int num) {
				if (data == 1) {
					try {
						dsr.activate(machine.getConsole(), machine.getMemory().getMemoryEntryFactory());
						activeDsr = dsr;
					} catch (IOException e) {
						System.err.println("Could not active DSR " + dsr.getName() + ": " + e.getMessage());
					}
				} else {
					dsr.deactivate(machine.getConsole());
					activeDsr = null;
				}
				return 0;
			}
			
		});
	}
	
	
	public void handleDSR(InstructionWorkBlock instructionWorkBlock_) {
		InstructionWorkBlock9900 instructionWorkBlock = (InstructionWorkBlock9900) instructionWorkBlock_;
		short callpc = (short) (instructionWorkBlock.pc - 2);
		short rambase = (short) (instructionWorkBlock.wp - 0xe0);
		short crubase = instructionWorkBlock.domain.readWord(instructionWorkBlock.wp + 12 * 2);
	
		if (callpc >= 0x4000 && callpc < 0x6000) {
			
			/*  Only respond if we have an active module whose
			   base matches that which DSRLNK is currently scanning. */
			if (activeDsr != null && ((IDsrHandler9900)activeDsr).getCruBase() == crubase) {
				//System.out.println("handling DSR: pc = "+HexUtils.toHex4(callpc)+" " + instructionWorkBlock.inst);
	
				// on success, return to DSR handler, to return an
				// error or otherwise terminate instead of continuing
				// to scan CRU bases
				
				IMemoryTransfer xfer = new ConsoleMemoryTransfer(
						instructionWorkBlock.domain,
						machine.getVdp(),
						((VdpTMS9918A) machine.getVdp()).getVdpMmio(),
						rambase);
				
				int retreg = instructionWorkBlock.wp + 11 * 2;
				short ret = instructionWorkBlock.domain.readWord(retreg);
				if (activeDsr.handleDSR(xfer, (short) ((BaseMachineOperand)instructionWorkBlock.inst.getOp1()).val)) {
					// success: skip next word (handling error)
					ret += 2;
				}
				instructionWorkBlock.domain.writeWord(retreg, ret);
				instructionWorkBlock.pc = ret;
			}
		}
	}
}
