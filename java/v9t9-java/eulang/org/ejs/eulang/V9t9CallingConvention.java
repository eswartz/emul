/**
 * 
 */
package org.ejs.eulang;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.ejs.eulang.ICallingConvention.Location;
import org.ejs.eulang.ICallingConvention.RegisterLocation;
import org.ejs.eulang.ICallingConvention.StackBarrierLocation;
import org.ejs.eulang.TypeEngine.Alignment;
import org.ejs.eulang.TypeEngine.Target;
import org.ejs.eulang.llvm.FunctionConvention;
import org.ejs.eulang.llvm.LLArgAttrType;
import org.ejs.eulang.llvm.LLAttrType;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLType;

/**
 * Standard V9t9 calling convention.  
 * <p>
 * Bools, ints, pointers, double-wide ints, passed in int registers.
 * Floats passed on the stack.  
 * Data and tuples are passed on the stack (FOR NOW).
 * <p>
 * Items may be interleaved among registers and the stack.
 * <p>
 * Bools, ints, pointers, double-wide ints, returned in R0 (R1).
 * Floats returned on the stack.
 * Data and tuples are passed on the stack (FOR NOW).
 * <p>
 * R0-R3 are argument registers and temporaries.
 * R4-R9 are temporaries and must be preserved.
 * R10 is the stack pointer.
 * R11 may be used as a temporary if no calls are done; must be preserved otherwise.
 * R12-R15 may be used as temporaries but must be preserved.  
 * @author ejs
 *
 */
public class V9t9CallingConvention implements ICallingConvention {


	private IRegClass intClass;
	private final ITarget target;
	private final FunctionConvention conv;

	public V9t9CallingConvention(ITarget target, FunctionConvention conv) {
		this.target = target;
		this.conv = conv;
		for (IRegClass rclass : target.getRegisterClasses()) {
			if (rclass.getBasicType() == BasicType.INTEGRAL) {
				intClass = rclass;
				break;
			}
		}
		assert intClass != null;
	}
	
	private boolean allocInt(List<Location> locs, LinkedList<Integer> argRegs,LLAttrType arg, String name) {
		int intRegSize = intClass.getRegisterSize();
		int doubleIntRegSize = intRegSize * 2;
		
		
		if (!argRegs.isEmpty() && intClass.supportsType(arg.getType())) {
			if (argRegs.size() >= 2 && arg.getType().getBits() == doubleIntRegSize) {
				locs.add(new RegisterLocation(name+"$hi", arg.getType(), intRegSize, intClass, argRegs.remove()));
				locs.add(new RegisterLocation(name+"$lo", arg.getType(), 0, intClass, argRegs.remove()));
				return true;
			}
			if (arg.getType().getBits() <= intRegSize) {
				locs.add(new RegisterLocation(name, arg.getType(), 0, intClass, argRegs.remove()));
				return true;
			}
		}
		// TODO: small tuples and data...
		return false;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ICallingConvention#getIncomingLocations(org.ejs.eulang.llvm.FunctionConvention)
	 */
	@Override
	public Location[] getArgumentLocations() {
		List<Location> locs = new ArrayList<Location>();
		
		// available
		LinkedList<Integer> argRegs = new LinkedList<Integer>();
		argRegs.add(0);
		argRegs.add(1);
		argRegs.add(2);
		argRegs.add(3);
		
		Alignment align = target.getTypeEngine().new Alignment(Target.STACK);
		
		List<LLArgAttrType> stackArgs = new ArrayList<LLArgAttrType>();
		for (LLArgAttrType arg : conv.getArgTypes()) {
			boolean alloced = false;
			alloced = allocInt(locs, argRegs, arg, arg.getName());

			if (!alloced) {
				stackArgs.add(arg);
			}
		}
		
		// stack args ordered reverse
		for (ListIterator<LLArgAttrType> iter = stackArgs.listIterator(stackArgs.size()); iter.hasPrevious(); ) {
			LLArgAttrType arg = iter.previous();
			//int alignedSize = align.alignedSize(arg.getType());
			//int curOffs = align.sizeof();
			//int endOffs = align.alignAndAdd(arg.getType());
			//int argEnd = curOffs - endOffs;
			align.alignAndAdd(arg.getType());
			locs.add(new StackLocation(((LLArgAttrType) arg).getName(),
					arg.getType(), -(align.sizeof() - arg.getType().getBits() ) / 8));

		}
		
		locs.add(new StackBarrierLocation("%stacksize", this.target.getTypeEngine().VOID, 
				align.sizeof() / 8));
		
		return (Location[]) locs.toArray(new Location[locs.size()]);
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ICallingConvention#getOutgoingLocations(org.ejs.eulang.llvm.FunctionConvention)
	 */
	@Override
	public Location[] getReturnLocations() {
		List<Location> locs = new ArrayList<Location>();
		
		// available
		LinkedList<Integer> retRegs = new LinkedList<Integer>();
		retRegs.add(0);
		retRegs.add(1);
		
		LLType type = conv.getRetType().getType();
		
		boolean alloced = allocInt(locs, retRegs, conv.getRetType(), "return");

		if (!alloced) {
			// stack
			locs.add(new CallerStackLocation("return", type));
		}
		
		return (Location[]) locs.toArray(new Location[locs.size()]);
	
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ICallingConvention#getFixedRegisters(org.ejs.eulang.IRegClass)
	 */
	@Override
	public int[] getFixedRegisters(IRegClass regClass) {
		if (regClass.equals(intClass))
			return new int[] { 10 }; 
		return new int[0];
	}
}
