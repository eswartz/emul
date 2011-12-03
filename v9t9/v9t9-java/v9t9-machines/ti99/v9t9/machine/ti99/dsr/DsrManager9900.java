/**
 * 
 */
package v9t9.machine.ti99.dsr;

import java.io.IOException;


import v9t9.base.properties.IPersistable;
import v9t9.base.utils.HexUtils;
import v9t9.common.asm.BaseMachineOperand;
import v9t9.common.cpu.InstructionWorkBlock;
import v9t9.engine.dsr.ConsoleMemoryTransfer;
import v9t9.engine.dsr.DsrManager;
import v9t9.engine.dsr.IDsrHandler;
import v9t9.engine.dsr.IDsrManager;
import v9t9.engine.dsr.IMemoryTransfer;
import v9t9.engine.hardware.ICruWriter;
import v9t9.machine.ti99.cpu.InstructionWorkBlock9900;
import v9t9.machine.ti99.machine.TI99Machine;

/**
 * @author ejs
 *
 */
public class DsrManager9900 extends DsrManager implements IPersistable, IDsrManager {

	public DsrManager9900(TI99Machine machine) {
		super(machine);
	}
	
	@Override
	public void registerDsr(IDsrHandler dsr) {
		super.registerDsr(dsr);
		if (dsr instanceof DsrHandler9900) {
			addDeviceCRU(((DsrHandler9900) dsr).getCruBase(), dsr);
		}
	}
	
	protected void addDeviceCRU(int addr, final IDsrHandler dsr) {
		((TI99Machine)machine).getCruManager().add(addr, 1, new ICruWriter() {

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
	
	
	public void handleDSR(InstructionWorkBlock instructionWorkBlock_) {
		InstructionWorkBlock9900 instructionWorkBlock = (InstructionWorkBlock9900) instructionWorkBlock_;
		short callpc = (short) (instructionWorkBlock.pc - 2);
		short rambase = (short) (instructionWorkBlock.wp - 0xe0);
		short crubase = instructionWorkBlock.domain.readWord(instructionWorkBlock.wp + 12 * 2);
	
		if (callpc >= 0x4000 && callpc < 0x6000) {
			
			/*  Only respond if we have an active module whose
			   base matches that which DSRLNK is currently scanning. */
			if (activeDsr != null && ((DsrHandler9900)activeDsr).getCruBase() == crubase) {
				System.out.println("handling DSR: pc = "+HexUtils.toHex4(callpc)+" " + instructionWorkBlock.inst);
	
				// on success, return to DSR handler, to return an
				// error or otherwise terminate instead of continuing
				// to scan CRU bases
				
				IMemoryTransfer xfer = new ConsoleMemoryTransfer(
						instructionWorkBlock.domain,
						machine.getVdp(), rambase);
				
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
