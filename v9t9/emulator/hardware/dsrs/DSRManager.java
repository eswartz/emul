/**
 * 
 */
package v9t9.emulator.hardware.dsrs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import v9t9.emulator.Machine;
import v9t9.emulator.hardware.CruWriter;
import v9t9.engine.cpu.InstructionAction.Block;
import v9t9.utils.Utils;

/**
 * @author ejs
 *
 */
public class DSRManager {

	private final Machine machine;
	private List<DsrHandler> dsrs;
	private DsrHandler activeDsr;

	public DSRManager(Machine machine) {
		this.machine = machine;
		dsrs = new ArrayList<DsrHandler>();
	}
	
	public void registerDsr(DsrHandler dsr) {
		addDeviceCRU(machine, dsr.getCruBase(), dsr);
		this.dsrs.add(dsr);
	}

	private void addDeviceCRU(Machine machine, int addr, final DsrHandler dsr) {
		machine.getCruManager().add(addr, 1, new CruWriter() {

			public int write(int addr, int data, int num) {
				if (data == 1) {
					try {
						dsr.activate();
					} catch (IOException e) {
						System.err.println("Could not active DSR " + dsr.getName() + ": " + e.getMessage());
					}
				} else {
					dsr.deactivate();
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
	public void handleDSR(Block block) {
		short callpc = (short) (block.pc - 2);
		short opcode = block.domain.readWord(callpc);
		short crubase = block.domain.readWord(0x83D0);

		if (callpc >= 0x4000 && callpc < 0x6000) {
			
			/*  Only respond if we have an active module whose
			   base matches that which DSRLNK is currently scanning. */
			if (activeDsr != null && activeDsr.getCruBase() == crubase) {
				System.out.println("handling DSR: pc = "+Utils.toHex4(callpc)+" " + Utils.toHex4(opcode));

				// on success, return to DSR handler, to return an
				// error or otherwise terminate instead of continuing
				// to scan CRU bases
				if (activeDsr.handleDSR(machine.getCpu(), (short) (opcode  & 0x3f))) {
					block.pc = block.domain.readWord(block.wp + 11 * 2);
				}
			}
		}
	}

	public void activate(DsrHandler dsr) {
		activeDsr = dsr;
	}

	public void deactivate(DsrHandler dsr) {
		activeDsr = null;
	}

}
