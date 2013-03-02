/*
  IHighLevelInstruction.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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

	/** 
	 * Set the next instruction after this one's PC addressable by the CPU.
	 * Not necessarily the logical instruction -- see {@link #getLogicalNext()} for that.
	 * @param next
	 */
	void setPhysicalNext(IHighLevelInstruction next);

	/** 
	 * Get the next possible instruction in memory.  This is not necessarily the next logical
	 * instruction, but the one at the next address according to the CPU's
	 * addressing capabilities. 
	 * @return
	 */
	IHighLevelInstruction getPhysicalNext();
	/** 
	 * Get the previous possible instruction in memory.  This is not necessarily the previous logical
	 * instruction, but the one at the previous address according to the CPU's
	 * addressing capabilities. 
	 * @return
	 */
	IHighLevelInstruction getPhysicalPrev();

	/** Get the instruction that precedes this one in PC order (not necessarily execution order).
	 * Its {@link #getInst()} will have the PC of this one's PC plus the receiver's size.
	 * @return
	 */
	IHighLevelInstruction getLogicalNext();
	
	/** 
	 * Get the instruction that precedes this one in PC order (not necessarily execution order).
	 * Its {@link #getInst()} plus its size will match the PC of the receiver's.
	 * @return
	 */
	IHighLevelInstruction getLogicalPrev();
	
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