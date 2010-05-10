/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.ejs.eulang.ICallingConvention;
import org.ejs.eulang.IRegClass;
import org.ejs.eulang.ITarget;
import org.ejs.eulang.ICallingConvention.Location;
import org.ejs.eulang.ICallingConvention.RegisterLocation;
import org.ejs.eulang.ICallingConvention.StackLocation;
import org.ejs.eulang.TypeEngine.Alignment;
import org.ejs.eulang.TypeEngine.Target;
import org.ejs.eulang.llvm.ILLCodeVisitor;
import org.ejs.eulang.llvm.LLBlock;
import org.ejs.eulang.llvm.LLCodeVisitor;
import org.ejs.eulang.llvm.directives.LLDefineDirective;
import org.ejs.eulang.llvm.instrs.LLAssignInstr;
import org.ejs.eulang.llvm.instrs.LLInstr;
import org.ejs.eulang.llvm.instrs.LLStoreInstr;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.ops.LLSymbolOp;
import org.ejs.eulang.llvm.ops.LLTempOp;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;

/**
 * Manage locals for a routine.
 * @author ejs
 *
 */
public class Locals {

	private final ITarget target;
	private Map<ISymbol, ILocal> argumentLocals;
	private Map<ISymbol, StackLocal> stackLocals;
	private Map<ISymbol, RegisterLocal> regLocals;
	protected LLBlock currentBlock;
	private Alignment alignment;
	
	private IScope localScope;
	private final LLDefineDirective def;
	private HashMap<IRegClass, RegAlloc> regAllocs;
	private boolean forceLocalsToStack;
	private ICallingConvention cc;
	
	public Locals(ITarget target, LLDefineDirective def) {
		this.target = target;
		this.def = def;
		
		this.cc = target.getCallingConvention(def.getConvention());

		argumentLocals = new LinkedHashMap<ISymbol, ILocal>();
		stackLocals = new LinkedHashMap<ISymbol, StackLocal>();
		regLocals = new LinkedHashMap<ISymbol, RegisterLocal>();
		localScope = def.getScope();
		
		alignment = target.getTypeEngine().new Alignment(Target.STACK);
		regAllocs = new HashMap<IRegClass, RegAlloc>();
		for (IRegClass regClass : target.getRegisterClasses()) {
			regAllocs.put(regClass, new RegAlloc(target, cc, regClass, localScope));
		}
	}
	
	public void setForceLocalsToStack(boolean forceLocalsToStack) {
		this.forceLocalsToStack = forceLocalsToStack;
	}
	/**
	 * @return the forceLocalsToStack
	 */
	public boolean isForceLocalsToStack() {
		return forceLocalsToStack;
	}

	public Map<ISymbol, StackLocal> getStackLocals() {
		return stackLocals;
	}
	/**
	 * @return the regLocals
	 */
	public Map<ISymbol, RegisterLocal> getRegLocals() {
		return regLocals;
	}
	
	public void buildLocalTable() {
		currentBlock = def.getEntryBlock();
		allocateParams();
		
		ILLCodeVisitor visitor = new LLCodeVisitor() {
			@Override
			public boolean enterBlock(LLBlock block) {
				currentBlock = block;
				System.out.println("Block " + currentBlock.getLabel());
				return true;
			}
			public boolean enterInstr(LLInstr instr) {
				if (instr instanceof LLAssignInstr) {
					LLAssignInstr assign = (LLAssignInstr) instr;
					if (assign.getResult() instanceof LLSymbolOp)
						allocateLocal(assign);
					else if (assign.getResult() instanceof LLTempOp)
						allocateTemp(assign);
					else
						assert false;
				}
				else if (instr instanceof LLStoreInstr) {
					// see if we're storing into an argument local
					matchLocalAllocation((LLStoreInstr) instr);
				}
				return false;
			}
		};
		def.accept(visitor);
	}

