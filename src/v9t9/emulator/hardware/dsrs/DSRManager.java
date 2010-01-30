/**
 * 
 */
package v9t9.emulator.hardware.dsrs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ejs.emul.core.utils.HexUtils;

import v9t9.emulator.Machine;
import v9t9.emulator.hardware.CruWriter;
import v9t9.engine.cpu.InstructionWorkBlock;

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
	public void handleDSR(InstructionWorkBlock instructionWorkBlock) {
		short callpc = (short) (instructionWorkBlock.pc - 2);
		short opcode = instructionWorkBlock.domain.readWord(callpc);
		short crubase = instructionWorkBlock.domain.readWord((instructionWorkBlock.wp & ~0xff) | 0x00D0);

		if (callpc >= 0x4000 && callpc < 0x6000) {
			
			/*  Only respond if we have an active module whose
			   base matches that which DSRLNK is currently scanning. */
			if (activeDsr != null && activeDsr.getCruBase() == crubase) {
				System.out.println("handling DSR: pc = "+HexUtils.toHex4(callpc)+" " + HexUtils.toHex4(opcode));

				// on success, return to DSR handler, to return an
				// error or otherwise terminate instead of continuing
				// to scan CRU bases
				if (activeDsr.handleDSR(machine.getCpu(), (short) (opcode  & 0x3f))) {
					instructionWorkBlock.pc = instructionWorkBlock.domain.readWord(instructionWorkBlock.wp + 11 * 2);
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
