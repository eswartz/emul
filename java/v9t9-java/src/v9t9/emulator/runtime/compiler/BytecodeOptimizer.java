/**
 * 
 */
package v9t9.emulator.runtime.compiler;

import java.util.Iterator;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.LocalVariableInstruction;
import org.apache.bcel.generic.PushInstruction;
import org.apache.bcel.generic.StackProducer;
import org.apache.bcel.generic.TargetLostException;
import org.apache.bcel.util.InstructionFinder;

/**
 * Optimizer for bytecode
 * @author ejs
 *
 */
public class BytecodeOptimizer {


	public static void peephole(CompileInfo info, Compiler9900.InstInfo ii) {
	    boolean changed;
	
	    //int origCount = info.ilist.size();
	    
	    do {
	        changed = false;
	        changed |= removeStoreLoads(info);
	        changed |= removeStoreStackOpLoads(info);
	        changed |= removeSwaps(info);
	        if (changed) {
	        	removeNOPs(info);
	        	
	        	//System.out.println("Changed " + origCount + " instructions to " + info.ilist.size());
			}
	
	        
	    } while (changed);
	
	}

	/**
	 * Cleanup this pattern:
	 * 
	 * <pre>
	 *   ISTORE n
	 *   ILOAD n
	 * </pre>
	 * 
	 * @param info
	 * @return true: changes made
	 */
	@SuppressWarnings("unchecked")
	private static final boolean removeStoreLoads(CompileInfo info) {
	    InstructionFinder f = new InstructionFinder(info.ilist);
	    String pat = "ISTORE ILOAD";
	    int count = 0;
	
	    for (Iterator<InstructionHandle[]> e = f.search(pat); e.hasNext();) {
	        InstructionHandle[] match = (InstructionHandle[]) e.next();
	        InstructionHandle first = match[0];
	        InstructionHandle last = match[1];
	
	        // see if they share the same local
	        LocalVariableInstruction store = (LocalVariableInstruction) first
	                .getInstruction();
	        LocalVariableInstruction load = (LocalVariableInstruction) last
	                .getInstruction();
	        if (store.getIndex() == load.getIndex()
	                && sameBlocks(first, last)) {
	            // ensure this is the last use
	            InstructionHandle anotherUse = findNextLocalUse(store
	                    .getIndex(), last);
	            if (anotherUse != null) {
					continue;
				}
	
	            count++;
	            first.setInstruction(InstructionConstants.NOP);
	            last.setInstruction(InstructionConstants.NOP);
	            f.reread();
	        }
	    }
	
	    if (count > 0) {
	        // System.out.println("Removed " + count
	        // + " STORE/LOAD pairs from method");
	        return true;
	    }
	    return false;
	}

	/**
	 * Cleanup this pattern:
	 * 
	 * <pre>
	 *   ISTORE n
	 *   stackload
	 *   ILOAD n
	 * </pre>
	 * 
	 * into:
	 * 
	 * <pre>
	 *   stackload
	 *   SWAP
	 * </pre>
	 * 
	 * @param info
	 * @return true: changes made
	 */
	@SuppressWarnings("unchecked")
	private static final boolean removeStoreStackOpLoads(CompileInfo info) {
	    InstructionFinder f = new InstructionFinder(info.ilist);
	    String pat = "ISTORE StackProducer ILOAD";
	    int count = 0;
	
	    for (Iterator<InstructionHandle[]> e = f.search(pat); e.hasNext();) {
	        InstructionHandle[] match = (InstructionHandle[]) e.next();
	        if (match.length != 3) {
				throw new AssertionError();
			}
	        InstructionHandle first = match[0];
	        InstructionHandle middle = match[1];
	        InstructionHandle last = match[2];
	
	        // see if they share the same local
	        LocalVariableInstruction store = (LocalVariableInstruction) first
	                .getInstruction();
	        StackProducer stackop = (StackProducer) middle.getInstruction();
	        LocalVariableInstruction load = (LocalVariableInstruction) last
	                .getInstruction();
	        if (store.getIndex() == load.getIndex()
	                && sameBlocks(first, last)
	                && stackop.produceStack(info.pgen) == 1) {
	            // ensure this is the last use
	            InstructionHandle anotherUse = findNextLocalUse(store
	                    .getIndex(), last);
	            if (anotherUse != null) {
					continue;
				}
	
	            count++;
	
	            // change to SWAP
	            first.setInstruction(InstructionConstants.NOP);
	            last.setInstruction(InstructionConstants.SWAP);
	            f.reread();
	        }
	    }
	
	    if (count > 0) {
	        // System.out.println("Removed " + count
	        // + " STORE/stackop/LOAD pairs from method");
	        return true;
	    }
	    return false;
	}

