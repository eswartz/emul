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
 * <p>
 * The stack looks like this:
 * 
 * When a frame pointer is used:
 * 
 * <pre>
 * SP -K  location of next callee's args
 *    -s3 ...
 *    -s2 local 2
 *    -s1 local 1
 *     0  saved FP
 *     2  saved reg 0
 *     4  saved ...
 *     6  last saved register
 *     8  last non-reg arg
 *    10  non-reg arg N-1
 *    12  ...
 *    +K  non-reg arg 1
 * </pre>
 * 
 * This is handled with:
 * 
 * <pre>
 *    AI SP, -saved*2+2	// save any registers (plus room for FP)
 *    MOV R11, *SP		// if called
 *    MOV R..., @2(SP)		
 *    //DECT SP			// only if no saved registers			
 *    MOV FP, *SP		// save FP
 *    MOV SP, FP		// new FP
 *    AI  SP, -K		// locals
 *    ...
 *    MOV FP, SP		// remove locals
 *    MOV *SP+, FP  	// restore FP
 *    MOV *SP+, R11		// restore registers
 *    MOV *SP+, R...	// restore registers
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
 *     0  saved reg 0
 *     2  saved ...
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
 *    MOV R11, *SP		// if calls
 *    MOV R..., @2(SP)		
 *    ...
 *    AI SP, K  	  	// remove locals (only)
 *    MOV *SP+, R11		// restore registers
 *    MOV *SP+, R...	// restore registers
 *    B *R11
 * </pre>
 * (min overhead: no locals, no calls: 0, 2) 
 * (min overhead: no locals, calls: 10) 
 * (min overhead: locals, calls: 16) 

 * @author ejs
 *
 */
public class Locals {

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
	
	public Locals(LLDefineDirective def) {
		this.def = def;
		
		ITarget target = def.getTarget();
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
	public Map<ISymbol, RegisterLocal> getRegLocals() {
		return regLocals;
	}
	
	public void buildLocalTable() {
		currentBlock = def.getEntryBlock();
		allocateParams();
		
		final Map<ILocal, LLBlock> lastLocalUse = new HashMap<ILocal, LLBlock>();
		
		ILLCodeVisitor visitor = new LLCodeVisitor() {
			@Override
			public boolean enterBlock(LLBlock block) {
				currentBlock = block;
				System.out.println("Block " + currentBlock.getLabel());
				return true;
			}
			@Override
			public boolean enterInstr(LLBlock block, LLInstr instr) {
				if (instr instanceof LLAssignInstr) {
					LLAssignInstr assign = (LLAssignInstr) instr;
					if (assign.getResult() instanceof LLSymbolOp)
						allocateLocal(assign).setLastUse(instr);
					else if (assign.getResult() instanceof LLTempOp)
						allocateTemp(assign).setLastUse(instr);
					else
						assert false;
				}
				else if (instr instanceof LLStoreInstr) {
					// see if we're storing into an argument local
					matchLocalAllocation((LLStoreInstr) instr);
				}
				return true;
			}
			@Override
			public boolean enterOperand(LLInstr instr, int num,
					LLOperand operand) {
				ILocal local = getLocal(operand);
				if (local != null) {
					LLBlock lastBlock = lastLocalUse.get(local);
					if (lastBlock != null && lastBlock != currentBlock)
						local.setSingleBlock(false);
					local.setLastUse(instr);
				}
				return true;
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
			if (arg.getIncoming() == null) {
				ISymbol mirrorSym = ((LLSymbolOp) ops[1]).getSymbol();
				StackLocal mirror = stackLocals.get(mirrorSym);
				System.out.println("Reassigning " + mirror + " to " + arg);
				
				int curOffset = mirror.getOffset();
				mirror.setIncoming(arg);
				if (arg instanceof StackLocal) {
					mirror.setOffset(((StackLocal) arg).getOffset());
				} else if (arg instanceof RegisterLocal) {
					stackLocals.remove(mirrorSym);
					regLocals.put(mirrorSym, (RegisterLocal) arg);
				}
				
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
				
				local = allocateLocal(localScope.add(loc.name, true), loc.type, stackLoc.offset); 
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
		
		local.setSingleBlock(false);
		
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

		// guess
		local.setSingleBlock(true);	

		return local;
	}

	public ILocal allocateTemp(LLType type) {
		ISymbol name = localScope.addTemporary("%reg");
		name.setType(type);
		return allocateTemp(name, type);
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

	/**
	 * @return
	 */
	public IScope getScope() {
		return localScope;
	}

	/**
	 * @param symbol
	 */
	public boolean forceToRegister(ISymbol symbol, int reg) {
		RegisterLocal regLocal = regLocals.get(symbol);
		if (regLocal == null)
			return false;
		regLocal.setVr(reg);
		return true;
	}

	/**
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
	 * @param sym
	 * @return
	 */
	public ILocal getLocal(ISymbol sym) {
		ILocal local = regLocals.get(sym);
		if (local == null)
			local = stackLocals.get(sym);
		if (local == null)
			local = argumentLocals.get(sym);
		return local;
	}


}
