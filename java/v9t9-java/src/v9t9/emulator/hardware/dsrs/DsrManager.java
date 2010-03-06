/**
 * 
 */
package v9t9.emulator.hardware.dsrs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.ejs.coffee.core.utils.HexUtils;

import v9t9.emulator.Machine;
import v9t9.emulator.hardware.CruWriter;
import v9t9.engine.cpu.InstructionWorkBlock;

/**
 * @author ejs
 *
 */
public class DsrManager {

	private final Machine machine;
	private List<DsrHandler> dsrs;
	private DsrHandler activeDsr;

	public DsrManager(Machine machine) {
		this.machine = machine;
		dsrs = new ArrayList<DsrHandler>();
	}
	
	public void dispose() {
		for (DsrHandler dsr : dsrs) {
			dsr.dispose();
		}
	}
	

	public void saveState(IDialogSettings section) {
		for (DsrHandler handler : dsrs) {
			handler.saveState(section.addNewSection(handler.getName()));
		}
	}
	public void loadState(IDialogSettings section) {
		if (section == null) return;
		for (DsrHandler handler : dsrs) {
			handler.loadState(section.getSection(handler.getName()));
		}
	}
	/**
	 * @return the dsrs
	 */
	public List<DsrHandler> getDsrs() {
		return dsrs;
	}
	
	public void registerDsr(DsrHandler dsr) {
		addDeviceCRU(dsr.getCruBase(), dsr);
		this.dsrs.add(dsr);
	}

	private void addDeviceCRU(int addr, final DsrHandler dsr) {
		machine.getCruManager().add(addr, 1, new CruWriter() {

			public int write(int addr, int data, int num) {
				if (data == 1) {
					try {
						dsr.activate(machine.getConsole());
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

	/**
	 *	Your DSR module may use the DSR opcode range (OP_DSR) to make
	 *	callbacks into V9t9 to handle subroutine calls.  This routine
	 *	handles those opcodes by calling the DSR module 'filehandler'
	 *	callback.
	 */
	public void handleDSR(InstructionWorkBlock instructionWorkBlock) {
		short callpc = (short) (instructionWorkBlock.pc - 2);
		short opcode = instructionWorkBlock.domain.readWord(callpc);
		short rambase = (short) (instructionWorkBlock.wp - 0xe0);
		short crubase = instructionWorkBlock.domain.readWord(rambase+ 0xD0);

		if (callpc >= 0x4000 && callpc < 0x6000) {
			
			/*  Only respond if we have an active module whose
			   base matches that which DSRLNK is currently scanning. */
			if (activeDsr != null && activeDsr.getCruBase() == crubase) {
				System.out.println("handling DSR: pc = "+HexUtils.toHex4(callpc)+" " + HexUtils.toHex4(opcode));

				// on success, return to DSR handler, to return an
				// error or otherwise terminate instead of continuing
				// to scan CRU bases
				
				MemoryTransfer xfer = new ConsoleMemoryTransfer(
						instructionWorkBlock.domain,
						machine.getVdp(), rambase);
				
				int retreg = instructionWorkBlock.wp + 11 * 2;
				short ret = instructionWorkBlock.domain.readWord(retreg);
				if (activeDsr.handleDSR(xfer, (short) (opcode  & 0x3f))) {
					// success: skip next word (handling error)
					ret += 2;
				}
				instructionWorkBlock.domain.writeWord(retreg, ret);
				instructionWorkBlock.pc = ret;
			}
		}
	}
}
