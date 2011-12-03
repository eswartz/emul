/**
 * 
 */
package v9t9.common.asm;

import java.util.Collection;


/**
 * @author ejs
 *
 */
public interface IHighLevelInstruction {

	/* instruction flags */
	/** changes PC */
	final public static int fIsBranch = 1; /* changes PC */
	/** conditional branch (with fIsBranch) */
	final public static int fIsCondBranch = 2;
	/** a branch we expect to return (with fIsBranch) */
	final public static int fIsCall = 4;
	/** return from function */
	final public static int fIsReturn = 8;
	final public static int fCheckLater = 16; /* guessed flags */
	/** this instruction should end a block */
	final public static int fEndsBlock = 32;
	final public static int fVisited = 64; /* temporary flag */
	/** unknown behavior */
	public static final int fUnknown = 128;
	/** byte operation */
	public static final int fByteOp = 256;
	/** this instruction does not fall through (with fEndsBlock) */
	final public static int fNotFallThrough = 512;
	/** this instruction starts a block (synthetic) */
	final public static int fStartsBlock = 1024;

	String format(boolean showOpcodeAddr, boolean showComments);

	boolean isBranch();

	boolean isCall();

	boolean isReturn();

	void setNext(IHighLevelInstruction next);
	void setPrev(IHighLevelInstruction next);

	IHighLevelInstruction getNext();

	IHighLevelInstruction getPrev();

	
	void setBlock(Block block);

	Block getBlock();

	Collection<Block> getReferencedBlocks();

	void setWp(short wp);

	short getWp();

	/**
	 * @return
	 */
	RawInstruction getInst();

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	int compareTo(IHighLevelInstruction o);

	/**
	 * @return
	 */
	int getFlags();

	/**
	 * @param i
	 */
	void setFlags(int i);

	/**
	 * 
	 */
	void convertToData();

}