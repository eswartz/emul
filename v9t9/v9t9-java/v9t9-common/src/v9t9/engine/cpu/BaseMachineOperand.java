/**
 * 
 */
package v9t9.engine.cpu;

/**
 * @author Ed
 *
 */
public abstract class BaseMachineOperand implements MachineOperand {

	public int type = OP_NONE;
	/** value in opcode, usually register or count */
	public int val = 0;
	/** immediate word */
	public short immed = 0;
	public int dest = OP_DEST_FALSE;
	public boolean bIsReference = false;
}