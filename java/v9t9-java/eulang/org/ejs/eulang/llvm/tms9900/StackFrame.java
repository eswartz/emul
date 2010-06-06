/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.ejs.eulang.ICallingConvention;
import org.ejs.eulang.IRegClass;
import org.ejs.eulang.ITarget;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ICallingConvention.CallerStackLocation;
import org.ejs.eulang.ICallingConvention.Location;
import org.ejs.eulang.ICallingConvention.RegisterLocation;
import org.ejs.eulang.ICallingConvention.StackBarrierLocation;
import org.ejs.eulang.ICallingConvention.StackLocation;
import org.ejs.eulang.TypeEngine.Alignment;
import org.ejs.eulang.TypeEngine.Target;
import org.ejs.eulang.llvm.ILLCodeVisitor;
import org.ejs.eulang.llvm.LLBlock;
import org.ejs.eulang.llvm.LLCodeVisitor;
import org.ejs.eulang.llvm.directives.LLDefineDirective;
import org.ejs.eulang.llvm.instrs.LLAllocaInstr;
import org.ejs.eulang.llvm.instrs.LLAssignInstr;
import org.ejs.eulang.llvm.instrs.LLInstr;
import org.ejs.eulang.llvm.instrs.LLStoreInstr;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.ops.LLSymbolOp;
import org.ejs.eulang.llvm.ops.LLTempOp;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLArrayType;
import org.ejs.eulang.types.LLType;

/**
 * Manage locals for a routine.
 * <p>
 * The stack looks like this:
 * 
 * When a frame pointer is used:
 * 
 * <pre>
 * SP -K  end location of next callee's args
 *    -s3 ...
 *    -s2 local 2
 *    -s1 local 1		(end of local 2)
 * FP  0  saved FP		(end of local 1)
 *     2  saved reg R11
 *     4  saved reg #0, ...
 *     6  last saved register
 *     8  last non-reg arg
 *    10  non-reg arg N-1
 *    12  ...
 *    +K  non-reg arg 1
 *    +K+w callee-return location, if any
 * </pre>
 * 
 * This is handled with:
 * 
 * <pre>
 *    AI SP, -saved*2+2	// save any registers (plus room for FP)
 *    MOV R..., @4(SP)	// saved regs		
 *    MOV R11, @2(SP)	// if called
 *    MOV FP, *SP		// save FP
 *    MOV SP, FP		// new FP
 *    AI  SP, -K		// locals
 *    ...
 *    MOV FP, SP		// remove locals
 *    MOV *SP+, FP  	// restore FP
 *    MOV *SP+, R...	// restore registers
 *    MOV *SP+, R11		// restore registers
 *    B *R11
 * </pre>
 * (min overhead, with no saved regs, no calls, and locals: 10, 6 = 16 bytes)
 * (min overhead, with no saved regs, calls, and no locals: 8, 8 = 16 bytes)
 * 
 * When no frame pointer is used:
 * 
 * <pre>
 * SP -K  location of next callee's args
 *    -s3 ...
 *    -s2 local 2
 *    -s1 local 1
 *     0  saved reg R11
 *     2  saved reg #...
 *     4  last saved register
 *     6  last non-reg arg
 *     8  non-reg arg N-1
 *    10  ...
 *    +K  non-reg arg 1
 * </pre>
 * 
 * This is handled with:
 * 
 * <pre>
 *    AI SP, -saved*2-K	// save any registers and get local space
 *    MOV R...,*SP			// saved regs
 *    MOV R11, @(K)SP		// if calls
 *    ...
 *    AI SP, K  	  	// remove locals (only)
 *    MOV *SP+, R...	// restore registers
 *    MOV *SP+, R11		// restore registers
 *    B *R11
 * </pre>
 * (min overhead: no locals, no calls: 0, 2) 
 * (min overhead: no locals, calls: 10) 
 * (min overhead: locals, calls: 16) 

 * @author ejs
 *
 */
public class StackFrame {
	public boolean DUMP = false;

	/** any locals defined on the stack */
	private Map<ISymbol, StackLocal> stackLocals;
	/** any locals defined in registers */
	private Map<ISymbol, RegisterLocal> regLocals;

	/** only those locals defined as incoming arguments */
	private Map<ISymbol, ILocal> argumentLocals;
	/** only those locals defined as expression temps */
	private Map<ISymbol, RegisterLocal> tempLocals;
	
	protected LLBlock currentBlock;
	private Alignment alignment;
	
	private IScope localScope;
	private HashMap<IRegClass, RegAlloc> regAllocs;
	private boolean forceLocalsToStack;
	