	@SuppressWarnings("unchecked")
	static final boolean removeSwaps(CompileInfo info) {
	    InstructionFinder f = new InstructionFinder(info.ilist);
	    String pat = "PushInstruction PushInstruction SWAP";
	    int count = 0;
	
	    for (Iterator<InstructionHandle[]> e = f.search(pat); e.hasNext();) {
	        InstructionHandle[] match = e.next();
	        if (match.length != 3) {
				throw new AssertionError();
			}
	        InstructionHandle first = match[0];
	        InstructionHandle middle = match[1];
	        InstructionHandle last = match[2];
	
	        PushInstruction prod1 = (PushInstruction) first.getInstruction();
	        PushInstruction prod2 = (PushInstruction) middle.getInstruction();
	        if (sameBlocks(first, last)
	                && prod1.produceStack(info.pgen) == 1
	                && prod2.produceStack(info.pgen) == 1
	                && first.getInstruction().getOpcode() != Constants.SWAP
	                && middle.getInstruction().getOpcode() != Constants.SWAP) {
	            count++;
	
	            // remove SWAP
	            org.apache.bcel.generic.Instruction tmp = first
	                    .getInstruction();
	            first.setInstruction(middle.getInstruction());
	            middle.setInstruction(tmp);
	            last.setInstruction(InstructionConstants.NOP);
	            f.reread();
	        }
	    }
	
	    if (count > 0) {
	        // System.out.println("Removed " + count
	        // + " stackop/stackop/SWAP pairs from method");
	        return true;
	    }
	    return false;
	}

	private static boolean sameBlocks(InstructionHandle a, InstructionHandle b) {
	    while (a != b) {
	        if (a.getTargeters() != null) {
				return false;
			}
	        a = a.getNext();
	    }
	    return true;
	}

	private static InstructionHandle findNextLocalUse(int index,
	        InstructionHandle after) {
	    InstructionHandle step = after.getNext();
	    while (step != null) {
	        if (step.getInstruction() instanceof LocalVariableInstruction) {
	            LocalVariableInstruction inst = (LocalVariableInstruction) step
	                    .getInstruction();
	            if (inst.getIndex() == index) {
					return step;
				}
	        }
	        step = step.getNext();
	    }
	    return null;
	}

	@SuppressWarnings("unchecked")
	private static final void removeNOPs(CompileInfo info) {
	    InstructionFinder f = new InstructionFinder(info.ilist);
	    String pat = "NOP+"; // Find at least one NOP
	    InstructionHandle next = null;
	    int count = 0;
	
	    for (Iterator<InstructionHandle[]> e = f.search(pat); e.hasNext();) {
	        InstructionHandle[] match = (InstructionHandle[]) e.next();
	        InstructionHandle first = match[0];
	        InstructionHandle last = match[match.length - 1];
	
	        /*
	         * Some nasty Java compilers may add NOP at end of method.
	         */
	        if ((next = last.getNext()) == null) {
				break;
			}
	
	        count += match.length;
	
	        /*
	         * Delete NOPs and redirect any references to them to the following
	         * (non-nop) instruction.
	         */
	        try {
	            info.ilist.delete(first, last);
	        } catch (TargetLostException ex) {
	            InstructionHandle[] targets = ex.getTargets();
	            for (InstructionHandle element : targets) {
	                InstructionTargeter[] targeters = element.getTargeters();
	
	                for (InstructionTargeter element2 : targeters) {
						element2.updateTarget(element, next);
					}
	            }
	        }
	    }
	
	    if (count > 0) {
	        // System.out.println("Removed " + count
	        // + " NOP instructions from method");
	    }
	
	    // info.ilist.dispose(); // Reuse instruction handles
	}

}