	/**
	 * @param instr
	 */
	protected void matchLocalAllocation(LLStoreInstr instr) {
		LLOperand[] ops = instr.getOperands();
		if (ops[0] instanceof LLSymbolOp && ops[1] instanceof LLSymbolOp) {
			ISymbol argSym = ((LLSymbolOp) ops[0]).getSymbol();
			ILocal arg = argumentLocals.get(argSym);
			if (arg instanceof StackLocal && arg.getIncoming() == null) {
				ISymbol mirrorSym = ((LLSymbolOp) ops[1]).getSymbol();
				StackLocal mirror = stackLocals.get(mirrorSym);
				System.out.println("Reassigning " + mirror + " to " + arg);
				
				int curOffset = mirror.getOffset();
				mirror.setOffset(((StackLocal) arg).getOffset());
				mirror.setIncoming(arg);
				
				// recover stack space: should always work since we store
				// immediately after allocating
				int argStackSize = alignment.alignedSize(mirror.getType());
				if (-curOffset * 8 + argStackSize == alignment.sizeof()) {
					alignment.add(-argStackSize);
				} else {
					System.err.println("Failed to recover stack space");
				}
			}
		}
	}

	/**
	 * Find where we want to place the arguments.
	 */
	private void allocateParams() {
		Location[] locations = cc.getArgumentLocations();
		
		for (Location loc : locations) {
			ILocal local = null;
			if (loc instanceof ICallingConvention.RegisterLocation) {
				// fixed register
				ICallingConvention.RegisterLocation regLoc = (RegisterLocation) loc;
				local = allocateRegister(localScope.add(loc.name, true),
						loc.type, regLoc.regClass, regLoc.number);
			}
			else if (loc instanceof ICallingConvention.StackLocation) {
				ICallingConvention.StackLocation stackLoc = (StackLocation) loc;
				
				// location is relative to the canonical frame pointer: there is a return addr in between
				int frameOffs = -target.getTypeEngine().getPtrBits() / 8 - stackLoc.offset;
				local = allocateLocal(localScope.add(loc.name, true), loc.type, frameOffs); 
			}
			else
				assert false;
			
			ISymbol arg = localScope.get(loc.name);
			assert arg != null;
			argumentLocals.put(arg, local);
		}
	}

	protected ILocal allocateLocal(LLAssignInstr instr) {
		
		LLSymbolOp result = (LLSymbolOp) instr.getResult();
		ISymbol name = result.getSymbol();
		
		if (name.getType().getBits() % 8 != 0)
			unhandled(result);
		return allocateLocal(name, instr.getType());
	}

	public ILocal allocateLocal(ISymbol name, LLType type) {
		
		if (!forceLocalsToStack) {
			// TODO
		}
		
		int offs = alignment.alignAndAdd(type);
		return allocateLocal(name, type, offs / 8);
	}

	private ILocal allocateLocal(ISymbol name, LLType type, int byteOffs) {
		StackLocal local = new StackLocal(name, type, currentBlock.getLabel(), -byteOffs);
		
		assert !stackLocals.containsKey(name);
		
		stackLocals.put(name, local);
		System.out.println("Allocated " + local);
		
		return local;
	}

	public RegisterLocal allocateRegister(ISymbol name, LLType type, IRegClass regClass, int number) {
		RegAlloc alloc = regAllocs.get(regClass);
		assert alloc != null;
		
		RegisterLocal local = new RegisterLocal(regClass, name, type, number);
		alloc.allocateRegister(number);
		
		System.out.println("Allocated " + local);
		regLocals.put(name, (RegisterLocal) local);
		
		return local;
	}

	public void allocateTemp(LLAssignInstr instr) {
		
		LLTempOp result = (LLTempOp) instr.getResult();
		
		ISymbol name = localScope.add(result.getName(), true);
		name.setType(result.getType());
		
		allocateTemp(name, result.getType());
	}
	
	public ILocal allocateTemp(ISymbol name, LLType type) {
		ILocal local = null;
		
		for (RegAlloc regAlloc : regAllocs.values()) {
			try {
				local = regAlloc.allocate(name);
				break;
			} catch (UnsupportedOperationException e) {
				
			}
		}
		
		if (local == null) {
			return allocateLocal(name, type);
		}
		
		System.out.println("Allocated " + local);
		regLocals.put(name, (RegisterLocal) local);
		
		return local;
	}

	private void unhandled(LLOperand op) {
		throw new IllegalStateException(op.toString());
	}

	/**
	 * @return size in bytes
	 */
	public int getFrameSize() {
		return alignment.sizeof() / 8;
	}


}