	private boolean requiresFramePointer;

	private TypeEngine typeEngine;

	public StackFrame(ITarget target) {
		typeEngine = target.getTypeEngine();
		
		stackLocals = new LinkedHashMap<ISymbol, StackLocal>();
		regLocals = new LinkedHashMap<ISymbol, RegisterLocal>();
		
		argumentLocals = new LinkedHashMap<ISymbol, ILocal>();
		tempLocals = new LinkedHashMap<ISymbol, RegisterLocal>();
		
		alignment = target.getTypeEngine().new Alignment(Target.STACK);
		regAllocs = new HashMap<IRegClass, RegAlloc>();
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

	/** Get locals allocated on the stack */
	public Map<ISymbol, StackLocal> getStackLocals() {
		return stackLocals;
	}
	/** Get locals allocated as registers */
	public Map<ISymbol, RegisterLocal> getRegLocals() {
		return regLocals;
	}
	
	/**
	 * Get the locals defined for arguments, which may contain entries from stackLocals or regLocals
	 */
	public Collection<ILocal> getArgumentLocals() {
		return argumentLocals.values();
	}
	/**
	 * Get the locals which are expression temps that go into registers.
	 * @return the tempLocals
	 */
	public Collection<RegisterLocal> getTempLocals() {
		return tempLocals.values();
	}
	
	/**
	 * Initialize the stack frame, scanning for argument and local references
	 * to determine their FP offsets, and performing the initial determination
	 * of whether a stack frame is needed (e.g. if variable-sized arrays
	 * are allocated).
	 * @param def
	 */
	public void buildLocalTable(final LLDefineDirective def) {
		stackLocals.clear();
		regLocals.clear();
		argumentLocals.clear();
		tempLocals.clear();
		requiresFramePointer = false;
		
		ILLCodeVisitor visitor = new LLCodeVisitor() {
			private Map<Integer, LLAssignInstr> tempDefs = new TreeMap<Integer, LLAssignInstr>();
			
			/* (non-Javadoc)
			 * @see org.ejs.eulang.llvm.LLCodeVisitor#enterCode(org.ejs.eulang.llvm.directives.LLDefineDirective)
			 */
			@Override
			public boolean enterCode(LLDefineDirective directive) {
				ITarget target = directive.getTarget();
				currentBlock = directive.getEntryBlock();
				localScope = directive.getScope();
				
				ICallingConvention cc = target.getCallingConvention(directive.getConvention());

				regAllocs.clear();
				for (IRegClass regClass : target.getRegisterClasses()) {
					regAllocs.put(regClass, new RegAlloc(target, cc, regClass, localScope));
				}
				
				allocateParams(cc);
				return super.enterCode(directive);
			}
			@Override
			public boolean enterBlock(LLBlock block) {
				currentBlock = block;
				if (DUMP) System.out.println("Block " + currentBlock.getLabel());
				return true;
			}
			@Override
			public boolean enterInstr(LLBlock block, LLInstr instr) {
				if (instr instanceof LLAllocaInstr) {
					// allocating space for a variable; try to put it in a register
					LLAllocaInstr alloca = (LLAllocaInstr) instr;
					if (alloca.getResult() instanceof LLSymbolOp) {
						LLSymbolOp result = (LLSymbolOp) alloca.getResult();
						if (isDynamicVariable(alloca.getResult().getType().getSubType())) 
							allocateDynamicArray(result.getSymbol(), alloca);
						else if (forceLocalsToStack)
							allocateLocal(result.getSymbol(), alloca.getType());
						else
							allocateTemp(result.getSymbol(), alloca.getType());
					} else
						assert false;
				} else if (instr instanceof LLAssignInstr) {
					// normal expression temp; try for a register again
					LLAssignInstr assign = (LLAssignInstr) instr;
					if (assign.getResult() instanceof LLTempOp) {
						allocateTemp(assign);
						tempDefs.put(((LLTempOp) assign.getResult()).getNumber(), assign);
					}
					else if (assign.getResult() != null)
						assert false;
				}
				else if (instr instanceof LLStoreInstr) {
					// see if we're initially storing into an argument local
					matchLocalAllocation((LLStoreInstr) instr);
				}
				return true;
			}
			@Override
			public boolean enterOperand(LLInstr instr, int num,
					LLOperand operand) {
				ILocal local = getLocal(operand);
				if (local != null) {
					updateLocalUsage(instr, local);
				}
				return true;
			}

			/**
			 * @param currentBlock2
			 * @param instr
			 */
			protected void updateLocalUsage(LLInstr instr, ILocal local) {
				if (local.getIncoming() != null)
					updateLocalUsage(instr, local.getIncoming());
			}


			/**
			 * @param symbol
			 * @param alloca
			 */
			protected void allocateDynamicArray(ISymbol symbol, LLAllocaInstr alloca) {
				
				assert false;  // not implemented
				
				LLOperand sizeOp = alloca.getOperands()[0];
				
				// fix up the unneeded cast-to-i32
				if (sizeOp instanceof LLTempOp) {
					LLAssignInstr instr = tempDefs.get(((LLTempOp) sizeOp).getNumber());
					instr.flags().add(LLInstr.FLAG_USE_INT_TYPE);
					alloca.flags().add(LLInstr.FLAG_USE_INT_TYPE);
				}

				allocateTemp(symbol, def.getTypeEngine().getPointerType(alloca.getResult().getType().getSubType().getSubType()));
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
			if (arg != null && arg.getIncoming() == null) {
				ISymbol mirrorSym = ((LLSymbolOp) ops[1]).getSymbol();
				StackLocal mirror = stackLocals.get(mirrorSym);
				if (mirror != null) {
					if (DUMP) System.out.println("Reassigning " + mirror + " to " + arg);
					
					int curOffset = mirror.getOffset();
					mirror.setIncoming(arg);
					if (arg instanceof StackLocal) {
						mirror.setOffset(((StackLocal) arg).getOffset());
					}
					
					// recover stack space: should always work since we store
					// immediately after allocating
					int argStackSize = alignment.alignedSize(mirror.getType());
					if (-curOffset * 8 /*+ argStackSize*/ == alignment.sizeof()) {
						alignment.add(-argStackSize);
					} else {
						System.err.println("Failed to recover stack space");
					}
				}
			}
		}
	}

	/**
	 * Find where we want to place the arguments.
	 */
	private void allocateParams(ICallingConvention cc) {
		Location[] locations = cc.getArgumentLocations();
		
		for (Location loc : locations) {
			ILocal local = null;
			if (loc instanceof CallerStackLocation) {
				// fixed register
				ICallingConvention.CallerStackLocation regLoc = (CallerStackLocation) loc;
				local = allocateVarRegister(localScope.add(loc.name, false),
						loc.type, regLoc.regClass, regLoc.number);
			} 
			else if (loc instanceof RegisterLocation) {
				// fixed register
				ICallingConvention.RegisterLocation regLoc = (RegisterLocation) loc;
				local = allocateVarRegister(localScope.get(loc.name),
						loc.type, regLoc.regClass, regLoc.number);
			}
			else if (loc instanceof StackLocation) {
				ICallingConvention.StackLocation stackLoc = (StackLocation) loc;
				
				local = allocateLocal(localScope.get(loc.name), loc.type, -stackLoc.offset);
			}
			else if (loc instanceof StackBarrierLocation) {
				continue;
			}
			else 
				assert false;
			
			ISymbol arg = localScope.get(loc.name);
			assert arg != null;
			arg.setType(loc.type);
			argumentLocals.put(arg, local);
		}
	}

	public StackLocal allocateLocal(ISymbol name, LLType type) {
		assert !isDynamicVariable(type);
		// we measure from the end of the type; align once for the next address
		// and bump again for the end of the type.
		int offs = alignment.alignAndAdd(type);
		int size = alignment.alignedSize(type);
		int gap = alignment.alignmentGap(type);	
		return allocateLocal(name, type, (offs + size + gap) / 8);
	}

	/**
	 * @param symbol
	 * @param type
	 * @param size 
	 * @return 
	 */
	protected StackLocal allocateDynamicArray(ISymbol symbol, LLType type) {
		StackLocal local = allocateLocal(symbol, type, 0);
		local.setIsAlloca(true);
		return local;
	}

	private StackLocal allocateLocal(ISymbol name, LLType type, int byteOffs) {
		StackLocal local = new StackLocal(name, type, currentBlock.getLabel(), -byteOffs);
		
		assert !stackLocals.containsKey(name);
		
		stackLocals.put(name, local);
		if (DUMP) System.out.println("Allocated " + local);
		
		return local;
	}

	private RegisterLocal allocateVarRegister(ISymbol name, LLType type, IRegClass regClass, int number) {
		RegAlloc alloc = regAllocs.get(regClass);
		assert alloc != null;
		
		RegisterLocal local = new RegisterLocal(regClass, name, type, number);
		
		if (DUMP) System.out.println("Allocated " + local);
		regLocals.put(name, (RegisterLocal) local);
		
		// not a temp
		
		return local;
	}

	public ILocal allocateTemp(LLAssignInstr instr) {
		
		LLTempOp result = (LLTempOp) instr.getResult();
		
		// must not fail: these names are not legal ids and LLVM guarantees they're unique
		ISymbol name = localScope.add(result.getName(), false);
		name.setType(result.getType());
		
		return allocateTemp(name, result.getType());
	}
	
	public ILocal allocateTemp(ISymbol name, LLType type) {
		ILocal local = null;
		
		for (RegAlloc regAlloc : regAllocs.values()) {
			try {
				local = regAlloc.allocate(name, type);
				break;
			} catch (UnsupportedOperationException e) {
				
			}
		}
		
		if (local == null) {
			return allocateLocal(name, type);
		}
		
		if (DUMP) System.out.println("Allocated " + local);
		
		assert !regLocals.containsKey(name);
		regLocals.put(name, (RegisterLocal) local);
		tempLocals.put(name, (RegisterLocal) local);
		
		local.setExprTemp(true);

		return local;
	}

	public ILocal allocateTemp(LLType type) {
		ISymbol name = localScope.add("%reg", true);
		name.setType(type);
		return allocateTemp(name, type);
	}

	/**
	 * @return size in bytes
	 */
	public int getFrameSize() {
		int size = alignment.sizeof() / 8;
		if (size % 2 != 0)
			size++;
		return size;
	}

	/**
	 * @return
	 */
	public IScope getScope() {
		return localScope;
	}

	/**
	 * @param symbol
	 */
	public boolean forceToRegister(RegisterLocal regLocal, int reg) {
		regLocal.setVr(reg);
		if (DUMP) System.out.println("Reassigned " + regLocal);
		return true;
	}

	/**
	 * Get the local referenced by this operand.
	 * @param operand
	 * @return
	 */
	public ILocal getLocal(LLOperand operand) {
		ISymbol sym = null;
		if (operand instanceof LLTempOp) {
			sym = localScope.get(((LLTempOp) operand).getName());
		} else if (operand instanceof LLSymbolOp) {
			sym = ((LLSymbolOp) operand).getSymbol();
		} 
		if (sym == null)
			return null;
		return getLocal(sym);
	}

	/**
	 * Get the local referenced by this symbol.
	 * @param sym
	 * @return local or <code>null</code> if not local or not a variable/temp
	 */
	public ILocal getLocal(ISymbol sym) {
		ILocal local = regLocals.get(sym);
		if (local == null)
			local = stackLocals.get(sym);
		return local;
	}

	/**
	 * Get the local that is used for the operand's storage (if it is a symbol)
	 * @param op
	 * @return local or <code>null</code> if not a local
	 */
	public ILocal getFinalLocal(LLOperand op) {
		ILocal local = getLocal(op);
		if (local == null)
			return null;
		if (local.getIncoming() != null)
			local = local.getIncoming();
		return local;
	}

	/**
	 * Get the local that is used for the symbol's storage
	 * @param sym
	 * @return
	 */
	public ILocal getFinalLocal(ISymbol sym) {
		ILocal local = getLocal(sym);
		if (local == null)
			return null;
		if (local.getIncoming() != null)
			local = local.getIncoming();
		return local;
	}

	/**
	 * Get all the locals (stack, register, temp)
	 * @return
	 */
	public ILocal[] getAllLocals() {
		Map<ISymbol, ILocal> map = new LinkedHashMap<ISymbol, ILocal>();
		for (Map.Entry<ISymbol, ? extends ILocal> entry : stackLocals.entrySet()) {
			map.put(entry.getKey(), entry.getValue());
		}
		for (Map.Entry<ISymbol, ? extends ILocal> entry : regLocals.entrySet()) {
			map.put(entry.getKey(), entry.getValue());
		}
		return (ILocal[]) map.values().toArray(new ILocal[map.values().size()]);
	}

	public void removeLocal(ILocal local) {
		assert !argumentLocals.containsValue(local);
		tempLocals.remove(local.getName());
		if (local instanceof RegisterLocal)
			regLocals.remove(local.getName());
		else if (local instanceof StackLocal)
			stackLocals.remove(local.getName());
		else
			assert false;
	}

	public boolean requiresFramePointer() {
		return requiresFramePointer;
	}

	public void setRequiresFramePointer(boolean requiresFramePointer) {
		this.requiresFramePointer = requiresFramePointer;
	}

	private boolean isDynamicVariable(LLType llType) {
		return llType instanceof LLArrayType && ((LLArrayType) llType).getDynamicSizeExpr() != null;
	}

	
}
